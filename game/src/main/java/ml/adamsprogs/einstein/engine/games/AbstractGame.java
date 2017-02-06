package ml.adamsprogs.einstein.engine.games;

import ml.adamsprogs.einstein.engine.components.Board;
import ml.adamsprogs.einstein.engine.components.Player;
import ml.adamsprogs.einstein.engine.components.Stone;
import ml.adamsprogs.einstein.engine.utils.Point;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.NoSuchElementException;

import static ml.adamsprogs.einstein.engine.utils.Utils.pointToString;

public abstract class AbstractGame implements Player.OnRollListener, Board.OnStoneMoved {
    protected boolean einStein = false;

    protected Board board;

    protected Player currentPlayer;
    protected Player waitingPlayer;

    protected OnWinListener onWinListener;
    protected OnErrorExit onErrorExit;

    protected Object androidContext;

    protected AbstractGame(Object androidContext) {
        this.androidContext = androidContext;
        createBoard();
        attachInterfaces();
    }

    protected abstract void attachInterfaces();

    protected abstract void createBoard();



    public void start() {
        currentPlayer.drawDie();

        try {
            board.initialise();
            board.draw();
        } catch (IllegalStateException e) {
            //Toast.makeText(context, context.getString(R.string.dieDrawError), Toast.LENGTH_SHORT).show();
        }

        currentPlayer.setActive(true);
    }

    @Override
    public void onRoll() {
        passControlsToBoard();
        if (einStein) {
            hintAsEinStein();
        }
        tryToHint();
    }

    private void passControlsToBoard() {
        currentPlayer.setActive(false);
        board.setMovable(true);
    }

    private void hintAsEinStein() {
        String p = getEinSteinPoint();
        board.processSelectTouch(pointToString(p));
        einStein = false;
    }

    private void tryToHint() throws IllegalStateException {
        System.out.println("tryingtohint");
        try {
            board.hint(currentPlayer);
        } catch (NoSuchElementException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public void onStoneMoved() {
        CheckWin();
        swapControls();
    }

    public void CheckWin() {
        if (board.isCurrentPlayerWinner(currentPlayer.getId()))
            endingDialogue(currentPlayer.getId());
    }

    private void endingDialogue(int winner) {
        board.setMovable(false);
        currentPlayer.setActive(false);
        waitingPlayer.setActive(false);
        onWinListener.onWin(winner);
    }

    public abstract void swapControls();

    public void destroy() {
        currentPlayer.stopDieAnimationThread();
        waitingPlayer.stopDieAnimationThread();
        board = null;
        currentPlayer = null;
        waitingPlayer = null;
    }

    protected void calculateEinStein() {
        int id = currentPlayer.getId();
        int sum = 0;
        for (Map.Entry<?, Stone> stone : board.getStones().entrySet()) {
            if (stone.getValue().getPlayerId() == id)
                ++sum;
        }
        einStein = (sum == 1);
    }

    @NotNull
    private String getEinSteinPoint() throws IllegalStateException {
        int id = currentPlayer.getId();
        for (Map.Entry<?, Stone> stone : board.getStones().entrySet()) {
            if (stone.getValue().getPlayerId() == id) {
                return (String) stone.getKey();
            }
        }
        throw new IllegalStateException("EinStein point not found");
    }

    public void exceptionExit(Exception e) {
        onErrorExit.onError(e);
    }

    public interface OnWinListener {
        void onWin(int winner);
    }

    public interface OnErrorExit {
        void onError(Exception e);
    }
}
