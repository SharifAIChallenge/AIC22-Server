package ir.sharif.aic.hideandseek.database;

import ir.sharif.aic.hideandseek.models.Player;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

@Data
public class InMemoryDataBase {

    @Value("${game.team.thief.number}")
    private int maximumThiefNumber;
    @Value("${game.team.police.number}")
    private int maximumPoliceNumber;

    private List<Player> players = new ArrayList<>();
}
