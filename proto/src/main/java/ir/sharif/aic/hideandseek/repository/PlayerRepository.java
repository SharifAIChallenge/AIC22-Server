package ir.sharif.aic.hideandseek.repository;

import ir.sharif.aic.hideandseek.database.InMemoryDataBase;
import ir.sharif.aic.hideandseek.models.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public class PlayerRepository {
    private final InMemoryDataBase dataBase;
    @Value("${game.team.thief_number}")
    private int maximumThiefNumber;
    @Value("${game.team.police_number}")
    private int maximumPoliceNumber;

    public void addPlayerToDataBase(Player player) {
        this.dataBase.getPlayers().add(player);
    }

    public Optional<Player> getPlayerByToken(String token) {
        return this.dataBase.getPlayers().stream().filter(e -> e.getToken().equals(token)).findFirst();
    }

    public List<Player> getPlayers() {
        return this.dataBase.getPlayers();
    }

    public int getTotalPlayersNumber() {
        return 2 * maximumPoliceNumber + 2 * maximumThiefNumber;
    }
}
