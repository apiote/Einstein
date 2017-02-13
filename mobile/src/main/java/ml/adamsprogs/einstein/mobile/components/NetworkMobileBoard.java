package ml.adamsprogs.einstein.mobile.components;

import android.support.annotation.NonNull;
import ml.adamsprogs.einstein.engine.components.Stone;
import ml.adamsprogs.einstein.engine.utils.Point;
import ml.adamsprogs.einstein.mobile.activities.Einstein;
import ml.adamsprogs.einstein.mobile.games.MobileGame;
import java.util.Map;

public class NetworkMobileBoard extends MobileBoard{
    private Einstein application;
    public NetworkMobileBoard(MobileBoard board, Einstein application) {
        super((MobileGame) board.game, board.view);
        this.application = application;
    }

    @Override
    public void processSelectTouch(@NonNull Point touchedPoint) {
        for(Map.Entry<String, Stone> stone: stones.entrySet()) {
            stone.getValue().setSelectable(false);
        }
        application.sendSelectVote(touchedPoint);
    }

    protected void processMoveTouch(Point target) {
        application.sendMoveVote(target);
    }
}
