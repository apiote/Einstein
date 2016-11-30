package ml.adamsprogs.einsteinplaysnodice.components;

import android.content.Context;
import android.graphics.*;
import android.support.annotation.NonNull;
import android.util.*;
import android.view.*;
import android.widget.ImageView;

import java.util.ArrayList;

import ml.adamsprogs.einsteinplaysnodice.R;
import ml.adamsprogs.einsteinplaysnodice.activities.BoardActivity;

import static ml.adamsprogs.einsteinplaysnodice.utilities.Utilities.*;

public class Die {
    private ImageView view;

    private Bitmap bitmap;
    private Canvas canvas;

    private final int orientation;
    private float width, height;
    private int value;

    @NonNull
    private int[] dieOrder = {1, 2, 3, 4, 5, 6};

    private float dotRadius;
    private float[][] dieDots;

    private boolean initialised;
    private boolean rollable;

    private int onColour, offColour, dotColour;

    private OnRollListener onRollListener;
    private OnErrorListener onErrorListener;
    @NonNull
    private final BoardActivity context;

    private Thread dieAnimationThread;

    public Die(Context context, int orientation, ImageView view, Player player) {
        this.view = view;
        this.orientation = orientation;
        this.context = (BoardActivity) context;

        setValue(7);
        initialised = false;
        setRollable(false);

        attachOnClickListener();
        setUpColours();
        attachInterfaces(player);
    }

    private void attachOnClickListener() {
        this.view.setOnTouchListener(
                (v, m) -> {
                    diePressed(m);
                    return true;
                }
        );
    }

    private void diePressed(@NonNull MotionEvent m) {
        if (m.getActionMasked() == MotionEvent.ACTION_UP && rollable) {
            setRollable(false);
            tryToRoll();
        }
    }

    private void tryToRoll() {
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

    public void draw() {
        this.draw(value);
    }

    private void draw(int number) throws IllegalStateException {
        tryToInitialise();

        drawBackground();
        drawDots(number);

        view.setImageBitmap(flipBitmap());
    }

    private void tryToInitialise() {
        try {
            initialise();
        } catch (NumberFormatException e) {
            Log.e("Die", e.getMessage());
            throw new IllegalStateException("Couldn't initialise die due to wrong size");
        }
    }

    private void initialise() throws NumberFormatException {
        if (isInitialised())
            return;
        if (!isProperSize())
            throw new NumberFormatException("Width and height must be greater than zero");

        createDots();

        bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        initialised = true;
    }

    private void createDots() {
        float offset = (width - height) / 2;
        float dieDiameter = height / 5;
        dieDots = new float[][]{{offset + dieDiameter, dieDiameter}, {offset + (4 * dieDiameter), 4 * dieDiameter},
                {offset + (4 * dieDiameter), dieDiameter}, {offset + dieDiameter, 4 * dieDiameter},
                {offset + (2 * dieDiameter) + dotRadius, dieDiameter}, {offset + (2 * dieDiameter) + dotRadius, 4 * dieDiameter},
                {offset + (2 * dieDiameter) + dotRadius, (2 * dieDiameter) + dotRadius}
        };
    }

    private void drawBackground() {
        int backgroundColour = rollable ? onColour : offColour;
        canvas.drawColor(backgroundColour);
    }

    private void drawDots(int number) {
        Paint p = setUpPaint();

        ArrayList<Pair<Float, Float>> dieDotsByNumber = calculateDotsFor(number);
        for (Pair<Float, Float> x : dieDotsByNumber)
            canvas.drawCircle(x.first, x.second, dotRadius, p);
    }

    @NonNull
    private Paint setUpPaint() {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(dotColour);
        return p;
    }

    @NonNull
    private ArrayList<Pair<Float, Float>> calculateDotsFor(int number) {
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

    private Bitmap flipBitmap() {
        Matrix mx = new Matrix();
        mx.preScale(1, orientation == 180 ? -1 : 1);

        return Bitmap.createBitmap(bitmap, 0, 0, (int) width, (int) height, mx, false);
    }

    public void signalRoll() {
        onRollListener.onRoll(value);
    }

    private void setUpColours() {
        offColour = getColour(context, R.color.dice_off);
        onColour = getColour(context, R.color.dice_on);
        dotColour = getColour(context, R.color.dice_num);
    }

    private void attachInterfaces(Player player) {
        try {
            onRollListener = player;
            onErrorListener = player;
        } catch (Exception ignored) {
        }
    }

    public void setHeight(float height) {
        this.height = height;
        this.dotRadius = height / 10;
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

    public void setRollable(boolean rollable) {
        this.rollable = rollable;
        if (rollable) {
            createDieAnimationThread();
            dieAnimationThread.start();
        } else {
            if (dieAnimationThread != null) dieAnimationThread.interrupt();
            if (isProperSize()) draw();
        }
    }

    private void createDieAnimationThread() {
        dieAnimationThread = new Thread(() -> {
            int i = 0;
            while (true) {
                try {
                    drawDieAnimationFrame(i);
                } catch (InterruptedException e) {
                    break;
                }
                i = (++i) % 6;
            }
        });
    }

    private void drawDieAnimationFrame(int i) throws InterruptedException {
        if (Thread.currentThread().isInterrupted())
            throw new InterruptedException();
        final int v = dieOrder[i];
        context.runOnUiThread(() -> draw(v));
        Thread.sleep(250, 0);
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void stopDieAnimationThread() {
        if (dieAnimationThread != null && dieAnimationThread.isAlive())
            dieAnimationThread.interrupt();
    }

    public interface OnRollListener {
        void onRoll(int value);
    }

    public interface OnErrorListener {
        void raiseError(Exception e);
    }
}
