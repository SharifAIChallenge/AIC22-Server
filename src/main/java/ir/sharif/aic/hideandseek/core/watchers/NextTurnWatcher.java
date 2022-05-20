package ir.sharif.aic.hideandseek.core.watchers;

import ir.sharif.aic.hideandseek.core.app.GameService;
import ir.sharif.aic.hideandseek.core.events.GameEvent;
import ir.sharif.aic.hideandseek.core.events.GameTurnChangedEvent;
import ir.sharif.aic.hideandseek.core.models.GameConfig;
import ir.sharif.aic.hideandseek.core.models.GameResult;
import ir.sharif.aic.hideandseek.core.models.Team;
import ir.sharif.aic.hideandseek.lib.channel.Channel;
import ir.sharif.aic.hideandseek.lib.channel.Watcher;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NextTurnWatcher implements Watcher<GameEvent> {
  private final Channel<GameEvent> eventChannel;
  private final GameConfig gameConfig;
  private final GameService gameService;

  @Override
  public void watch(GameEvent event) {
    if (event instanceof GameTurnChangedEvent) {
      this.arrestThieves();
      this.chargeBalances();
      this.figureOutGameResult();
    }
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

    if (!firstTeamHasAnyAliveThief && !secondTeamHasAnyAliveThief) {
      this.gameService.changeGameResultTo(GameResult.TIE);
      return;
    }

    if (!firstTeamHasAnyAliveThief) {
      this.gameService.changeGameResultTo(GameResult.SECOND_WINS);
      return;
    }

    if (!secondTeamHasAnyAliveThief) {
      this.gameService.changeGameResultTo(GameResult.FIRST_WINS);
      return;
    }
    if (!this.gameService.isAllTurnsFinished()) return;

    changeGameStateIfAllTurnsAreFinished();
  }

  private void changeGameStateIfAllTurnsAreFinished() {
    var firstTeamThiefNumber = this.gameConfig.findAllThiefAgentByTeam(Team.FIRST).size();
    var secondTeamThiefNumber = this.gameConfig.findAllThiefAgentByTeam(Team.SECOND).size();
    if (firstTeamThiefNumber < secondTeamThiefNumber) {
      this.gameService.changeGameResultTo(GameResult.SECOND_WINS);
      return;
    }

    if (firstTeamThiefNumber > secondTeamThiefNumber) {
      this.gameService.changeGameResultTo(GameResult.FIRST_WINS);
      return;
    }

    this.gameService.changeGameResultTo(GameResult.TIE);
  }
}
