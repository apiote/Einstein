package ml.adamsprogs.einstein.mobile.components;

import android.content.Context;
import android.graphics.*;
import ml.adamsprogs.einstein.engine.components.Player;
import ml.adamsprogs.einstein.engine.utils.Point;
import ml.adamsprogs.einstein.engine.components.Stone;
import android.support.annotation.NonNull;
import android.view.*;
import android.widget.ImageView;

import java.util.*;

import ml.adamsprogs.einstein.R;
import ml.adamsprogs.einstein.mobile.games.MobileGame;
import org.jetbrains.annotations.NotNull;

import static ml.adamsprogs.einstein.engine.utils.Utils.pointToString;
import static ml.adamsprogs.einstein.engine.utils.Utils.shuffleArray;
import static ml.adamsprogs.einstein.mobile.utilities.Utilities.*;

public class MobileBoard extends ml.adamsprogs.einstein.engine.components.Board {
    private static final int gridWidth = 2;

    private float size, squareSize;

    private Bitmap bitmap;
    private Canvas canvas;
    private Paint p;

    protected ImageView view;

    private int boardColour;
    private int gridColour;
    private int hintColour;
    private int hintMoveColour;

    private Context context;

    public MobileBoard(@NonNull MobileGame game, ImageView view) {
        initialiseVariables(game, view);
        setUpOnClickListener();
        setUpInterfaces();
        shuffleBoard();
    }

    private void initialiseVariables(MobileGame mobileGame, ImageView view) {
        this.view = view;
        this.game = mobileGame;
        initialised = false;
        movable = false;
        context = ((MobileGame)this.game).getContext();
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
                (v, m) -> {
                    handleTouch(m);
                    return true;
                }
        );
    }

    private void handleTouch(@NonNull MotionEvent m) {
        System.out.println("touched");
        int action = m.getActionMasked();
        Point touchedPoint = getTouchPoint(m);

        if (isValidTouch(action)) {
            System.out.println("isValidTouch");
            if (isValidMoveTouch(touchedPoint)) {
                processMoveTouch(touchedPoint);
            }
            if (isValidSelectTouch(touchedPoint)) {
                System.out.println("processing select touch");
                processSelectTouch(touchedPoint);
            }
        }
    }

    @NonNull
    private Point getTouchPoint(@NonNull MotionEvent m) {
        int posX = (int) (m.getX(0) / squareSize);
        int posY = (int) (m.getY(0) / squareSize);
        return new ml.adamsprogs.einstein.engine.utils.Point(posX, posY);
    }

    private boolean isValidTouch(int action) {
        return action == MotionEvent.ACTION_UP && isMovable();
    }

    @Override
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

    @Override
    protected void drawStone(@NonNull Stone stone) {
        Point position = new Point((int) (stone.getX() * squareSize), (int) (stone.getY() * squareSize));
        canvas.drawBitmap((Bitmap) stone.getBitmap(), position.x, position.y, null);
    }

    protected void hintMove(@NonNull Point point) {
        super.hintMove(point);
        view.setImageBitmap(bitmap);
    }

    @Override
    protected void drawLiftedStone(@NotNull Stone stone) {
        stone.drawLiftShadow(gridWidth / 2);
        drawStone(stone);
    }

    @Override
    protected void drawSelectableStone(@NonNull Stone s) {

        drawHintHighlight(s.getX(), s.getY());

        Bitmap stoneBitmap = (Bitmap) s.getBitmap();
        canvas.drawBitmap(stoneBitmap, s.getX() * squareSize, s.getY() * squareSize, null);
    }

    private void drawHintHighlight(int x, int y) {
        p.setColor(hintColour);
        p.setStyle(Paint.Style.FILL);
        drawHighlight(x, y);
    }

    @Override
    protected void drawMoveHintHighlight(int x, int y) {
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

    @Override
    protected void putPlayerStones(int colour, @NonNull int[] arr) {
        int k = 0;
        MobilePlayer player = new MobilePlayer((MobileGame)game, colour);
        int[] stoneShuffleArray = {1, 2, 3, 4, 5, 6};
        shuffleArray(stoneShuffleArray);

        for (int i : arr)
            //noinspection ConstantConditions
            for (int j : ar(i, colour)) {
                Point position = new Point(j, i);
                stones.put(pointToString(position), new MobileStone(context, player, stoneShuffleArray[k++], position));
            }
    }

    public void setSize(float size) {
        this.size = size;
        squareSize = size / 5;
    }

    @Override
    protected void tryToInitialise() throws NumberFormatException {
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

    private boolean isProperSize() {
        return size + squareSize > 0;
    }

    public void hint(@NonNull Player player) throws NoSuchElementException {
        super.hint(player);
        view.setImageBitmap(bitmap);
    }
}
