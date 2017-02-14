package ml.adamsprogs.einstein.mobile.games;

import ml.adamsprogs.einstein.engine.components.Player;
import ml.adamsprogs.einstein.engine.utils.Point;
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

    @Override
    public void makeSelection(Point result) {

    }

    @Override
    public void voteForMove() {

    }

    @Override
    public void makeMove(Point target) {

    }

    private void swapPlayers() {
        Player tmp = waitingPlayer;
        waitingPlayer = currentPlayer;
        currentPlayer = tmp;
    }
}
