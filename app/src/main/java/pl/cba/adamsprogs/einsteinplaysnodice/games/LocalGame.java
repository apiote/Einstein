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
    public void swapControls() {
        if (currentPlayer == null || waitingPlayer == null)
            return;
        currentPlayer.setActive(false);
        swapPlayers();
        decideEinStein();
    }

    private void swapPlayers() {
        Player tmp = waitingPlayer;
        waitingPlayer = currentPlayer;
        currentPlayer = tmp;
    }

    private void decideEinStein() {
        calculateEinStein();
        if (einStein) {
            rollAsEinStein();
        } else {
            passControlsToPlayer();
        }
    }

    private void rollAsEinStein() {
        try {
            currentPlayer.triggerRoll();
        } catch (IllegalStateException e){
            exceptionExit(e);
        }
    }

    private void passControlsToPlayer() {
        currentPlayer.setActive(true);
        board.setMovable(false);
    }
}
