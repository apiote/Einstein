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
    private boolean selected;
    private int orientation;

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

    public Bitmap create(float squareSize) { //FIXME draw shadow not turned
        bitmap = Bitmap.createBitmap((int) squareSize, (int) squareSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();
        Rect bounds = new Rect();

        Rect ambientShadowRect = new Rect(0, 0, ambientShadow.getWidth(), ambientShadow.getHeight());
        Rect bitmapRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawBitmap(ambientShadow, ambientShadowRect, bitmapRect, null);

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

        return bitmap;
    }

    public void moveTo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        selectable = false;
    }

    public void setSelected() {
        setSelected(true);
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
        selected = false;
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

    public Bitmap getDirectionalShadow() {
        return directionalShadow;
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
        if(playerDifference!=0)
            return playerDifference;
        return this.getIntValue()-another.getIntValue();
    }

    @Override
    public String toString() {
        return "Stone: "+value+"/player"+getPlayerId();
    }
}
