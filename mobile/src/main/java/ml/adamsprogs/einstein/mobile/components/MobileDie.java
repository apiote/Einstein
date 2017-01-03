package ml.adamsprogs.einstein.mobile.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.widget.ImageView;
import ml.adamsprogs.einstein.R;
import ml.adamsprogs.einstein.engine.utils.Pair;
import ml.adamsprogs.einstein.mobile.activities.BoardActivity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static ml.adamsprogs.einstein.mobile.utilities.Utilities.getColour;

public class MobileDie extends ml.adamsprogs.einstein.engine.components.Die{
    private ImageView view;

    private Bitmap bitmap;
    private Canvas canvas;

    private float width, height;

    private float dotRadius;

    private int onColour, offColour, dotColour;

    @NonNull
    private final BoardActivity context;

    private Thread dieAnimationThread;

    MobileDie(Context context, int orientation, ImageView view, MobilePlayer player) {
        super(orientation, player);
        this.view = view;
        this.context = (BoardActivity) context;

        attachOnClickListener();
        setUpColours();
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

    @Override
    protected void draw(int number) throws IllegalStateException {
        tryToInitialise();

        drawBackground();
        drawDots(number);

        view.setImageBitmap(flipBitmap());
    }

    @Override
    protected void initialise() throws NumberFormatException {
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

        @NotNull ArrayList<Pair<Float, Float>> dieDotsByNumber = calculateDotsFor(number);
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

    private Bitmap flipBitmap() {
        Matrix mx = new Matrix();
        mx.preScale(1, orientation == 180 ? -1 : 1);

        return Bitmap.createBitmap(bitmap, 0, 0, (int) width, (int) height, mx, false);
    }

    private void setUpColours() {
        offColour = getColour(context, R.color.dice_off);
        onColour = getColour(context, R.color.dice_on);
        dotColour = getColour(context, R.color.dice_num);
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

    @Override
    public void setRollable(boolean rollable) {
        super.setRollable(rollable);
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

    @Override
    public void stopDieAnimationThread() {
        if (dieAnimationThread != null && dieAnimationThread.isAlive())
            dieAnimationThread.interrupt();
    }
}
