package pl.cba.adamsprogs.einsteinplaysnodice.activities;

import android.content.Intent;
import android.graphics.*;
import android.support.v7.app.*;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.*;

import pl.cba.adamsprogs.einsteinplaysnodice.R;
import pl.cba.adamsprogs.einsteinplaysnodice.game.Die;

import static pl.cba.adamsprogs.einsteinplaysnodice.utilities.Utilities.*;

public class BoardActivity extends AppCompatActivity {
    private int player;
    private int[] board = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public boolean blockTouch = true;
    private int[] selected = {0, 0};
    private float bHeight;
    private float bWidth;
    private float sq;
    private Bitmap b;
    private Canvas cb;
    private int winner = -1;
    private Die[] dice;

    private int startPlayer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        setContentView(R.layout.board_layout);

        startPlayer = getIntent().getIntExtra("startPlayer", 0);

        ImageView boardV = (ImageView) findViewById(R.id.board);

        dice = new Die[2];
        dice[0] = new Die((ImageView) findViewById(R.id.die0), Die.ORIENTATION_SOUTH, this);
        dice[1] = new Die((ImageView) findViewById(R.id.die1), Die.ORIENTATION_NORTH, this);

        boardV.setOnTouchListener(
                new ImageView.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent m) {
                        handleTouch(m);
                        return true;
                    }
                }
        );

        player = startPlayer;

        int[] a = {1, 2, 3, 4, 5, 6};

        shuffleArray(a);
        board[0] = a[0];
        board[1] = a[1];
        board[2] = a[2];
        board[5] = a[3];
        board[6] = a[4];
        board[10] = a[5];

        shuffleArray(a);
        board[14] = a[0] + 10;
        board[18] = a[1] + 10;
        board[19] = a[2] + 10;
        board[22] = a[3] + 10;
        board[23] = a[4] + 10;
        board[24] = a[5] + 10;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float width = metrics.widthPixels;
        bHeight = metrics.heightPixels / 2;
        bWidth = min((int) width, (int) bHeight);
        //noinspection SuspiciousNameCombination
        bHeight = bWidth;
        sq = bWidth / 5;

        for (Die d : dice) {
            d.setHeight(metrics.heightPixels >> 2);
            d.setWidth(width);
            try {
                d.draw();
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Couldn't draw dice", Toast.LENGTH_SHORT).show();
            }
        }

        b = Bitmap.createBitmap((int) bWidth, (int) bHeight, Bitmap.Config.ARGB_8888);
        cb = new Canvas(b);

        DrawBoard();

        dice[0].setRollable(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == -1) {
                finish();
            }
        }
    }

    public int min(int a, int b) {
        if (a < b) return a;
        return b;
    }

    public void DrawBoard() {
        Bitmap stone;
        Canvas stoneCanvas;
        Bitmap resStone;
        float x, y;
        int tColour;
        int[] Col = {getColour(this, R.color.secondary), getColour(this, R.color.primary)};
        Paint p;
        p = new Paint();
        p.setAntiAlias(true);

        cb.drawColor(getColour(this, R.color.board));

        DrawGrid();

        p.setStyle(Paint.Style.FILL);

        for (int i = 0; i < 25; ++i) {
            x = sq * (i % 5) + (sq / 2);
            y = sq * (i / 5) + (sq / 2);

            if (board[i] > 10) {
                p.setColor(Col[1]);
                tColour = getColour(this, R.color.text_light);
            } else if (board[i] > 0) {
                p.setColor(Col[0]);
                tColour = getColour(this, R.color.text);
            } else
                continue;

            Bitmap ambientShadow = BitmapFactory.decodeResource(getResources(), R.drawable.shadowambient);

            Rect src = new Rect(0, 0, ambientShadow.getWidth(), ambientShadow.getHeight());
            Rect dst = new Rect((int) (x - (sq / 2)), (int) (y - (sq / 2)), (int) (x + (sq / 2)), (int) (y + (sq / 2)));

            cb.drawBitmap(ambientShadow, src, dst, null);
            cb.drawCircle(x, y, sq / 3, p);
            p.setColor(tColour);
            p.setTextSize(sq / 2);
            p.setTextAlign(Paint.Align.CENTER);

            stone = Bitmap.createBitmap((int) sq, (int) sq, Bitmap.Config.ARGB_8888);
            stoneCanvas = new Canvas(stone);
            Rect bounds = new Rect();
            String text = board[i] % 10 + "";
            p.getTextBounds(text, 0, text.length(), bounds);
            float height = bounds.height() / 2;
            stoneCanvas.drawText(text, sq / 2, sq / 2 + height, p);

            Matrix mx = new Matrix();
            mx.postRotate(180);

            if (board[i] > 10)
                resStone = Bitmap.createBitmap(stone);
            else
                resStone = Bitmap.createBitmap(stone, 0, 0, (int) sq, (int) sq, mx, false);


            cb.drawBitmap(resStone, x - (sq / 2), y - (sq / 2), p);
        }

        ImageView iv = (ImageView) findViewById(R.id.board);
        iv.setImageBitmap(b);

    }

    public void DrawGrid() {
        Paint p;
        p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(getColour(this, R.color.grid));
        p.setStrokeWidth(3);

        for (int i = 0; i < 6; ++i) {
            cb.drawLine(0, i * sq, bWidth, i * sq, p);
            cb.drawLine(i * sq, 0, i * sq, bHeight, p);
        }

        ImageView iv = (ImageView) findViewById(R.id.board);
        iv.setImageBitmap(b);
    }

    public void MoveHint() {
        int wx, wy;

        for (int i = 0; i < 25; ++i) {
            if (board[i] > 200) board[i] = board[i] % 100;
        }
        DrawBoard();
        for (int i = 0; i < 25; ++i) {
            if (board[i] > 100 && board[i] < 200) {
                DrawHint(i, 1);
            }
        }

        if (player == 0) {
            wx = 1;
            wy = 1;
        } else {
            wx = -1;
            wy = -1;
        }
        int[][] hint = {{selected[0] + wx, selected[1] + wy}, {selected[0] + wx, selected[1]}, {selected[0], selected[1] + wy}};

        for (int i = 0; i < 3; ++i) {
            if (hint[i][0] > 4 || hint[i][1] > 4 || hint[i][0] < 0 || hint[i][1] < 0)
                continue;
            int hntPos = hint[i][1] * 5 + hint[i][0];
            if (hntPos >= 0 && hntPos < 25) {
                DrawHint(hntPos, 2);
                setAt(hint[i][0], hint[i][1], at(hint[i][0], hint[i][1]) + 200);
            }
        }
    }

    public void MoveTo(int x, int y) {
        setAt(x, y, at(selected[0], selected[1]));
        setAt(selected[0], selected[1], 0);

        DrawBoard();
    }

    public void CheckWin() {
        int[] p = {0, 0};
        for (int i = 0; i < 25; ++i) {
            if (board[i] % 100 > 0 && board[i] % 100 < 10) ++p[0];
            if (board[i] % 100 > 10) ++p[1];
        }
        if (p[0] == 0) winner = 1;
        if (p[1] == 0) winner = 0;

        if (board[0] % 100 > 10) winner = 1;
        if (board[24] % 100 > 0 && board[24] % 100 < 10) winner = 0;

        if (winner != -1) {
            endingDialog();
        } else {
            blockTouch = true;
        }
    }

    private void endingDialog() {
        blockTouch = true;
        for (Die d : dice)
            d.setRollable(false);
        Intent endingIntent = new Intent(this, EndingDialogueActivity.class);
        endingIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        endingIntent.putExtra("winner", winner);
        endingIntent.putExtra("startPlayer", startPlayer);
        startActivityForResult(endingIntent, 0);
    }

    public void HintStone() {
        DrawBoard();

        int[] boardBak = Arrays.copyOf(board, 25);
        Arrays.sort(boardBak);

        int i = 0;
        while (boardBak[i] == 0) ++i;
        for (; i < 25; ++i) {
            if (boardBak[i] >= ((10 * player) + dice[player].getValue()))
                break;
        }
        if (i >= 25) i = 24;
        int[] eq = {-1, -1};
        if (boardBak[i] == dice[player].getValue() + (10 * player)) {
            eq[0] = boardBak[i];
        } else {
            int k = 0;
            int d = 0;
            if (boardBak[i] / 10 == player && (boardBak[i] % 10) != 0) {
                eq[k] = boardBak[i];
                if (eq[k] > 10 * player + dice[player].getValue()) d += 10;
                else ++d;
                ++k;
            }
            if (boardBak[i - 1] / 10 == player && (boardBak[i - 1] % 10) != 0) {
                if (d % 10 != 1) {
                    eq[k] = boardBak[i - 1];
                }
            }
        }
        for (int k = 0; k < 2; ++k)
            if (eq[k] > -1) {
                i = 0;
                while (board[i] != eq[k]) ++i;
                DrawHint(i, 0);
            }
    }

    public void DrawHint(int pos, int type) {
        float x, y;
        int[] typeColour = {getColour(this, R.color.hint_stone), getColour(this, R.color.hint_stone), getColour(this, R.color.hint_move)};
        int[] tColour = {getColour(this, R.color.text), getColour(this, R.color.text_light)};
        int[] Col = {getColour(this, R.color.secondary), getColour(this, R.color.primary)};
        Paint p;
        p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);

        x = sq * (pos % 5) + (sq / 2);
        y = sq * (pos / 5) + (sq / 2);

        Rect hintBg = new Rect((int) x - (int) (sq / 2) + 1, (int) y - (int) (sq / 2) + 1, (int) x + (int) (sq / 2) - 1, (int) y + (int) (sq / 2) - 1);

        p.setColor(typeColour[type]);
        cb.drawRect(hintBg, p);

        if (board[pos] != 0) {
            Bitmap ambientShadow = BitmapFactory.decodeResource(getResources(), R.drawable.shadowambient);
            Bitmap directionalShadow = BitmapFactory.decodeResource(getResources(), R.drawable.shadowdirectional);

            Rect srcA = new Rect(0, 0, ambientShadow.getWidth(), ambientShadow.getHeight());
            Rect dstA = new Rect((int) (x - (sq / 2)), (int) (y - (sq / 2)), (int) (x + (sq / 2)), (int) (y + (sq / 2)));
            Rect srcD = new Rect(0, 0, directionalShadow.getWidth(), directionalShadow.getHeight());
            Rect dstD = new Rect((int) (x - (sq / 2)), (int) (y - (sq / 2)), (int) (x + (sq / 2)), (int) (y + (sq / 2)));

            cb.drawBitmap(ambientShadow, srcA, dstA, null);
            if (type == 1 && pos == selected[0] + (5 * selected[1]))
                cb.drawBitmap(directionalShadow, srcD, dstD, null);
            p.setColor(Col[(board[pos] / 10) % 10]);
            cb.drawCircle(x, y, sq / 3, p);
            p.setColor(tColour[(board[pos] / 10) % 10]);
            p.setTextSize(sq / 2);
            p.setTextAlign(Paint.Align.CENTER);

            Bitmap stone = Bitmap.createBitmap((int) sq, (int) sq, Bitmap.Config.ARGB_8888);
            Canvas stoneCanvas = new Canvas(stone);
            Bitmap resStone;
            Rect bounds = new Rect();
            String text = board[pos] % 10 + "";
            p.getTextBounds(text, 0, text.length(), bounds);
            float height = bounds.height() / 2;
            stoneCanvas.drawText(text, sq / 2, sq / 2 + height, p);

            Matrix mx = new Matrix();
            mx.postRotate(180);

            if (board[pos] % 100 > 10) {
                resStone = Bitmap.createBitmap(stone);
            } else {
                resStone = Bitmap.createBitmap(stone, 0, 0, (int) sq, (int) sq, mx, false);
            }

            cb.drawBitmap(resStone, x - (sq / 2), y - (sq / 2), p);
        }

        board[pos] %= 100;
        board[pos] += 100;

        DrawGrid();

        ImageView iv = (ImageView) findViewById(R.id.board);
        iv.setImageBitmap(b);
    }

    public int at(int x, int y) {
        if ((y * 5) + x < 0 || (y * 5) + x > 24)
            return -1;
        return board[y * 5 + x];
    }

    public void setAt(int x, int y, int v) {
        if ((y * 5) + x < 0 || (y * 5) + x > 24)
            return;
        board[y * 5 + x] = v;
    }

    public void handleTouch(MotionEvent m) {
        ImageView iv = (ImageView) findViewById(R.id.board);

        float offset = (iv.getWidth() - iv.getHeight()) / 2;

        float iSqH = iv.getHeight() / 5;
        float iSqW = iv.getWidth() / 5;

        int x = (int) (m.getX(0) + offset);
        int y = (int) m.getY(0);
        int action = m.getActionMasked();
        int posX = (int) (x / iSqW);
        int posY = (int) (y / iSqH);

        if (action == MotionEvent.ACTION_UP && !blockTouch) {
            if (at(posX, posY) - 100 > 0 && at(posX, posY) - 100 < 100) {
                dice[player].setRollable(false);
                selected[0] = posX;
                selected[1] = posY;
                MoveHint();
            }
            if (at(posX, posY) - 200 >= 0) {
                MoveTo(posX, posY);
                for (int i = 0; i < 25; ++i) {
                    board[i] = board[i] % 100;
                }
                DrawBoard();
                CheckWin();
                player += 1;
                player %= 2;
                dice[player].setRollable(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
