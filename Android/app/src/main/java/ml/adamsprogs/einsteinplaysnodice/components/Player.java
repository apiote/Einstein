package ml.adamsprogs.einsteinplaysnodice.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.*;

import ml.adamsprogs.einsteinplaysnodice.R;
import ml.adamsprogs.einsteinplaysnodice.games.ServerGame;
import ml.adamsprogs.einsteinplaysnodice.utilities.Utilities;

public class Player implements Die.OnRollListener, Die.OnErrorListener {
    public static final int COLOUR_LIGHT = 1;
    public static final int COLOUR_DARK = 0;
    private static final int ORIENTATION_NORTH = 180;
    private static final int ORIENTATION_SOUTH = 0;

    private int orientation;
    private final int id;

    private int colour;
    private int textColour;

    @Nullable
    private Die die;
    private int dieValue;

    @NonNull
    private final ServerGame game;

    @NonNull
    private final Context context;
    private OnRollListener onRollListener;

    public Player(@NonNull ServerGame game, int id, @Nullable ImageView dieImage) {
        this.game = game;
        this.context = game.getContext();
        this.id = id;

        setUpColours();
        attachInterfaces();
        createDie(dieImage);
    }

    private void createDie(@Nullable ImageView dieImage) {
        if (dieImage != null)
            die = new Die(context, orientation, dieImage, this);
    }

    private void setUpColours() {
        if (id == COLOUR_LIGHT) {
            orientation = ORIENTATION_NORTH;
            this.colour = Utilities.getColour(context, R.color.light);
            textColour = Utilities.getColour(context, R.color.text);
        } else {
            orientation = ORIENTATION_SOUTH;
            this.colour = Utilities.getColour(context, R.color.dark);
            textColour = Utilities.getColour(context, R.color.text_light);
        }
    }

    private void attachInterfaces() {
        try {
            onRollListener = game;
        } catch (Exception ignored) {
        }
    }

    public Player(@NonNull ServerGame game, int id) {
        this(game, id, null);
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

    public int getColour() {
        return colour;
    }

    public int getTextColour() {
        return textColour;
    }

    public int getDieValue() {
        return dieValue;
    }

    public void setDieWidth(int dieWidth) {
        if (die != null) die.setWidth(dieWidth);
    }

    public void setDieHeight(int dieHeight) {
        if (die != null) die.setHeight(dieHeight);
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

    public interface OnRollListener {
        void onRoll();
    }
}
