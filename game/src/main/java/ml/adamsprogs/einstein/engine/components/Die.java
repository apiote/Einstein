package ml.adamsprogs.einstein.engine.components;

import ml.adamsprogs.einstein.engine.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static ml.adamsprogs.einstein.engine.utils.Utils.shuffleArray;

abstract public class Die {
    protected final int orientation;
    protected float[][] dieDots;
    protected int value;
    @NotNull
    protected int[] dieOrder = {1, 2, 3, 4, 5, 6};

    protected boolean initialised;
    protected boolean rollable;

    protected OnRollListener onRollListener;
    protected OnErrorListener onErrorListener;

    public Die(int orientation, Player player) {
        this.orientation = orientation;

        setValue(7);
        initialised = false;
        setRollable(false);

        attachInterfaces(player);
    }

    public void setRollable(boolean rollable) {
        this.rollable = rollable;
    }

    private void attachInterfaces(Player player) {
        try {
            onRollListener = player;
            onErrorListener = player;
        } catch (Exception ignored) {
        }
    }


    protected void tryToRoll() {
        try {
            rollDie();
        } catch (IllegalStateException e) {
            onErrorListener.raiseError(e);
        }
    }

    private void rollDie() {
        shuffleArray(dieOrder);
        setValue(dieOrder[dieOrder.length - 1]);
        draw();
        signalRoll();
    }

    public void signalRoll() {
        onRollListener.onRoll(value);
    }

    public void draw() throws IllegalStateException {
        this.draw(value);
    }

    abstract protected void draw(int number);

    protected void tryToInitialise() {
        try {
            initialise();
        } catch (NumberFormatException e) {
            //Log.e("Die", e.getMessage());
            throw new IllegalStateException("Couldn't initialise die due to wrong size");
        }
    }

    abstract protected void initialise() throws NumberFormatException;

    @NotNull
    protected ArrayList<Pair<Float, Float>> calculateDotsFor(int number) {
        ArrayList<Pair<Float, Float>> dots = new ArrayList<>();
        if (number % 2 == 1) {
            dots.add(new Pair<>(dieDots[6][0], dieDots[6][1]));
            --number;
        }
        while (number > 0) {
            dots.add(new Pair<>(dieDots[number - 2][0], dieDots[number - 2][1]));
            dots.add(new Pair<>(dieDots[number - 1][0], dieDots[number - 1][1]));
            number -= 2;
        }
        return dots;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public abstract void stopDieAnimationThread();

    public interface OnRollListener {
        void onRoll(int value);
    }

    public interface OnErrorListener {
        void raiseError(Exception e);
    }
}
