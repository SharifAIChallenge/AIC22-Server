package ir.sharif.aic.hideandseek.config;

import ir.sharif.aic.hideandseek.core.models.Node;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "game.map")
public class GameConfiguration {
    @Value("${game.team.maxPoliceCount}")
    private int maxPoliceCount;
    @Value("${game.team.maxThiefCount}")
    private int maxThiefCount;
    @Value("${game.map.nodes")
    private List<Node> nodes;
}

