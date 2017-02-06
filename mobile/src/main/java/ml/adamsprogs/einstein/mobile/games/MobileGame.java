package ml.adamsprogs.einstein.mobile.games;

import android.util.DisplayMetrics;
import android.widget.*;

import ml.adamsprogs.einstein.R;
import ml.adamsprogs.einstein.engine.games.AbstractGame;
import ml.adamsprogs.einstein.mobile.activities.BoardActivity;
import ml.adamsprogs.einstein.mobile.components.*;

import static ml.adamsprogs.einstein.engine.utils.Utils.opponent;

public abstract class MobileGame extends AbstractGame{
    public MobileGame(BoardActivity context, int startPlayer) {
        super(context);

        createPlayers(startPlayer);
        setSizes();
    }

    protected void attachInterfaces() {
        try {
            onWinListener = (OnWinListener) androidContext;
            onErrorExit = (OnErrorExit) androidContext;
        } catch (Exception ignored) {
        }
    }

    protected void createBoard() {
        board = new MobileBoard(this, (ImageView) ((BoardActivity) androidContext).findViewById(R.id.board));
    }

    protected void createPlayers(int startPlayer) {
        int[] dieImages = {R.id.dieLight, R.id.dieDark};

        currentPlayer = new MobilePlayer(this, startPlayer, (ImageView) ((BoardActivity) androidContext).findViewById(dieImages[startPlayer]));
        waitingPlayer = new MobilePlayer(this, opponent(startPlayer), (ImageView) ((BoardActivity) androidContext).findViewById(dieImages[opponent(startPlayer)]));
    }

    protected void setSizes() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((BoardActivity) androidContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        ((MobileBoard)board).setSize(Math.min(width, height >> 1));
        ((MobilePlayer)currentPlayer).setDieHeight(height >> 2);
        ((MobilePlayer)currentPlayer).setDieWidth(width);
        ((MobilePlayer)waitingPlayer).setDieHeight(height >> 2);
        ((MobilePlayer)waitingPlayer).setDieWidth(width);
    }

    @Override
    public void destroy() {
        super.destroy();
        androidContext = null;
    }

    public BoardActivity getContext() {
        return ((BoardActivity) androidContext);
    }
}
