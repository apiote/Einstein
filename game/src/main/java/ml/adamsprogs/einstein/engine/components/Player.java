package ml.adamsprogs.einstein.engine.components;

import ml.adamsprogs.einstein.engine.games.AbstractGame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract public class Player implements Die.OnRollListener, Die.OnErrorListener {

    public static final int COLOUR_LIGHT = 1;
    public static final int COLOUR_DARK = 0;
    public static final int ORIENTATION_NORTH = 180;
    public static final int ORIENTATION_SOUTH = 0;

    protected int orientation;
    protected final int id;

    @Nullable
    protected Die die;
    private int dieValue;

    @NotNull
    private final AbstractGame game;

    private OnRollListener onRollListener;

    public Player(@NotNull AbstractGame game, int id) {
        this.game = game;
        this.id = id;

        attachInterfaces();
    }

    private void attachInterfaces() {
        try {
            onRollListener = game;
        } catch (Exception ignored) {
        }
    }

    public int getId() {
        return id;
    }

    public void setActive(boolean active) {
        if (die != null) die.setRollable(active);
    }

    public int getOrientation() {
        return orientation;
    }

    public int getDieValue() {
        return dieValue;
    }

    public void drawDie() {
        if (die != null) die.draw();
    }

    public void triggerRoll() {
        if (die != null) die.signalRoll();
    }

    public void stopDieAnimationThread() {
        if (die != null) die.stopDieAnimationThread();
    }

    @Override
    public void onRoll(int value) {
        dieValue = value;
        onRollListener.onRoll();
    }

    @Override
    public void raiseError(Exception e) {
        game.exceptionExit(e);
    }

    public abstract Die getDie();

    public abstract void setDie(Die die);

    public abstract void waitForRoll();

    public interface OnRollListener {
        void onRoll();
    }


}
