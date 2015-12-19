package pl.cba.adamsprogs.einsteinplaysnodice.components;

import android.content.Context;
import android.graphics.*;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;

import java.util.*;

import pl.cba.adamsprogs.einsteinplaysnodice.R;
import pl.cba.adamsprogs.einsteinplaysnodice.games.ServerGame;

import static pl.cba.adamsprogs.einsteinplaysnodice.utilities.Utilities.*;

public class Board {
    private static final int gridWidth = 2;

    private float size, squareSize;

    private Bitmap bitmap;
    private Canvas canvas;
    private Paint p;

    private ImageView view;

    private int boardColour;
    private int gridColour;
    private int hintColour;
    private int hintMoveColour;

    private OnStoneMoved onStoneMoved;

    private boolean initialised;
    private boolean movable;

    @NonNull
    private HashMap<Point, Stone> stones = new HashMap<>();
    private ArrayList<Stone> selectableStones;
    private Stone selectedStone;
    private Point[] targetablePoints;

    private ServerGame game;
    private Context context;

    public Board(@NonNull ServerGame game, ImageView view) {
        initialiseVariables(game, view);
        setUpOnClickListener();
        setUpInterfaces();
        shuffleBoard();
    }

    private void initialiseVariables(ServerGame serverGame, ImageView view) {
        this.view = view;
        this.game = serverGame;
        initialised = false;
        movable = false;
        context = this.game.getContext();
        initialiseColours();
        initialisePaint();
    }

    private void initialiseColours() {
        boardColour = getColour(context, R.color.board);
        gridColour = getColour(context, R.color.grid);
        hintColour = getColour(context, R.color.hint_stone);
        hintMoveColour = getColour(context, R.color.hint_move);
    }

    private void initialisePaint() {
        p = new Paint();
        p.setAntiAlias(true);
    }

    private void setUpOnClickListener() {
        this.view.setOnTouchListener(
                new ImageView.OnTouchListener() {
                    public boolean onTouch(View v, @NonNull MotionEvent m) {
                        handleTouch(m);
                        return true;
                    }
                }
        );
    }

    private void handleTouch(@NonNull MotionEvent m) {
        int action = m.getActionMasked();
        Point touchedPoint = getTouchPoint(m);

        if (isValidTouch(action)) {
            if (isValidMoveTouch(touchedPoint)) {
                processMoveTouch(touchedPoint);
            }
            if (isValidSelectTouch(touchedPoint)) {
                processSelectTouch(touchedPoint);
            }
        }
    }

    @NonNull
    private Point getTouchPoint(@NonNull MotionEvent m) {
        int posX = (int) (m.getX(0) / squareSize);
        int posY = (int) (m.getY(0) / squareSize);
        return new Point(posX, posY);
    }

    private boolean isValidTouch(int action) {
        return action == MotionEvent.ACTION_UP && isMovable();
    }

    private boolean isMovable() {
        return movable;
    }

