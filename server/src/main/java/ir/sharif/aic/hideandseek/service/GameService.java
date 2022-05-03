package ir.sharif.aic.hideandseek.service;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.database.InMemoryDataBase;
import ir.sharif.aic.hideandseek.exception.DeclareReadinessException;
import ir.sharif.aic.hideandseek.models.Player;
import ir.sharif.aic.hideandseek.util.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {
    private final InMemoryDataBase database;

    public HideAndSeek.DeclareReadinessResponse declareReadinessForClient(HideAndSeek.DeclareReadinessRequest request) {
        checkIfLobbyIsFull();
        checkThiefAndPoliceNumber(request);
        var token = Utility.generateNewToken();
        addPlayerToDatabase(request, token);
        return HideAndSeek.DeclareReadinessResponse.newBuilder().setPlayerToken(token
        ).build();
    }

    private void addPlayerToDatabase(HideAndSeek.DeclareReadinessRequest request, String token) {
        database.getPlayers().add(createNewPlayer(request, token));
    }

    private Player createNewPlayer(HideAndSeek.DeclareReadinessRequest request, String token) {
        return Player.builder().playerType(request.getPlayerType()).token(token).team(request.getTeam()).build();
    }

    private void checkIfLobbyIsFull() {
        if (getMaximumClientNumber() <= database.getPlayers().size()) {
            throw new DeclareReadinessException("Lobby is full!");
        }
    }

    private void checkThiefAndPoliceNumber(HideAndSeek.DeclareReadinessRequest request) {
        var currentNumber = database.getPlayers()
                .stream()
                .filter(player -> player.getTeam().equals(request.getTeam()) && player.getPlayerType().equals(request.getPlayerType()))
                .count();
        if (request.getPlayerType() == HideAndSeek.PlayerType.POLICE && currentNumber == database.getMaximumPoliceNumber()) {
            throw new DeclareReadinessException("Can't add anymore police to the game!");
        } else if (request.getPlayerType().equals(HideAndSeek.PlayerType.THIEF) && currentNumber == database.getMaximumThiefNumber()) {
            throw new DeclareReadinessException("Can't add anymore thief to the game!");
        }
    }

    private int getMaximumClientNumber() {
        return database.getMaximumThiefNumber() * 2 + database.getMaximumPoliceNumber() * 2;
    }
}
