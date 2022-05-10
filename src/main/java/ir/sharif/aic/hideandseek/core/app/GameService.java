package ir.sharif.aic.hideandseek.core.app;

import ir.sharif.aic.hideandseek.api.PoliceArrestHandler;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.commands.DeclareReadinessCommand;
import ir.sharif.aic.hideandseek.core.commands.DoActionCommand;
import ir.sharif.aic.hideandseek.core.commands.WatchCommand;
import ir.sharif.aic.hideandseek.core.events.*;
import ir.sharif.aic.hideandseek.core.exceptions.PreconditionException;
import ir.sharif.aic.hideandseek.core.models.*;
import ir.sharif.aic.hideandseek.lib.channel.AsyncChannel;
import ir.sharif.aic.hideandseek.lib.channel.Channel;
import org.springframework.stereotype.Service;

/**
 * Deaths, Result and Status, Visibility
 */
@Service
public class GameService {
    private final GameRepository gameRepository;
    private final Channel<GameEvent> eventChannel;
    private GameStatus status;
    private GameResult result;
    private Turn turn;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
        this.eventChannel = new AsyncChannel<>();
        this.status = GameStatus.PENDING;
        this.result = GameResult.UNKNOWN;
        this.turn = Turn.THIEF_TURN;
        this.eventChannel.addWatcher(new PoliceArrestHandler(gameRepository, this));
    }

    public synchronized void handle(DeclareReadinessCommand cmd) {
        cmd.validate();
        var agent = this.gameRepository.findAgentByToken(cmd.getToken());
        agent.apply(cmd, this.eventChannel);

        if (this.gameRepository.everyAgentIsReady()) {
            var fromStatus = this.status;
            this.status = GameStatus.ONGOING;
            this.eventChannel.push(new GameStatusChangedEvent(fromStatus, GameStatus.ONGOING));
        }
    }

    public synchronized void handle(WatchCommand cmd) {
        cmd.validate();
        this.gameRepository.assertAgentExistsWithToken(cmd.getToken());
        this.eventChannel.addWatcher(cmd.getWatcher());
    }

    public synchronized void handle(DoActionCommand cmd) {
        cmd.validate();
        var agent = this.gameRepository.findAgentByToken(cmd.getToken());

        if (!agent.canDoActionOnTurn(this.turn)) {
            throw new PreconditionException("it's not your turn yet.");
        }

        if (!agent.isReady()) {
            throw new PreconditionException("you have not declared your readiness yet.");
        }

        var src = agent.getNodeId();
        var dst = cmd.getToNodeId();

        if (src == dst) {
            agent.setMovedThisTurn(true);
            this.eventChannel.push(new AgentMovedEvent(agent.getId(), agent.getNodeId(), agent.getNodeId()));
        } else {
            var path = this.gameRepository.findPath(src, dst);
            agent.moveAlong(path, this.eventChannel);
        }

        if (this.gameRepository.everyAgentHasMovedThisTurn(turn.equals(Turn.THIEF_TURN) ? AgentType.THIEF : AgentType.POLICE)) {
            this.gameRepository.getAllAgents().forEach(Agent::onTurnChange);
            this.turn = this.turn.next();
            this.eventChannel.push(new GameTurnChangedEvent(this.turn));
        }
    }

    public HideAndSeek.GameView getView(String fromToken) {
        var viewerAgent = this.gameRepository.findAgentByToken(fromToken);
        var visibleAgents =
                this.gameRepository.findVisibleAgents(viewerAgent).stream().map(Agent::toProto).toList();

        return HideAndSeek.GameView.newBuilder()
                .setStatus(this.status.toProto())
                .setViewer(viewerAgent.toProto())
                .setResult(this.result.toProto())
                .setSpecs(this.gameRepository.getSpecs())
                .setTurn(this.turn.toProto())
                .addAllVisibleAgents(visibleAgents)
                .build();
    }

    public synchronized void arrestThieves(Node node, Team team) {
        if (this.gameRepository.checkTeamPoliceInNode(team, node)) {
            var thieves = this.gameRepository.getAllThievesByTeamAndNode(team, node);
            thieves.forEach(e->e.setDead(true));
            for (Agent thief : thieves) {
                eventChannel.push(new PoliceCaughtThieves(node.getId(), thief.getId()));
            }
        }
    }


}
