package ir.sharif.aic.hideandseek.service;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.exception.DeclareReadinessException;
import ir.sharif.aic.hideandseek.exception.InvalidTokenIdException;
import ir.sharif.aic.hideandseek.models.Player;
import ir.sharif.aic.hideandseek.repository.PlayerRepository;
import ir.sharif.aic.hideandseek.util.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {
    private final PlayerRepository playerRepository;

    public HideAndSeek.DeclareReadinessResponse declareReadinessForClient(HideAndSeek.DeclareReadinessRequest request) {
        checkIfLobbyIsFull();
        checkThiefAndPoliceNumber(request);
        var token = Utility.generateNewToken();
        addPlayerToDatabase(request, token);
        return HideAndSeek.DeclareReadinessResponse.newBuilder().setPlayerToken(token
        ).build();
    }

    public HideAndSeek.GameResponse watchGameRequest(HideAndSeek.WatchRequest watchRequest) throws InvalidTokenIdException {
        var player = playerRepository.getPlayerByToken(watchRequest.getToken());
        if(!player.isPresent())
            throw new InvalidTokenIdException();
        HideAndSeek.GameStatus gameStatus = getGameStatus();
        HideAndSeek.GameResult gameResult = getGameResult();

        return null;
    }
    //TODO return gameResult after game is finished
    private HideAndSeek.GameResult getGameResult(){
        return HideAndSeek.GameResult.UNKNOWN;
    }

    private HideAndSeek.GameStatus getGameStatus(){
        if(playerRepository.getTotalPlayersNumber() == playerRepository.getPlayers().size())
            return HideAndSeek.GameStatus.ONGOING;
        else if(playerRepository.getTotalPlayersNumber() > playerRepository.getPlayers().size())
            return HideAndSeek.GameStatus.PENDING;
        //TODO finish the game
        else
            return HideAndSeek.GameStatus.FINISHED;
    }

    private void addPlayerToDatabase(HideAndSeek.DeclareReadinessRequest request, String token) {
        playerRepository.addPlayerToDataBase(createNewPlayer(request, token));
    }


    private Player createNewPlayer(HideAndSeek.DeclareReadinessRequest request, String token) {
        return Player.builder().playerType(request.getPlayerType()).token(token).team(request.getTeam()).build();
    }

    private void checkIfLobbyIsFull() {
        if (playerRepository.getTotalPlayersNumber() <= playerRepository.getPlayers().size()) {
            throw new DeclareReadinessException("Lobby is full!");
        }
    }

    private void checkThiefAndPoliceNumber(HideAndSeek.DeclareReadinessRequest request) {
        var currentNumber = playerRepository.getPlayers()
                .stream()
                .filter(player -> player.getTeam().equals(request.getTeam()) && player.getPlayerType().equals(request.getPlayerType()))
                .count();
        if (request.getPlayerType() == HideAndSeek.PlayerType.POLICE && currentNumber == playerRepository.getMaximumPoliceNumber()) {
            throw new DeclareReadinessException("Can't add anymore police to the game!");
        } else if (request.getPlayerType().equals(HideAndSeek.PlayerType.THIEF) && currentNumber == playerRepository.getMaximumThiefNumber()) {
            throw new DeclareReadinessException("Can't add anymore thief to the game!");
        }
    }


}
