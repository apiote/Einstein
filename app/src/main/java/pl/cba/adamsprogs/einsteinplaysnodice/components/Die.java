package pl.cba.adamsprogs.einsteinplaysnodice.components;

import android.content.Context;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.ImageView;

import java.util.ArrayList;

import pl.cba.adamsprogs.einsteinplaysnodice.R;

import static pl.cba.adamsprogs.einsteinplaysnodice.utilities.Utilities.*;

public class Die {
    private ImageView view;

    private int orientation;
    private float width, height;
    private int value;

    private int[] dieOrder = {1, 2, 3, 4, 5, 6};

    private float spotRadius;
    private float[][] dieSpots;

    private Bitmap bitmap;
    private Canvas canvas;

    private boolean initialised;
    private boolean rollable;

    private final int onColour, offColour, spotColour;

    private OnRollListener onRollListener;

    public Die(Context context, int orientation, ImageView view, Player player) {
        this.view = view;
        this.orientation = orientation;

        value = 7;
        initialised = false;
        setRollable(false);

        this.view.setOnTouchListener(
                new ImageView.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent m) {
                        diePressed(m);
                        return true;
                    }
                }
        );
        offColour = getColour(context, R.color.dice_off);
        onColour = getColour(context, R.color.dice_on);
        spotColour = getColour(context, R.color.dice_num);

        try {
            onRollListener = player;
        } catch (Exception ignored) {
        }
    }

    public void diePressed(MotionEvent m) {
        int action = m.getActionMasked();
        if (action == MotionEvent.ACTION_UP && isRollable()) {
            setRollable(false);
            rollDie();
        }
    }

    public void rollDie() {
        shuffleArray(dieOrder);
        setValue(dieOrder[dieOrder.length - 1]);

        draw();

        onRollListener.onRoll(value);
    }

    public void draw() {
        this.draw(value);
    }

    private void draw(int number) throws IllegalStateException {
        try {
            initialise();
        } catch (NumberFormatException e) {
            Log.e("Die", e.getMessage());
            throw new IllegalStateException("Couldn't initialise die due to wrong size");
        }

        Paint p;
        p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);

        canvas.drawColor(rollable ? onColour : offColour);

        p.setColor(spotColour);

        ArrayList<Pair<Float, Float>> dieSpotsByNumber = calculateSpotsFor(number);

        for (Pair<Float, Float> x : dieSpotsByNumber)
            canvas.drawCircle(x.first, x.second, spotRadius, p);

        Matrix mx = new Matrix();
        mx.postRotate(orientation);

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mx, false);
        view.setImageBitmap(rotatedBitmap);
    }

    private void initialise() throws NumberFormatException {
        if (isInitialised())
            return;
        if (!isProperSize())
            throw new NumberFormatException("Width and height must be greater than zero");

        float off = (width - height) / 2;
        float dieD = height / 5;
        dieSpots = new float[][]{{off + dieD, dieD}, {off + (4 * dieD), 4 * dieD},
                {off + (4 * dieD), dieD}, {off + dieD, 4 * dieD},
                {off + (2 * dieD) + spotRadius, dieD}, {off + (2 * dieD) + spotRadius, 4 * dieD},
                {off + (2 * dieD) + spotRadius, (2 * dieD) + spotRadius}
        };

        bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        initialised = true;
    }

    private ArrayList<Pair<Float, Float>> calculateSpotsFor(int number) {
        ArrayList<Pair<Float, Float>> spots = new ArrayList<>();
        if (number % 2 == 1) {
            spots.add(new Pair<>(dieSpots[6][0], dieSpots[6][1]));
            --number;
        }
        while (number > 0) {
            spots.add(new Pair<>(dieSpots[number - 2][0], dieSpots[number - 2][1]));
            spots.add(new Pair<>(dieSpots[number - 1][0], dieSpots[number - 1][1]));
            number -= 2;
        }
        return spots;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
        this.spotRadius = height / 10;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    private boolean isProperSize() {
        return width + height > 0;
    }

    private boolean isInitialised() {
        return initialised;
    }

    public boolean isRollable() {
        return rollable;
    }

    public void setRollable(boolean rollable) {
        this.rollable = rollable;
        if (isProperSize()) draw();
        //TODO animate
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public interface OnRollListener {
        void onRoll(int value);
    }
}
