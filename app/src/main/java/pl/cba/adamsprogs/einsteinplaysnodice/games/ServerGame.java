package pl.cba.adamsprogs.einsteinplaysnodice.games;

import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.*;

import pl.cba.adamsprogs.einsteinplaysnodice.R;
import pl.cba.adamsprogs.einsteinplaysnodice.activities.BoardActivity;
import pl.cba.adamsprogs.einsteinplaysnodice.components.*;

public abstract class ServerGame implements Player.OnRollListener, Board.OnStoneSelected, Board.OnStoneMoved {
    protected Board board;
    protected Player currentPlayer;
    protected Player waitingPlayer;
    private BoardActivity context;
    private OnWinListener onWinListener;

    public ServerGame(BoardActivity context, int startPlayer) {
        this.context = context;
        int[] dieImages = {R.id.dieLight, R.id.dieDark};

        board = new Board(this, (ImageView) context.findViewById(R.id.board));

        currentPlayer = new Player(this, startPlayer, (ImageView) context.findViewById(dieImages[startPlayer]));
        waitingPlayer = new Player(this, 1 - startPlayer, (ImageView) context.findViewById(dieImages[1-startPlayer]));

        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        board.setSize(Math.min(width, height >> 1));
        currentPlayer.setDieHeight(height >> 2);
        currentPlayer.setDieWidth(width);
        waitingPlayer.setDieHeight(height >> 2);
        waitingPlayer.setDieWidth(width);

        try {
            onWinListener = context;
        } catch (Exception ignored) {
        }
    }

    public void start() {
        currentPlayer.drawDie();
        //waitingPlayer.drawDie();

        try {
            board.draw();
        } catch (IllegalStateException e) {
            Toast.makeText(context, "Couldn't draw the board", Toast.LENGTH_SHORT).show();
        }

        currentPlayer.setActive(true);
    }

    @Override
    public void onRoll() {
        currentPlayer.setActive(false);
        board.setMovable(true);
        board.hint(currentPlayer);
    }

    @Override
    public void onStoneSelected(Point point) {
        board.hintMove(point);
    }

    @Override
    public void onStoneMoved() {
        CheckWin();
        swapPlayers();
        currentPlayer.setActive(true);
        board.setMovable(false);
    }

    public BoardActivity getContext() {
        return context;
    }

    public void CheckWin() {
        if (board.isCurrentPlayerWinner(currentPlayer.getId()))
            endingDialogue(currentPlayer.getId());
    }

    private void endingDialogue(int winner) {
        board.setMovable(false);
        onWinListener.onWin(winner);
    }

    public abstract void swapPlayers();

    public void destroy() {
        board = null;
        currentPlayer=null;
        waitingPlayer=null;
        context=null;
    }

    public interface OnWinListener {
        void onWin(int winner);
    }
}
