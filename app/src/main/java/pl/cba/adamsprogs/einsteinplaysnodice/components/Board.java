package pl.cba.adamsprogs.einsteinplaysnodice.components;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;

import java.util.*;

import pl.cba.adamsprogs.einsteinplaysnodice.R;
import pl.cba.adamsprogs.einsteinplaysnodice.games.ServerGame;

import static pl.cba.adamsprogs.einsteinplaysnodice.utilities.Utilities.*;

public class Board {
    private ImageView view;
    private float size, squareSize;
    private HashMap<Point, Stone> stones;
    private Bitmap bitmap;
    private Canvas canvas;
    private OnStoneMoved onStoneMoved;
    private OnStoneSelected onStoneSelected;
    private boolean initialised;
    private int boardColour, gridColour;
    private int hintColour, hintMoveColour;
    private Paint p;
    private boolean movable;
    private int gridWidth = 2;
    private Stone selectedStone;
    private Point[] targetablePoints;
    private Player tempCurrentPlayer;

    public Board(ServerGame serverGame, ImageView view) {
        this.view = view;
        initialised = false;
        movable = false;
        Context context = serverGame.getContext();

        p = new Paint();
        p.setAntiAlias(true);

        this.view.setOnTouchListener(
                new ImageView.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent m) {
                        handleTouch(m);
                        return true;
                    }
                }
        );

        boardColour = getColour(context, R.color.board);
        gridColour = getColour(context, R.color.grid);
        hintColour = getColour(context, R.color.hint_stone);
        hintMoveColour = getColour(context, R.color.hint_move);

        try {
            onStoneSelected = serverGame;
            onStoneMoved = serverGame;
        } catch (Exception ignored) {
        }

        stones = new HashMap<>();

        int k = 0;
        Player player = new Player(serverGame, Player.COLOUR_LIGHT);
        int[] stoneShuffleArray = {1, 2, 3, 4, 5, 6};
        shuffleArray(stoneShuffleArray);
        for (int i : new int[]{0, 1, 2})
            for (int j = 0; j < 3 - i; ++j) {
                Point position = new Point(j, i);
                stones.put(position, new Stone(context, player, stoneShuffleArray[k++], position));
            }

        k = 0;
        player = new Player(serverGame, Player.COLOUR_DARK);
        shuffleArray(stoneShuffleArray);
        for (int i : new int[]{4, 3, 2})
            for (int j = 4; j > 5 - i; --j) {
                Point position = new Point(j, i);
                stones.put(position, new Stone(context, player, stoneShuffleArray[k++], position));
            }
    }

    private void initialise() throws NumberFormatException {
        if (isInitialised())
            return;
        if (!isProperSize())
            throw new NumberFormatException("Size must be greater than zero");

        bitmap = Bitmap.createBitmap((int) size, (int) size, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        initialised = true;
    }

    public void draw() {
        try {
            initialise();
        } catch (NumberFormatException e) {
            Log.e("Die", e.getMessage());
            throw new IllegalStateException("Couldn't initialise die due to wrong size");
        }

        canvas.drawColor(boardColour);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(gridColour);
        p.setStrokeWidth(gridWidth);
        for (int i = 0; i < 6; ++i) {
            canvas.drawLine(0, i * squareSize, size, i * squareSize, p);
            canvas.drawLine(i * squareSize, 0, i * squareSize, size, p);
        }

        drawStones();

        view.setImageBitmap(bitmap);

    }

    private void drawStones() {
        for (Map.Entry<?, ?> stoneEntry : stones.entrySet()) {
            Stone stone = (Stone) stoneEntry.getValue();
            drawStone(stone);
        }
    }

    private void drawStone(Stone stone) {
        Point position = new Point((int) (stone.getX() * squareSize), (int) (stone.getY() * squareSize));
        canvas.drawBitmap(stone.create(squareSize), position.x, position.y, null);
    }

    public void handleTouch(MotionEvent m) {
        float offset = (view.getWidth() - view.getHeight()) / 2; //FIXME what is real offset

        float iSqH = view.getHeight() / 5;
        float iSqW = view.getWidth() / 5;

        int x = (int) (m.getX(0) + offset);
        int y = (int) m.getY(0);
        int action = m.getActionMasked();
        int posX = (int) (x / iSqW);
        int posY = (int) (y / iSqH);

        if (action == MotionEvent.ACTION_UP && isMovable()) {
            Point touchedPoint = new Point(posX, posY);
            if (isPointTargetable(touchedPoint) && selectedStone != null) {
                stones.remove(new Point(selectedStone.getX(), selectedStone.getY()));
                selectedStone.moveTo(posX, posY);
                selectedStone.setSelected(false);
                stones.remove(touchedPoint);
                stones.put(touchedPoint, selectedStone);
                draw();
                onStoneMoved.onStoneMoved();
                setMovable(false);
                targetablePoints = null;
                clearHint();
                tempCurrentPlayer = null;
            } else {
                selectedStone = stones.get(touchedPoint);
                if (selectedStone != null && selectedStone.isSelectable()) {
                    selectedStone.setSelected();
                    int targetDirection = selectedStone.getPlayerId() == 0 ? -1 : 1;
                    targetablePoints = new Point[]{new Point(touchedPoint.x + targetDirection, touchedPoint.y),
                            new Point(touchedPoint.x, touchedPoint.y + targetDirection),
                            new Point(touchedPoint.x + targetDirection, touchedPoint.y + targetDirection)};
                    hintMove(touchedPoint);
                    /*stones.remove(touchedPoint);
                    stones.put(touchedPoint, selectedStone);*/
                    onStoneSelected.onStoneSelected(touchedPoint);
                }
            }
        }
    }

    private void clearHint() {
        for (Map.Entry<?, Stone> stone : stones.entrySet())
            stone.getValue().setSelectable(false);
    }

    private boolean isPointTargetable(Point touchedPoint) {
        if (targetablePoints == null)
            return false;
        for (Point p : targetablePoints) {
            if (p.equals(touchedPoint))
                return true;
        }
        return false;
    }

    public void hint(Player player) {
        tempCurrentPlayer = player;
        String value = player.getDieValue() + "";
        ArrayList<Stone> stone = new ArrayList<>();
        for (Map.Entry<?, Stone> stoneEntry : stones.entrySet()) {
            if (stoneEntry.getValue().getValue().equals(value)
                    && stoneEntry.getValue().getPlayerId() == player.getId()) {
                stone.add(stoneEntry.getValue());
                break;
            }
        }


        if (stone.isEmpty()) { //FIXME 6 doesn't work
            ArrayList<Stone> stonesList = new ArrayList<>();
            for (Map.Entry<?, Stone> stoneEntry : stones.entrySet())
                stonesList.add(stoneEntry.getValue());
            Collections.sort(stonesList);

            int i;
            for (i = 0; i < stonesList.size(); ++i) {
                Stone s = stonesList.get(i);
                if (s.getIntValue() > Integer.parseInt(value) && s.getPlayerId() == player.getId()) {
                    stone.add(stonesList.get(i));
                    break;
                }
            }
            if (i > 0 && stonesList.get(i - 1).getPlayerId() == player.getId())
                stone.add(stonesList.get(i - 1));
        }
        if (stone.isEmpty())
            return;

        for (Stone s : stone) {
            drawSelectableStone(s);
            s.setSelectable(true);
        }
        view.setImageBitmap(bitmap);
    }

    private void drawSelectableStone(Stone s) {
        p.setColor(hintColour);
        p.setStyle(Paint.Style.FILL);
        drawHintRect(s.getX(), s.getY());

        Bitmap stoneBitmap = s.getBitmap();
        canvas.drawBitmap(stoneBitmap, s.getX() * squareSize, s.getY() * squareSize, null);
    }

    private void drawHintRect(int x, int y) {
        RectF out = new RectF((x * squareSize) + (gridWidth >> 1),
                y * squareSize + (gridWidth >> 1),
                (x + 1) * squareSize - (gridWidth >> 1),
                (y + 1) * squareSize - (gridWidth >> 1));
        canvas.drawRect(out, p);
    }

    public void hintMove(Point point) {
        draw();
        hint(tempCurrentPlayer);
        Stone stone = stones.get(point);

        if (stone == null)
            return;

        p.setColor(hintMoveColour);
        p.setStyle(Paint.Style.FILL);

        int x = stone.getX();
        int y = stone.getY();

        Bitmap shadow = stone.getDirectionalShadow();
        Rect shadowRect = new Rect(0, 0, shadow.getWidth(), shadow.getHeight());
        Rect out = new Rect(x * (int) squareSize + gridWidth, y * (int) squareSize + gridWidth,
                (x + 1) * (int) squareSize - gridWidth, (y + 1) * (int) squareSize - gridWidth);
        canvas.drawBitmap(shadow, shadowRect, out, null);

        Bitmap stoneBitmap = stone.getBitmap();
        canvas.drawBitmap(stoneBitmap, stone.getX() * squareSize, stone.getY() * squareSize, null);

        for (Point p : targetablePoints) {
            x = p.x;
            y = p.y;
            drawHintRect(x, y);
            stone = stones.get(p);
            if (stone != null) {
                stoneBitmap = stone.getBitmap();
                canvas.drawBitmap(stoneBitmap, stone.getX() * squareSize, stone.getY() * squareSize, null);
            }
        }
        view.setImageBitmap(bitmap);
    }

    private boolean isProperSize() {
        return size + squareSize > 0;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
        squareSize = size / 5;
    }

    public boolean isInitialised() {
        return initialised;
    }

    public boolean isMovable() {
        return movable;
    }

    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    public boolean isCurrentPlayerWinner(int id) {
        return isInCorner(id) || areThereNoOpponentStones(id);
    }

    private boolean isInCorner(int id) {
        Point point = new Point(id * 4, id * 4);
        Stone stone = stones.get(point);
        return stone != null && stone.getOrientation() / 180 == id;
    }

    private boolean areThereNoOpponentStones(int id) {
        int sum = 0;
        for (Map.Entry<?, Stone> stoneEntry : stones.entrySet()) {
            if (stoneEntry.getValue().getPlayerId() == id)
                ++sum;
        }
        Log.i("NoOpponents", "sum=" + sum);
        return sum == 0;
    }

    public HashMap<Point, Stone> getStones() {
        return stones;
    }

    public interface OnStoneMoved {
        void onStoneMoved();
    }

    public interface OnStoneSelected {
        void onStoneSelected(Point point);
    }
}
