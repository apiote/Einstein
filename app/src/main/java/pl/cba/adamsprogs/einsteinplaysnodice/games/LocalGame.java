package pl.cba.adamsprogs.einsteinplaysnodice.games;

import java.util.Map;

import pl.cba.adamsprogs.einsteinplaysnodice.activities.BoardActivity;
import pl.cba.adamsprogs.einsteinplaysnodice.components.*;

public class LocalGame extends ServerGame {
    public LocalGame(BoardActivity context, int startPlayer) {
        super(context, startPlayer);
    }

    @Override
    public void start() {
        super.start();
        waitingPlayer.drawDie();
    }

    @Override
    public void swapPlayers() {
        if (currentPlayer == null || waitingPlayer == null)
            return;
        currentPlayer.setActive(false);
        Player tmp = waitingPlayer;
        waitingPlayer = currentPlayer;
        currentPlayer = tmp;
        if (isEinStein()) {
            currentPlayer.rollDieAlmost();
        } else {
            currentPlayer.setActive(true);
            board.setMovable(false);
        }
    }

    private boolean isEinStein() {
        int id = currentPlayer.getId();
        int sum = 0;
        for (Map.Entry<?, Stone> stone : board.getStones().entrySet()) {
            if (stone.getValue().getPlayerId() == id)
                ++sum;
        }
        return sum == 1;
    }
}
