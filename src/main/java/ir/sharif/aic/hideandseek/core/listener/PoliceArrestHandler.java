package ir.sharif.aic.hideandseek.core.listener;

import ir.sharif.aic.hideandseek.core.app.GameService;
import ir.sharif.aic.hideandseek.core.events.GameEvent;
import ir.sharif.aic.hideandseek.core.events.GameTurnChangedEvent;
import ir.sharif.aic.hideandseek.core.models.GameRepository;
import ir.sharif.aic.hideandseek.core.models.Team;
import ir.sharif.aic.hideandseek.lib.channel.Watcher;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PoliceArrestHandler implements Watcher<GameEvent> {
  private final GameRepository gameRepository;
  private final GameService gameService;

  @Override
  public void watch(GameEvent event) {
    if (event instanceof GameTurnChangedEvent) {
      handleArrestedThieves();
    }
  }

  public void handleArrestedThieves() {
    var nodes = this.gameRepository.getAllNodes();

    for (var node : nodes) {
      for (var team : Team.values()) {
        this.gameService.arrestThieves(node, team);
      }
    }
  }
}
