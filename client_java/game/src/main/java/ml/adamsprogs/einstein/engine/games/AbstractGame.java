package ml.adamsprogs.einstein.engine.games;

import ml.adamsprogs.einstein.engine.components.Board;
import ml.adamsprogs.einstein.engine.components.Player;
import ml.adamsprogs.einstein.engine.components.Stone;
import ml.adamsprogs.einstein.engine.utils.Point;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;

import static ml.adamsprogs.einstein.engine.utils.Utils.pointToString;
import static ml.adamsprogs.einstein.engine.utils.Utils.stringToPoint;

public abstract class AbstractGame implements Player.OnRollListener, Board.OnStoneMoved {
    protected boolean einStein = false;

    protected Board board;

    protected Player currentPlayer;
    protected Player waitingPlayer;

    protected OnWinListener onWinListener;
    protected OnErrorExit onErrorExit;

    protected Object context;

    protected AbstractGame(Object context) {
        this.context = context;
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
        try {
            hintAsOnly();
        } catch (IllegalStateException e) {
            tryToHint();
        }
    }

    private void hintAsOnly() {
        String p = getOnlyRolledPoint();
        board.processSelectTouch(stringToPoint(p));
    }

    private void passControlsToBoard() {
        currentPlayer.setActive(false);
        board.setMovable(true);
    }

    private void hintAsEinStein() {
        String p = getEinSteinPoint();
        board.processSelectTouch(stringToPoint(p));
        einStein = false;
    }

    private void tryToHint() throws IllegalStateException {
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

    protected void decideEinStein() {
        calculateEinStein();
        if (einStein) {
            rollAsEinStein();
        } else {
            passControlsToPlayer();
        }
    }

    protected void rollAsEinStein() {
        try {
            currentPlayer.triggerRoll();
        } catch (IllegalStateException e) {
            exceptionExit(e);
        }
    }

    protected void passControlsToPlayer() {
        currentPlayer.setActive(true);
        board.setMovable(false);
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

    protected String getOnlyRolledPoint() throws IllegalStateException {
        ArrayList<Stone> stone = board.findRolledStone(currentPlayer);
        if (stone.isEmpty())
            stone = board.findTwoRolledStones(currentPlayer);
        if (stone.size() == 1) {
            Point p = new Point(stone.get(0).getX(), stone.get(0).getY());
            return pointToString(p);
        }
        throw new IllegalStateException("Not one point");
    }

    public void exceptionExit(Exception e) {
        onErrorExit.onError(e);
    }

    public abstract void makeSelection(Point result);

    public abstract void voteForMove();

    public abstract void makeMove(Point target);

    public interface OnWinListener {
        void onWin(int winner);
    }

    public interface OnErrorExit {
        void onError(Exception e);
    }
}
