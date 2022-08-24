package ir.sharif.aic.hideandseek.core.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.config.GameConfigInjector;
import ir.sharif.aic.hideandseek.core.commands.ChatCommand;
import ir.sharif.aic.hideandseek.core.commands.DeclareReadinessCommand;
import ir.sharif.aic.hideandseek.core.events.*;
import ir.sharif.aic.hideandseek.core.exceptions.InternalException;
import ir.sharif.aic.hideandseek.core.exceptions.PreconditionException;
import ir.sharif.aic.hideandseek.core.exceptions.ValidationException;
import ir.sharif.aic.hideandseek.lib.channel.Channel;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Data
public class Agent {
    private Integer id;
    private Integer nodeId;
    private String token;
    private Team team;
    private AgentType type;
    private Double balance;
    @Getter
    private static List<AgentMovedEvent> agentMovedEvents = new ArrayList<>();
    @JsonIgnore
    private boolean ready = false;
    @JsonIgnore
    private boolean dead = false;
    @JsonIgnore
    private int turnDeadAt = -1;
    @JsonIgnore
    private boolean visible = true;
    @JsonIgnore
    private boolean movedThisTurn = false;
    @JsonIgnore
    private boolean sentMessageThisTurn = false;

    public synchronized void apply(DeclareReadinessCommand cmd, Channel<GameEvent> eventChannel) {
        // validations
        cmd.validate();
        if (!(this.token != null && this.token.equals(cmd.getToken()))) {
            throw new InternalException("command token does not match agent token");
        }

        if (this.ready) {
            return;
        }

        if (this.is(AgentType.THIEF)) {
            this.nodeId = cmd.getStartNodeId();
        }

        // side effects
        this.ready = true;

        // broadcast event
        var event = new AgentDeclaredReadinessEvent(this);
        eventChannel.push(event);
    }

    public synchronized void chargeBalance(double wage, Channel<GameEvent> eventChannel) {
        if (wage < 0) {
            throw new ValidationException("the wage must be positive", "wage");
        }

        if (this.balance == null) {
            return;
        }

        this.balance += wage;
        eventChannel.push(new AgentBalanceChargedEvent(this.id, wage , this.balance));
    }

    public synchronized void stayInPlace(Channel<GameEvent> eventChannel) {
        if (this.movedThisTurn) {
            throw new PreconditionException("agent has already moved this turn")
              .withDetail("agentId", this.id);
        }

        this.movedThisTurn = true;
        Agent.agentMovedEvents.add(new AgentMovedEvent(this.id, this.nodeId , this.balance));
    }

    public synchronized void moveAlong(Path path, Channel<GameEvent> eventChannel) {
        if (this.movedThisTurn) {
            throw new PreconditionException("agent has already moved this turn")
                    .withDetail("agentId", this.id);
        }

        if (path.getFirstNodeId() != this.nodeId && path.getSecondNodeId() != this.nodeId) {
            throw new InternalException("agent is moving along a path from invalid source")
                    .withDetail("agentId", this.id)
                    .withDetail("currentNodeId", this.nodeId)
                    .withDetail("destinationNodeId", path.getSecondNodeId());
        }

        if (path.getPrice() > this.balance) {
            throw new PreconditionException("agent doesn't have enough balance to move along this path")
                    .withDetail("agentId", this.id)
                    .withDetail("currentBalance", this.balance)
                    .withDetail("pathPrice", path.getPrice())
                    .withDetail("pathId", path.getId());
        }

        this.balance -= path.getPrice();
        var previousNodeId = this.nodeId;
        var newNodeId= previousNodeId == path.getFirstNodeId() ? path.getSecondNodeId() : path.getFirstNodeId();
//        this.nodeId = newNodeId;
        this.movedThisTurn = true;
        Agent.agentMovedEvents.add(new AgentMovedEvent(this.id, previousNodeId, newNodeId, path.getPrice() , this.balance));
    }

    public synchronized void sendMessage(ChatCommand cmd, List<Chat> chatBox, GameConfigInjector.ChatSettings chatSettings, Channel<GameEvent> eventChannel) {
        cmd.validate();

        if(!cmd.getToken().equals(this.token)) {
            throw new InternalException("chat command's token did not match agent token.")
              .withDetail("agentToken", this.token)
              .withDetail("commandToken", cmd.getToken());
        }

        var price = cmd.getText().length() * chatSettings.getChatCostPerCharacter();
        if (price > this.balance) {
            throw new PreconditionException("agent doesn't have enough balance to send this text.")
              .withDetail("agentId", this.id)
              .withDetail("currentBalance", this.balance)
              .withDetail("chatPrice", price)
              .withDetail("text", cmd.getText());
        }

        this.sentMessageThisTurn = true;
        this.balance -= price;
        var chat = new Chat(this.id, cmd.getText(), this.team, this.type, price);

        chatBox.add(chat);
        eventChannel.push(new AgentSentMessageEvent(chat , this.balance));
    }

    public boolean hasId(int anId) {
        return this.id == anId;
    }

    public boolean isInTheSameTeam(Agent agent) {
        return this.team != null && this.team.equals(agent.getTeam());
    }

    public boolean is(AgentType type) {
        return this.type != null && this.type.equals(type);
    }

    public boolean hasMovedThisTurn() {
        return this.movedThisTurn;
    }

    public boolean hasSentMessageThisTurn() {
        return this.sentMessageThisTurn;
    }

    public boolean cannotDoActionOnTurn(TurnType turnType) {
        return switch (this.type) {
            case POLICE -> !turnType.equals(TurnType.POLICE_TURN);
            case THIEF -> !turnType.equals(TurnType.THIEF_TURN);
        };
    }

    public boolean isAlive() {
        return !this.dead;
    }

    public void onTurnChange() {
        this.movedThisTurn = false;
        this.sentMessageThisTurn = false;
    }

    public void arrest(int currentTurn) {
        this.dead = true;
        this.turnDeadAt = currentTurn;
    }

    public void validate() {
        if (this.id == null) throw new ValidationException("agent id cannot be null", "agent.id");
        if (this.id <= 0) throw new ValidationException("agent id must be positive", "agent.id");
        GraphValidator.validateNodeId(this.nodeId, "agent.nodeId");
        TokenValidator.validate(this.token, "agent.token");
        if (this.team == null) throw new ValidationException("agent team cannot be null", "agent.team");
        if (this.type == null) throw new ValidationException("agent type cannot be null", "agent.type");
        if (this.type.equals(AgentType.POLICE) && this.nodeId == null)
            throw new ValidationException("police node id cannot be null", "agent.nodeId");
        if (this.balance == null)
            throw new ValidationException("balance cannot be null", "agent.balance");
        if (this.balance < 0)
            throw new ValidationException("balance must be positive", "agent.balance");
    }

    public HideAndSeek.Agent toProto() {
        return HideAndSeek.Agent.newBuilder()
                .setId(this.id)
                .setNodeId(this.nodeId)
                .setTeam(this.team.toProto())
                .setType(this.type.toProto())
                .setIsDead(this.dead)
                .build();
    }
}
