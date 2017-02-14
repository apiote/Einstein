package ml.adamsprogs.einstein.engine.components;

import ml.adamsprogs.einstein.engine.games.AbstractGame;
import ml.adamsprogs.einstein.engine.utils.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static ml.adamsprogs.einstein.engine.utils.Utils.opponent;
import static ml.adamsprogs.einstein.engine.utils.Utils.pointToString;

public abstract class Board {
    protected OnStoneMoved onStoneMoved;

    @NotNull
    public HashMap<String, Stone> stones = new HashMap<>();
    public ArrayList<Stone> selectableStones;
    public Stone selectedStone;
    protected Point[] targetablePoints;

    protected boolean movable;
    protected boolean initialised;

    public AbstractGame game;

    protected boolean isMovable() {
        return movable;
    }

    protected boolean isValidMoveTouch(Point touchedPoint) {
        return isPointTargetable(touchedPoint) && selectedStone != null;
    }

    protected boolean isPointTargetable(Point touchedPoint) {
        System.out.println("targetableChecking");
        if (targetablePoints == null)
            return false;
        System.out.println("not null");
        System.out.println("touched: "+ pointToString(touchedPoint));
        for (Point p : targetablePoints) {
            System.out.println("checking with "+ pointToString(p));
            if (pointToString(p).equals(pointToString(touchedPoint)))
                return true;
        }
        System.out.println("none");
        return false;
    }

    protected void processMoveTouch(Point target) {
        moveStone(target);
        draw();
        clearTouchability();
        onStoneMoved.onStoneMoved();
    }

    public abstract void draw();

    private void clearTouchability() {
        selectedStone.setSelectable(false);
        movable = false;
        targetablePoints = null;
        clearHint();
        selectableStones = null;
    }

    protected void clearHint() {
        for (Map.Entry<?, Stone> stone : stones.entrySet())
            stone.getValue().setSelectable(false);
    }

    protected void moveStone(Point target) {
        stones.remove(pointToString(selectedStone.getPosition()));
        selectedStone.moveTo(target);
        stones.remove(pointToString(target));
        stones.put(pointToString(target), selectedStone);
    }

    protected boolean isValidSelectTouch(Point touchedPoint) {
        Stone tempSelectedStone = stones.get(pointToString(touchedPoint));
        System.out.println("Touched " + touchedPoint.x + "x" + touchedPoint.y);
        for (Map.Entry<String, Stone> e : stones.entrySet()) {
            System.out.println("stone at " + e.getKey());
        }
        return (tempSelectedStone != null && tempSelectedStone.isSelectable());
    }

    public void processSelectTouch(@NotNull Point touchedPoint) {
        selectedStone = stones.get(pointToString(touchedPoint));
        selectedStone.setSelectable(false);
        hintMove(touchedPoint);
    }

    public void hintMove(@NotNull Point point) {
        System.out.println("Hinting move");
        createTargetablePoints(point);
        draw();
        drawSelectableStones();
        Stone stone = stones.get(pointToString(point));

        drawLiftedStone(stone);

        for (Point targetPoint : targetablePoints) {
            drawMoveHintHighlight(targetPoint.x, targetPoint.y);
            stone = stones.get(pointToString(targetPoint));
            if (stone != null) {
                drawStone(stone);
            }
        }
    }

    private void createTargetablePoints(@NotNull Point touchedPoint) {
        int targetDirection = selectedStone.getPlayerId() == 0 ? -1 : 1;
        targetablePoints = new Point[]{new Point(touchedPoint.x + targetDirection, touchedPoint.y),
                new Point(touchedPoint.x, touchedPoint.y + targetDirection),
                new Point(touchedPoint.x + targetDirection, touchedPoint.y + targetDirection)};
    }

    protected void drawSelectableStones() {
        System.out.println("Drawing selectable Stones");
        if (selectableStones == null)
            return;
        System.out.println("They’re not null");
        for (Stone s : selectableStones) {
            drawSelectableStone(s);
            s.setSelectable(true);
        }
    }

    protected abstract void drawLiftedStone(@NotNull Stone stone);

    protected void setUpInterfaces() {
        try {
            onStoneMoved = game;
        } catch (Exception ignored) {
        }
    }

