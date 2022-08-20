package ir.sharif.aic.hideandseek.core.watchers;

import ir.sharif.aic.hideandseek.core.app.GameService;
import ir.sharif.aic.hideandseek.core.events.GameEvent;
import ir.sharif.aic.hideandseek.core.events.GameStatusChangedEvent;
import ir.sharif.aic.hideandseek.core.events.GameTurnChangedEvent;
import ir.sharif.aic.hideandseek.core.models.*;
import ir.sharif.aic.hideandseek.lib.channel.Channel;
import ir.sharif.aic.hideandseek.lib.channel.Watcher;
import lombok.AllArgsConstructor;

import java.util.Comparator;
import java.util.Random;

@AllArgsConstructor
public class NextTurnWatcher implements Watcher<GameEvent> {
    private final Channel<GameEvent> eventChannel;
    private final GameConfig gameConfig;
    private final GameService gameService;
    private final Random random = new Random();


    @Override
    public void watch(GameEvent event) {
        Runnable clientReadinessTimer = () -> {
            try {
                Thread.sleep(gameConfig.getClientReadinessThresholdTimeMillisecond());
                var status = gameService.getStatus();
                if (status.equals(GameStatus.PENDING)) {
                    gameService.changeGameStatusTo(GameStatus.ONGOING);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        Runnable timer = () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            var turn = gameService.getTurn();
            gameService.setTurn(turn.next());
            var currentTurn = gameService.getCurrentTurnNumber();
            boolean isVisible = gameConfig.getTurnSettings().getVisibleTurns().contains(currentTurn);
            eventChannel.push(
                    new GameTurnChangedEvent(gameService.getTurn().getTurnType(), gameService.getCurrentTurnNumber(), isVisible));
        };

        if (gameService.getStatus().equals(GameStatus.PENDING))
            clientReadinessTimer.run();

        if (event instanceof GameStatusChangedEvent && gameService.getStatus().equals(GameStatus.ONGOING)) {
            this.moveAgents();
            this.initNextTurn();
            this.arrestThieves();
            this.chargeBalances();
            this.figureOutGameResult();
            timer.run();
        }
        if (event instanceof GameStatusChangedEvent && gameService.getStatus().equals(GameStatus.FINISHED)) {
            new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(200);
                        System.exit(0);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }.run();
        }


        if (event instanceof GameTurnChangedEvent) {
            this.moveAgents();
            this.initNextTurn();
            this.arrestThieves();
            this.chargeBalances();
            this.figureOutGameResult();
            if (!gameService.getStatus().equals(GameStatus.FINISHED)) {
                timer.run();
            }
        }
    }

    private synchronized void moveAgents() {
        Agent.getAgentMovedEvents().forEach(e -> {
            var agent = gameConfig.findAgentById(e.getAgentId());
            agent.setNodeId(e.getNodeId());
            eventChannel.push(e);
        });
    }

    private void initNextTurn() {
        this.gameConfig.getAllAgents().forEach(Agent::onTurnChange);
        Agent.getAgentMovedEvents().clear();
    }

    public void arrestThieves() {
        var nodes = this.gameConfig.getAllNodes();

        for (var node : nodes) {
            for (var team : Team.values()) {
                this.gameService.arrestThieves(node, team);
            }
        }
    }

    public void chargeBalances() {
        var allPolice = this.gameConfig.findAllPolice();
        var policeIncome = this.gameConfig.getIncomeSettings().getPoliceIncomeEachTurn();
        allPolice.forEach(police -> police.chargeBalance(policeIncome, this.eventChannel));

        var aliveThieves = this.gameConfig.findAliveThieves();
        var thievesIncome = this.gameConfig.getIncomeSettings().getThiefIncomeEachTurn();
        aliveThieves.forEach(thief -> thief.chargeBalance(thievesIncome, this.eventChannel));
    }

    public void figureOutGameResult() {
        var firstTeamHasAnyAliveThief = this.gameConfig.hasAliveThief(Team.FIRST);
        var secondTeamHasAnyAliveThief = this.gameConfig.hasAliveThief(Team.SECOND);

        if (firstTeamHasAnyAliveThief && !secondTeamHasAnyAliveThief) {
            this.gameService.changeGameResultTo(GameResult.FIRST_WINS);
            return;
        }

        if (secondTeamHasAnyAliveThief && !firstTeamHasAnyAliveThief) {
            this.gameService.changeGameResultTo(GameResult.SECOND_WINS);
            return;
        }

        if (!firstTeamHasAnyAliveThief || gameService.isAllTurnsFinished()) {
            this.figureOutGameResultByDetails();
        }
    }

    private void figureOutGameResultByDetails() {
        var firstTeamDeadThieves = this.gameConfig.findAllThievesByTeam(Team.FIRST)
                .stream().filter(Agent::isDead)
                .sorted(Comparator.comparingInt(Agent::getTurnDeadAt).reversed()).toList();
        var secondTeamDeadThieves = this.gameConfig.findAllThievesByTeam(Team.SECOND)
                .stream().filter(Agent::isDead)
                .sorted(Comparator.comparingInt(Agent::getTurnDeadAt).reversed()).toList();

        if (firstTeamDeadThieves.size() > secondTeamDeadThieves.size()) {
            this.gameService.changeGameResultTo(GameResult.SECOND_WINS);
            return;
        } else if (firstTeamDeadThieves.size() < secondTeamDeadThieves.size()) {
            this.gameService.changeGameResultTo(GameResult.FIRST_WINS);
            return;
        }

        if (firstTeamDeadThieves.size() == 0 && !gameService.isAllTurnsFinished()) {
            return;
        }

        for (int i = 0; i < firstTeamDeadThieves.size(); i++) {
            var firstTeamThief = firstTeamDeadThieves.get(i);
            var secondTeamThief = secondTeamDeadThieves.get(i);

            if (firstTeamThief.getTurnDeadAt() < secondTeamThief.getTurnDeadAt()) {
                this.gameService.changeGameResultTo(GameResult.SECOND_WINS);
                return;
            } else if (firstTeamThief.getTurnDeadAt() > secondTeamThief.getTurnDeadAt()) {
                this.gameService.changeGameResultTo(GameResult.FIRST_WINS);
                return;
            }
        }

        if (random.nextBoolean()) {
            this.gameService.changeGameResultTo(GameResult.FIRST_WINS);
        } else {
            this.gameService.changeGameResultTo(GameResult.SECOND_WINS);
        }
    }
}
