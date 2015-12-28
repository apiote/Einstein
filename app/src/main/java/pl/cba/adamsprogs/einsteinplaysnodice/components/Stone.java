package pl.cba.adamsprogs.einsteinplaysnodice.components;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;

import pl.cba.adamsprogs.einsteinplaysnodice.R;

import static pl.cba.adamsprogs.einsteinplaysnodice.utilities.Utilities.*;

public class Stone implements Comparable<Stone> {
    private final Bitmap ambientShadow, directionalShadow;
    private Bitmap bitmap;
    private Canvas canvas;

    private int x, y;
    @NonNull
    private final String value;
    private final int orientation;
    private float squareSize;

    private final int textColour;
    private final int colour;

    @NonNull
    private Paint p = new Paint();

    private boolean selectable;

    public Stone(@NonNull Context context, @NonNull Player player, int value, @NonNull Point position) {
        ambientShadow = ((BitmapDrawable) getDrawable(context, R.drawable.shadow_ambient)).getBitmap();
        directionalShadow = ((BitmapDrawable) getDrawable(context, R.drawable.shadow_directional)).getBitmap();

        this.orientation = player.getOrientation();
        this.colour = player.getColour();
        this.textColour = player.getTextColour();
        this.value = value + "";
        x = position.x;
        y = position.y;

        createBitmap();
    }


    private void createBitmap() {
        bitmap = Bitmap.createBitmap((int) squareSize, (int) squareSize, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    public void create(float squareSize) {
        this.squareSize = squareSize;
        draw(createStone(), createAmbientShadow());
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

    private void moveTo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    private int getIntValue() {
        return Integer.parseInt(value);
    }

    public Bitmap getBitmap() throws NullPointerException {
        if (bitmap == null) throw new NullPointerException("Bitmap not initialised.");
        else return bitmap;
    }

    public int getOrientation() {
        return orientation;
    }

    public int getPlayerId() {
        return orientation / 180;
    }

    @Override
    public int compareTo(@NonNull Stone another) {
        int playerDifference = this.getPlayerId() - another.getPlayerId();
        if (playerDifference != 0)
            return playerDifference;
        return this.getIntValue() - another.getIntValue();
    }

    @NonNull
    @Override
    public String toString() {
        return "Stone: " + value + "@" + x + ", " + y + "/player" + getPlayerId();
    }

    public void moveTo(@NonNull Point target) {
        moveTo(target.x, target.y);
    }

    @NonNull
    public Point getPosition() {
        return new Point(x, y);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Stone))
            return false;
        Stone another = (Stone) object;
        return this.orientation == another.orientation && this.x == another.x && this.y == another.y;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public boolean hasRolledValue(@NonNull Player player) {
        return this.getPlayerId() == player.getId() && this.value.equals("" + player.getDieValue());
    }

    public boolean isRolledValueNextBigger(@NonNull Player player) {
        return getIntValue() > player.getDieValue() && getPlayerId() == player.getId();
    }

    public boolean isPlayerBigger(@NonNull Player player) {
        return getPlayerId() > player.getId();
    }

    public void drawDirectionalShadow(@NonNull Canvas canvas, int gridWidth) {
        Rect shadowRect = new Rect(0, 0, directionalShadow.getWidth(), directionalShadow.getHeight());
        Rect out = new Rect(x * (int) squareSize + gridWidth, y * (int) squareSize + gridWidth,
                (x + 1) * (int) squareSize - gridWidth, (y + 1) * (int) squareSize - gridWidth);
        canvas.drawBitmap(directionalShadow, shadowRect, out, null);

    }
}
