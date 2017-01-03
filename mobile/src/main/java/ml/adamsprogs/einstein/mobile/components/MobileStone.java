package ml.adamsprogs.einstein.mobile.components;

import android.content.Context;
import android.graphics.*;
import ml.adamsprogs.einstein.engine.utils.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;

import ml.adamsprogs.einstein.R;

import static ml.adamsprogs.einstein.mobile.utilities.Utilities.*;

public class MobileStone extends ml.adamsprogs.einstein.engine.components.Stone {
    private final Bitmap ambientShadow, directionalShadow;
    private Bitmap bitmap;
    private Canvas canvas;

    private float squareSize;

    private final int textColour;
    private final int colour;

    @NonNull
    private Paint p = new Paint();

    public MobileStone(@NonNull Context context, @NonNull MobilePlayer player, int value, @NonNull Point position) {
        super(player, value, position);
        ambientShadow = ((BitmapDrawable) getDrawable(context, R.drawable.shadow_ambient)).getBitmap();
        directionalShadow = ((BitmapDrawable) getDrawable(context, R.drawable.shadow_directional)).getBitmap();

        this.colour = player.getColour();
        this.textColour = player.getTextColour();

    }

    public void create(float squareSize) {
        this.squareSize = squareSize;
        createBitmap();
        draw(createStone(), createAmbientShadow());
    }

    private void createBitmap() {
        bitmap = Bitmap.createBitmap((int) squareSize, (int) squareSize, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    private Bitmap createStone() {
        Bitmap stoneBitmap = Bitmap.createBitmap((int) squareSize, (int) squareSize, Bitmap.Config.ARGB_8888);
        Canvas stoneCanvas = new Canvas(stoneBitmap);

        drawBlankStone(stoneCanvas);
        printValue(stoneCanvas);

        return rotateBitmap(stoneBitmap);
    }

    private void drawBlankStone(@NonNull Canvas stoneCanvas) {
        p.setColor(colour);
        stoneCanvas.drawCircle(squareSize / 2, squareSize / 2, squareSize / 3, p);
    }

    private void printValue(@NonNull Canvas stoneCanvas) {
        p.setColor(textColour);
        p.setTextSize(squareSize / 2);
        p.setTextAlign(Paint.Align.CENTER);

        Rect bounds = new Rect();
        p.getTextBounds(value, 0, value.length(), bounds);

        stoneCanvas.drawText(value, squareSize / 2, squareSize / 2 + bounds.height() / 2, p);
    }

    private Bitmap rotateBitmap(Bitmap stoneBitmap) {
        Matrix mx = new Matrix();
        mx.postRotate(orientation);
        return Bitmap.createBitmap(stoneBitmap, 0, 0, (int) squareSize, (int) squareSize, mx, false);
    }

    private Bitmap createAmbientShadow() {
        Bitmap ambientShadowBitmap = Bitmap.createBitmap((int) squareSize, (int) squareSize, Bitmap.Config.ARGB_8888);
        Canvas ambientShadowCanvas = new Canvas(ambientShadowBitmap);

        Rect ambientShadowRect = new Rect(0, 0, ambientShadow.getWidth(), ambientShadow.getHeight());
        Rect bitmapRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        ambientShadowCanvas.drawBitmap(ambientShadow, ambientShadowRect, bitmapRect, null);

        return ambientShadowBitmap;
    }

    private void draw(@NonNull Bitmap stone, @NonNull Bitmap ambientShadow) {
        canvas.drawBitmap(ambientShadow, 0, 0, null);
        canvas.drawBitmap(stone, 0, 0, null);
    }

    public Bitmap getBitmap() throws NullPointerException {
        if (bitmap == null) throw new NullPointerException("Bitmap not initialised.");
        else return bitmap;
    }

    public void drawLiftShadow(int borderWidth) {
        Rect shadowRect = new Rect(0, 0, directionalShadow.getWidth(), directionalShadow.getHeight());
        Rect out = new Rect((int) squareSize + borderWidth, (int) squareSize + borderWidth,
                (int) squareSize - borderWidth, (int) squareSize - borderWidth);
        canvas.drawBitmap(directionalShadow, shadowRect, out, null);

    }
}
