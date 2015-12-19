package pl.cba.adamsprogs.einsteinplaysnodice.components;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;

import pl.cba.adamsprogs.einsteinplaysnodice.R;

import static pl.cba.adamsprogs.einsteinplaysnodice.utilities.Utilities.*;

public class Stone implements Comparable<Stone> {
    private String value;
    private int textColour;
    private Bitmap ambientShadow, directionalShadow;
    private int x, y;
    private int colour;
    private Bitmap bitmap;
    private boolean selectable;
    private int orientation;
    private float squareSize;

    public Stone(Context context, Player player, int value, Point position) {
        ambientShadow = ((BitmapDrawable) getDrawable(context, R.drawable.shadow_ambient)).getBitmap();
        directionalShadow = ((BitmapDrawable) getDrawable(context, R.drawable.shadow_directional)).getBitmap();

        this.orientation = player.getOrientation();
        this.colour = player.getColour();
        this.textColour = player.getTextColour();
        this.value = value + "";
        x = position.x;
        y = position.y;
    }

    public void create(float squareSize) {
        bitmap = Bitmap.createBitmap((int) squareSize, (int) squareSize, Bitmap.Config.ARGB_8888);
        this.squareSize = squareSize;
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();
        Rect bounds = new Rect();

        p.setColor(colour);
        canvas.drawCircle(squareSize / 2, squareSize / 2, squareSize / 3, p);

        p.setColor(textColour);
        p.setTextSize(squareSize / 2);
        p.setTextAlign(Paint.Align.CENTER);
        p.getTextBounds(value, 0, value.length(), bounds);
        canvas.drawText(value, squareSize / 2, squareSize / 2 + bounds.height() / 2, p);

        Matrix mx = new Matrix();
        mx.postRotate(orientation);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, (int) squareSize, (int) squareSize, mx, false);

        Bitmap sND = Bitmap.createBitmap((int) squareSize, (int) squareSize, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(sND);
        Rect ambientShadowRect = new Rect(0, 0, ambientShadow.getWidth(), ambientShadow.getHeight());
        Rect bitmapRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawBitmap(ambientShadow, ambientShadowRect, bitmapRect, null);
        canvas.drawBitmap(bitmap, 0, 0, null);

        bitmap = sND;
    }

    public void moveTo(int x, int y) {
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

    public String getValue() {
        return value;
    }

    public int getIntValue() {
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

    @Override
    public String toString() {
        return "Stone: " + value + "@" + x + ", " + y + "/player" + getPlayerId();
    }

    public void moveTo(Point target) {
        moveTo(target.x, target.y);
    }

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

    public boolean hasRolledValue(Player player) {
        return this.getPlayerId() == player.getId() && this.value.equals("" + player.getDieValue());
    }

    public boolean isRolledValueNextBigger(Player player) {
        return getIntValue() > player.getDieValue() && getPlayerId() == player.getId();
    }

    public boolean isPlayerBigger(Player player) {
        return getPlayerId() > player.getId();
    }

    public void drawDirectionalShadow(Canvas canvas, int gridWidth) {
        Rect shadowRect = new Rect(0, 0, directionalShadow.getWidth(), directionalShadow.getHeight());
        Rect out = new Rect(x * (int) squareSize + gridWidth, y * (int) squareSize + gridWidth,
                (x + 1) * (int) squareSize - gridWidth, (y + 1) * (int) squareSize - gridWidth);
        canvas.drawBitmap(directionalShadow, shadowRect, out, null);

    }
}
