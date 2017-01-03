package ml.adamsprogs.einstein.mobile.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.*;

import ml.adamsprogs.einstein.R;
import ml.adamsprogs.einstein.mobile.games.MobileGame;
import ml.adamsprogs.einstein.mobile.utilities.Utilities;

public class MobilePlayer extends ml.adamsprogs.einstein.engine.components.Player {

    private int colour;
    private int textColour;

    @NonNull
    private final Context context;

    public MobilePlayer(@NonNull MobileGame game, int id, @Nullable ImageView dieImage) {
        super(game, id);
        this.context = game.getContext();

        setUpColours();
        createDie(dieImage);
    }

    public MobilePlayer(MobileGame game, int colour) {
        this(game, colour, null);
    }

    private void createDie(@Nullable ImageView dieImage) {
        if (dieImage != null)
            die = new MobileDie(context, orientation, dieImage, this);
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

    public int getColour() {
        return colour;
    }

    public int getTextColour() {
        return textColour;
    }

    public void setDieWidth(int dieWidth) {
        if (die != null) ((MobileDie)die).setWidth(dieWidth);
    }

    public void setDieHeight(int dieHeight) {
        if (die != null) ((MobileDie)die).setHeight(dieHeight);
    }
}
