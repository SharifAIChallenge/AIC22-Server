package ir.sharif.aic.hideandseek.core.watchers;

import ir.sharif.aic.hideandseek.core.app.GameService;
import ir.sharif.aic.hideandseek.core.events.GameEvent;
import ir.sharif.aic.hideandseek.core.events.GameResultChangedEvent;
import ir.sharif.aic.hideandseek.core.events.GameStatusChangedEvent;
import ir.sharif.aic.hideandseek.core.events.GameTurnChangedEvent;
import ir.sharif.aic.hideandseek.core.models.*;
import ir.sharif.aic.hideandseek.lib.channel.Channel;
import ir.sharif.aic.hideandseek.lib.channel.Watcher;
import lombok.AllArgsConstructor;

import java.util.Timer;
import java.util.TimerTask;

@AllArgsConstructor
public class NextTurnWatcher implements Watcher<GameEvent> {
    private final Channel<GameEvent> eventChannel;
    private final GameConfig gameConfig;
    private final GameService gameService;

    @Override
    public void watch(GameEvent event) {
        Runnable timer = () -> {
            try {
                Thread.sleep(1000);
                var turn = gameService.getTurn();
                gameService.setTurn(turn.next());
                var currentTurn = gameService.getCurrentTurnNumber();
                boolean isVisible = gameConfig.getTurnSettings().getVisibleTurns().contains(currentTurn);
                eventChannel.push(
                        new GameTurnChangedEvent(gameService.getTurn().getTurnType(), gameService.getCurrentTurnNumber(), isVisible));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        if (event instanceof GameStatusChangedEvent && gameService.getStatus().equals(GameStatus.ONGOING)) {
            timer.run();
        }


        if (event instanceof GameTurnChangedEvent) {
            this.initNextTurn();
            this.arrestThieves();
            this.chargeBalances();
            this.figureOutGameResult();
            if (!gameService.getStatus().equals(GameStatus.FINISHED))
                timer.run();
        }
    }

    private void initNextTurn() {
        this.gameConfig.getAllAgents().forEach(Agent::onTurnChange);
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
        var firstTeamThiefNumber = this.gameConfig.findAllThievesByTeam(Team.FIRST).size();
        var secondTeamThiefNumber = this.gameConfig.findAllThievesByTeam(Team.SECOND).size();
        var firstTeamHasAnyAliveThief = this.gameConfig.hasAliveThief(Team.FIRST);
        var secondTeamHasAnyAliveThief = this.gameConfig.hasAliveThief(Team.SECOND);


        if (!firstTeamHasAnyAliveThief && secondTeamHasAnyAliveThief) {
            this.gameService.changeGameResultTo(GameResult.SECOND_WINS);
            return;
        }

        if (!secondTeamHasAnyAliveThief && firstTeamHasAnyAliveThief) {
            this.gameService.changeGameResultTo(GameResult.FIRST_WINS);
            return;
        }

        if(firstTeamThiefNumber > secondTeamThiefNumber){
            this.gameService.changeGameResultTo(GameResult.FIRST_WINS);
        }else if(firstTeamThiefNumber < secondTeamThiefNumber){
            this.gameService.changeGameResultTo(GameResult.SECOND_WINS);
        }else{
            this.gameService.changeGameResultTo(GameResult.TIE);
        }

    }

//    private void findWinnerTeam() {
//        var firstTeamThievesCount = this.gameConfig.findAllThievesByTeam(Team.FIRST).size();
//        var secondTeamThievesCount = this.gameConfig.findAllThievesByTeam(Team.SECOND).size();
//
//        if (firstTeamThievesCount < secondTeamThievesCount) {
//            this.gameService.changeGameResultTo(GameResult.SECOND_WINS);
//            return;
//        }
//
//        if (firstTeamThievesCount > secondTeamThievesCount) {
//            this.gameService.changeGameResultTo(GameResult.FIRST_WINS);
//            return;
//        }
//
//        this.gameService.changeGameResultTo(GameResult.TIE);
//    }
}
