package ml.adamsprogs.einstein.mobile.games;

import ml.adamsprogs.einstein.engine.components.Player;
import ml.adamsprogs.einstein.mobile.activities.BoardActivity;
import ml.adamsprogs.einstein.mobile.components.*;

public class MobileLocalGame extends MobileGame {
    public MobileLocalGame(BoardActivity context, int startPlayer) {
        super(context, startPlayer, true);
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
