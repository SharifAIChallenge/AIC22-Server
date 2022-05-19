package ir.sharif.aic.hideandseek.api;

import ir.sharif.aic.hideandseek.core.app.GameService;
import ir.sharif.aic.hideandseek.core.events.GameEvent;
import ir.sharif.aic.hideandseek.core.events.GameTurnChangedEvent;
import ir.sharif.aic.hideandseek.core.events.PoliceCaughtThieves;
import ir.sharif.aic.hideandseek.core.models.Agent;
import ir.sharif.aic.hideandseek.core.models.GameRepository;
import ir.sharif.aic.hideandseek.core.models.Node;
import ir.sharif.aic.hideandseek.core.models.Team;
import ir.sharif.aic.hideandseek.lib.channel.Channel;
import ir.sharif.aic.hideandseek.lib.channel.Watcher;
import lombok.AllArgsConstructor;

import java.util.Arrays;

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
        nodes.forEach(node -> Arrays.stream(Team.values()).forEach(team -> gameService.arrestThieves(node, team)));
    }


}
