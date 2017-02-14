package ml.adamsprogs.einstein.mobile.components;

import android.support.annotation.NonNull;
import ml.adamsprogs.einstein.engine.components.Stone;
import ml.adamsprogs.einstein.engine.utils.Point;
import ml.adamsprogs.einstein.mobile.activities.Einstein;
import ml.adamsprogs.einstein.mobile.games.MobileGame;
import java.util.Map;

import static ml.adamsprogs.einstein.engine.utils.Utils.pointToString;

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

    @Override
    public void makeSelection(Point selection) {
        selectableStones = null;
        hintMove(selection);
        selectedStone = stones.get(pointToString(selection));
    }

    @Override
    public void makeMove(Point target) {
        moveStone(target);
        draw();
        movable = false;
        targetablePoints = null;
        clearHint();
        onStoneMoved.onStoneMoved();
    }

    protected void processMoveTouch(Point target) {
        application.sendMoveVote(target);
    }
}
