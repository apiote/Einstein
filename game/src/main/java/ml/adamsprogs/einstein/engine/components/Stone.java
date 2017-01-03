package ml.adamsprogs.einstein.engine.components;

import ml.adamsprogs.einstein.engine.utils.Point;
import org.jetbrains.annotations.NotNull;

abstract public class Stone implements Comparable<Stone>{

    protected int x, y;
    @NotNull
    protected final String value;
    protected final int orientation;
    protected boolean selectable;

    public Stone(@NotNull Player player, int value, @NotNull Point position) {

        this.orientation = player.getOrientation();
        this.value = value + "";
        x = position.x;
        y = position.y;
    }

    public abstract void create(float squareSize);

    public abstract void drawLiftShadow(int borderWidth);

    protected void moveTo(int x, int y) {
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

    public int getOrientation() {
        return orientation;
    }

    public int getPlayerId() {
        return orientation / 180;
    }

    @Override
    public int compareTo(@NotNull Stone another) {
        int playerDifference = this.getPlayerId() - another.getPlayerId();
        if (playerDifference != 0)
            return playerDifference;
        return this.getIntValue() - another.getIntValue();
    }

    @NotNull
    @Override
    public String toString() {
        return "Stone: " + value + "@" + x + ", " + y + "/player" + getPlayerId();
    }

    public void moveTo(@NotNull Point target) {
        moveTo(target.x, target.y);
    }

    @NotNull
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

    public boolean hasRolledValue(@NotNull Player player) {
        return this.getPlayerId() == player.getId() && this.value.equals("" + player.getDieValue());
    }

    public boolean isRolledValueNextBigger(@NotNull Player player) {
        return getIntValue() > player.getDieValue() && getPlayerId() == player.getId();
    }

    public boolean isPlayerBigger(@NotNull Player player) {
        return getPlayerId() > player.getId();
    }

    public abstract Object getBitmap();
}