    private boolean isValidMoveTouch(Point touchedPoint) {
        return isPointTargetable(touchedPoint) && selectedStone != null;
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

    private void processMoveTouch(Point target) {
        moveStone(target);
        draw();
        clearTouchability();
        onStoneMoved.onStoneMoved();
    }

    private void moveStone(Point target) {
        stones.remove(selectedStone.getPosition());
        selectedStone.moveTo(target);
        stones.remove(target);
        stones.put(target, selectedStone);
    }

    public void draw() {
        drawGrid();
        drawStones();
        view.setImageBitmap(bitmap);

    }

    private void drawGrid() {
        p.setStyle(Paint.Style.STROKE);
        p.setColor(gridColour);
        p.setStrokeWidth(gridWidth);
        canvas.drawColor(boardColour);
        for (int i = 0; i < 6; ++i) {
            canvas.drawLine(0, i * squareSize, size, i * squareSize, p);
            canvas.drawLine(i * squareSize, 0, i * squareSize, size, p);
        }
    }

    private void drawStones() {
        for (Map.Entry<?, ?> stoneEntry : stones.entrySet()) {
            Stone stone = (Stone) stoneEntry.getValue();
            drawStone(stone);
        }
    }

    private void drawStone(@NonNull Stone stone) {
        Point position = new Point((int) (stone.getX() * squareSize), (int) (stone.getY() * squareSize));
        canvas.drawBitmap(stone.getBitmap(), position.x, position.y, null);
    }

    private void clearTouchability() {
        selectedStone.setSelectable(false);
        movable = false;
        targetablePoints = null;
        clearHint();
        selectableStones = null;
    }

    private void clearHint() {
        for (Map.Entry<?, Stone> stone : stones.entrySet())
            stone.getValue().setSelectable(false);
    }

    private boolean isValidSelectTouch(Point touchedPoint) {
        Stone tempSelectedStone = stones.get(touchedPoint);
        return (tempSelectedStone != null && tempSelectedStone.isSelectable());
    }

    public void processSelectTouch(@NonNull Point touchedPoint) {
        selectedStone = stones.get(touchedPoint);
        selectedStone.setSelectable(false);
        hintMove(touchedPoint);
    }

    private void hintMove(@NonNull Point point) {
        createTargetablePoints(point);
        draw();
        drawSelectableStones();
        Stone stone = stones.get(point);

        drawLiftedStone(stone);

        for (Point targetPoint : targetablePoints) {
            drawMoveHintHighlight(targetPoint.x, targetPoint.y);
            stone = stones.get(targetPoint);
            if (stone != null) {
                drawStone(stone);
            }
        }
        view.setImageBitmap(bitmap);
    }

    private void createTargetablePoints(@NonNull Point touchedPoint) {
        int targetDirection = selectedStone.getPlayerId() == 0 ? -1 : 1;
        targetablePoints = new Point[]{new Point(touchedPoint.x + targetDirection, touchedPoint.y),
                new Point(touchedPoint.x, touchedPoint.y + targetDirection),
                new Point(touchedPoint.x + targetDirection, touchedPoint.y + targetDirection)};
    }

    private void drawSelectableStones() {
        if(selectableStones==null)
            return;
        for (Stone s : selectableStones) {
            drawSelectableStone(s);
            s.setSelectable(true);
        }
    }

    private void drawSelectableStone(@NonNull Stone s) {

        drawHintHighlight(s.getX(), s.getY());

        Bitmap stoneBitmap = s.getBitmap();
        canvas.drawBitmap(stoneBitmap, s.getX() * squareSize, s.getY() * squareSize, null);
    }

    private void drawHintHighlight(int x, int y) {
        p.setColor(hintColour);
        p.setStyle(Paint.Style.FILL);
        drawHighlight(x, y);
    }

    private void drawLiftedStone(@NonNull Stone stone) {
        stone.drawDirectionalShadow(canvas, gridWidth);
        drawStone(stone);
    }

    private void drawMoveHintHighlight(int x, int y) {
        p.setColor(hintMoveColour);
        p.setStyle(Paint.Style.FILL);
        drawHighlight(x, y);
    }

    private void drawHighlight(int x, int y) {
        RectF out = new RectF((x * squareSize) + (gridWidth >> 1),
                y * squareSize + (gridWidth >> 1),
                (x + 1) * squareSize - (gridWidth >> 1),
                (y + 1) * squareSize - (gridWidth >> 1));
        canvas.drawRect(out, p);
    }

    private void setUpInterfaces() {
        try {
            onStoneMoved = game;
        } catch (Exception ignored) {
        }
    }

    private void shuffleBoard() throws IllegalStateException{
        try {
            putPlayerStones(Player.COLOUR_LIGHT, new int[]{0, 1, 2});
            putPlayerStones(Player.COLOUR_DARK, new int[]{4, 3, 2});
        }catch(IndexOutOfBoundsException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private void putPlayerStones(int colour, @NonNull int[] arr) {
        int k = 0;
        Player player = new Player(game, colour);
        int[] stoneShuffleArray = {1, 2, 3, 4, 5, 6};
        shuffleArray(stoneShuffleArray);

        for (int i : arr)
            //noinspection ConstantConditions
            for (int j : ar(i, colour)) {
                Point position = new Point(j, i);
                stones.put(position, new Stone(context, player, stoneShuffleArray[k++], position));
            }
    }

    @Nullable
    private int[] ar(int i, int colour) throws IndexOutOfBoundsException {
        if (i == 0) return new int[]{0, 1, 2};
        if (i == 1) return new int[]{0, 1};
        if (i == 2)
            if (colour == Player.COLOUR_LIGHT) return new int[]{0};
            else return new int[]{4};
        if (i == 3) return new int[]{3, 4};
        if (i == 4) return new int[]{2, 3, 4};
        throw new IndexOutOfBoundsException("Einstein fell while shuffling");
    }

    public void setSize(float size) {
        this.size = size;
        squareSize = size / 5;
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
        return sum == 0;
    }

    @NonNull
    public HashMap<Point, Stone> getStones() {
        return stones;
    }

    public interface OnStoneMoved {
        void onStoneMoved();
    }

    public void initialise() {
        try {
            tryToInitialise();
        } catch (NumberFormatException e) {
            Log.e("Board", e.getMessage());
            throw new IllegalStateException("Couldn't initialise board due to wrong size");
        }
    }

    private void tryToInitialise() throws NumberFormatException {
        if (isInitialised())
            return;
        if (!isProperSize())
            throw new NumberFormatException("Size must be greater than zero");

        bitmap = Bitmap.createBitmap((int) size, (int) size, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        for (Map.Entry<?, Stone> x : stones.entrySet()) {
            x.getValue().create(squareSize);
        }

        initialised = true;
    }

    private boolean isInitialised() {
        return initialised;
    }

    private boolean isProperSize() {
        return size + squareSize > 0;
    }

    public void hint(@NonNull Player player) throws NoSuchElementException {
        ArrayList<Stone> stone = findRolledStone(player);
        if (stone.isEmpty())
            stone = findTwoRolledStones(player);
        if (stone.isEmpty())
            throw new NoSuchElementException("Einstein fell looking for stones");

        selectableStones = stone;

        drawSelectableStones();
        view.setImageBitmap(bitmap);
    }

    @NonNull
    private ArrayList<Stone> findRolledStone(Player player) {
        ArrayList<Stone> stone = new ArrayList<>();

        for (Map.Entry<?, Stone> stoneEntry : stones.entrySet()) {
            if (stoneEntry.getValue().hasRolledValue(player)) {
                stone.add(stoneEntry.getValue());
                break;
            }
        }
        return stone;
    }

    @NonNull
    private ArrayList<Stone> findTwoRolledStones(Player player) {
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

    @NonNull
    private ArrayList<Stone> sortStones() {
        ArrayList<Stone> stonesList = new ArrayList<>();
        for (Map.Entry<?, Stone> stoneEntry : stones.entrySet())
            stonesList.add(stoneEntry.getValue());
        Collections.sort(stonesList);
        return stonesList;

    }

    private boolean isNextLowerToBeHinted(int i, @NonNull ArrayList<Stone> stonesList) {
        return i == stonesList.size() || (i > 0 && stonesList.get(i).getPlayerId() == stonesList.get(i - 1).getPlayerId());
    }
}