    protected void shuffleBoard() throws IllegalStateException {
        try {
            putPlayerStones(Player.COLOUR_LIGHT, new int[]{0, 1, 2});
            putPlayerStones(Player.COLOUR_DARK, new int[]{4, 3, 2});
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Nullable
    protected int[] ar(int i, int colour) throws IndexOutOfBoundsException {
        if (i == 0) return new int[]{0, 1, 2};
        if (i == 1) return new int[]{0, 1};
        if (i == 2)
            if (colour == Player.COLOUR_LIGHT) return new int[]{0};
            else return new int[]{4};
        if (i == 3) return new int[]{3, 4};
        if (i == 4) return new int[]{2, 3, 4};
        throw new IndexOutOfBoundsException("Einstein fell while shuffling");
    }

    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    public boolean isCurrentPlayerWinner(int id) {
        return isInCorner(id) || areThereNoOpponentStones(id);
    }

    protected boolean isInCorner(int id) {
        Point point = new Point(id * 4, id * 4);
        Stone stone = stones.get(pointToString(point));
        return stone != null && stone.getOrientation() / 180 == id;
    }

    protected boolean areThereNoOpponentStones(int id) {
        int sum = 0;
        for (Map.Entry<?, Stone> stoneEntry : stones.entrySet()) {
            if (stoneEntry.getValue().getPlayerId() == opponent(id))
                ++sum;
        }
        return sum == 0;
    }

    @NotNull
    public HashMap<String, Stone> getStones() {
        return stones;
    }

    public void initialise() {
        try {
            tryToInitialise();
        } catch (NumberFormatException e) {
            //Log.e("Board", e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
    }

    protected abstract void tryToInitialise() throws NumberFormatException;

    public void hint(@NotNull Player player) throws NoSuchElementException {
        ArrayList<Stone> stone = findRolledStone(player);
        if (stone.isEmpty())
            stone = findTwoRolledStones(player);
        if (stone.isEmpty())
            throw new NoSuchElementException("Einstein fell looking for stones");

        selectableStones = stone;

        drawSelectableStones();
    }

    @NotNull
    public ArrayList<Stone> findRolledStone(Player player) {
        ArrayList<Stone> stone = new ArrayList<>();

        for (Map.Entry<?, Stone> stoneEntry : stones.entrySet()) {
            if (stoneEntry.getValue().hasRolledValue(player)) {
                stone.add(stoneEntry.getValue());
                break;
            }
        }
        return stone;
    }

    @NotNull
    public ArrayList<Stone> findTwoRolledStones(Player player) {
        ArrayList<Stone> stonesList = sortStones(), stone = new ArrayList<>();

        int i = 0;
        Stone prev = stonesList.get(0);
        for (Stone s : stonesList) {
            if (s.isRolledValueNextBigger(player)) {
                stone.add(s);
                break;
            }
            if (s.isPlayerBigger(player)) {
                stone.add(prev);
                break;
            }
            ++i;
            prev = s;
        }
        if (isNextLowerToBeHinted(i, stonesList))
            stone.add(stonesList.get(i - 1));

        return stone;
    }

    @NotNull
    protected ArrayList<Stone> sortStones() {
        ArrayList<Stone> stonesList = new ArrayList<>();
        for (Map.Entry<?, Stone> stoneEntry : stones.entrySet())
            stonesList.add(stoneEntry.getValue());
        Collections.sort(stonesList);
        return stonesList;

    }

    protected void drawStones() {
        for (Map.Entry<?, ?> stoneEntry : stones.entrySet()) {
            Stone stone = (Stone) stoneEntry.getValue();
            drawStone(stone);
        }
    }

    protected abstract void drawStone(@NotNull Stone stone);

    private boolean isNextLowerToBeHinted(int i, @NotNull ArrayList<Stone> stonesList) {
        return i == stonesList.size() ||
                (i > 0 && stonesList.get(i).getPlayerId() == stonesList.get(i - 1).getPlayerId());
    }

    protected boolean isInitialised() {
        return initialised;
    }

    protected abstract void putPlayerStones(int colour, @NotNull int[] arr);

    protected abstract void drawSelectableStone(@NotNull Stone s);

    protected abstract void drawMoveHintHighlight(int x, int y);

    public void fill(String socketBoard) {
        //todo
    }

    public abstract void makeMove(Point target);

    public abstract void makeSelection(Point selection);

    public interface OnStoneMoved {
        void onStoneMoved();
    }
}
