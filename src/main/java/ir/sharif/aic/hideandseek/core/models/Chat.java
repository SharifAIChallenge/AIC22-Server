package ir.sharif.aic.hideandseek.core.models;

import com.google.protobuf.Timestamp;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class Chat implements Comparable<Chat> {
  private final String id;
  private final int fromAgentId;
  private final Team fromTeam;
  private final AgentType fromType;
  private final String text;
  private final Date timeStamp;
  private final double price;

  public Chat(int fromAgentId, String text, Team fromTeam, AgentType fromType, double price) {
    this.id = UUID.randomUUID().toString();
    this.timeStamp = new Date();
    this.fromAgentId = fromAgentId;
    this.fromTeam = fromTeam;
    this.fromType = fromType;
    this.price = price;
    this.text = text;
  }

  public boolean isFromTeam(Team team) {
    return this.fromTeam != null && this.fromTeam.equals(team);
  }

  public boolean isFromType(AgentType agentType) {
    return this.fromType != null && this.fromType.equals(agentType);
  }

  @Override
  public int compareTo(Chat other) {
    if (this.timeStamp.after(other.getTimeStamp())) {
      return -1;
    }

    if (this.timeStamp.equals(other.getTimeStamp())) {
      return 0;
    }

    return 1;
  }

  public HideAndSeek.Chat toProto() {
    return HideAndSeek.Chat.newBuilder()
        .setId(this.id)
        .setFromAgentId(this.fromAgentId)
        .setText(this.text)
        .setTimeStamp(Timestamp.newBuilder().setNanos(this.timeStamp.toInstant().getNano()).build())
        .build();
  }
}
