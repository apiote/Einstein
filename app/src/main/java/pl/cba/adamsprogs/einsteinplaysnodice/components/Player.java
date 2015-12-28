package pl.cba.adamsprogs.einsteinplaysnodice.components;

import android.content.Context;
import android.widget.*;

import pl.cba.adamsprogs.einsteinplaysnodice.R;
import pl.cba.adamsprogs.einsteinplaysnodice.games.ServerGame;
import pl.cba.adamsprogs.einsteinplaysnodice.utilities.Utilities;

public class Player implements Die.OnRollListener, Die.OnErrorListener {
    public static final int COLOUR_LIGHT = 1;
    public static final int COLOUR_DARK = 0;
    public static final int ORIENTATION_NORTH = 180;
    public static final int ORIENTATION_SOUTH = 0;
    private int orientation;
    private int colour;
    private int textColour;
    private Die die;
    private int dieValue;
    private Context context;
    private OnRollListener onRollListener;
    private int id;
    private ServerGame game;

    public Player(ServerGame serverGame, int colour, ImageView dieImage) {
        game = serverGame;
        this.context = serverGame.getContext();
        if (colour == COLOUR_LIGHT) {
            orientation = ORIENTATION_NORTH;
            this.colour = Utilities.getColour(context, R.color.light);
            textColour = Utilities.getColour(context, R.color.text);
        } else {
            orientation = ORIENTATION_SOUTH;
            this.colour = Utilities.getColour(context, R.color.dark);
            textColour = Utilities.getColour(context, R.color.text_light);
        }

        id = colour;

        try {
            onRollListener = serverGame;
        } catch (Exception ignored) {
        }

        if (dieImage != null) die = new Die(context, orientation, dieImage, this);
    }

    public Player(ServerGame serverGame, int colourLight) {
        this(serverGame, colourLight, null);
    }

    public int getId() {
        return id;
    }

    public void setActive(boolean active) {
        die.setRollable(active);
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
        die.setWidth(dieWidth);
    }

    public void setDieHeight(int dieHeight) {
        die.setHeight(dieHeight);
    }

    public void drawDie() {
        try {
            die.draw();
        } catch (IllegalStateException e) {
            Toast.makeText(context, "Couldn't draw dice", Toast.LENGTH_SHORT).show();
        }
    }

    public void triggerRoll() {
        die.signalRoll();
    }

    public void stopDieAnimationThread() {
        die.stopDieAnimationThread();
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
