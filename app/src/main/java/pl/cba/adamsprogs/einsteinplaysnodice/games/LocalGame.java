package pl.cba.adamsprogs.einsteinplaysnodice.games;

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
            einStein = true;
            try {
                currentPlayer.triggerRoll();
            } catch (IllegalStateException e){
                exceptionExit(e);
            }
        } else {
            currentPlayer.setActive(true);
            board.setMovable(false);
        }
    }
}
