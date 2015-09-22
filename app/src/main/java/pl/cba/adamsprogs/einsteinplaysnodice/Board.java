/*
 * Copyright (c) 2015. Adam Pioterek
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 *     (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
 *     merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 *     furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *     LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 *     IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package pl.cba.adamsprogs.einsteinplaysnodice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Random;


public class Board extends AppCompatActivity {

    int player = 0;
    int[] dice = {7, 7};
    int[] board = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] diceOrder = {1, 2, 3, 4, 5, 6};
    boolean blockTouch = true;
    boolean blockRoll = false;
    int[] selected = {0, 0};
    float width;
    float dHeight;
    float bHeight;
    float bWidth;
    float sq;
    float[][] diceCrcls0, diceCrcls1;
    float dieR;
    Bitmap d0, d1, b;
    Canvas cd0, cd1, cb;
    int winner = -1;

    int[] DieImg = {R.id.die0, R.id.die1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        setContentView(R.layout.board_layout);

        ImageView boardV = (ImageView) findViewById(R.id.board);

        boardV.setOnTouchListener(
                new ImageView.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent m) {
                        handleTouch(m);
                        return true;
                    }
                }
        );

        ImageView dIV = (ImageView) findViewById(R.id.die0);
        dIV.setOnTouchListener(
                new ImageView.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent m) {
                        diePressed(v, m);
                        return true;
                    }
                }
        );
        dIV = (ImageView) findViewById(R.id.die1);
        dIV.setOnTouchListener(
                new ImageView.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent m) {
                        diePressed(v, m);
                        return true;
                    }
                }
        );

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
        width = metrics.widthPixels;
        bHeight = metrics.heightPixels / 2;
        bWidth = min((int) width, (int) bHeight);
        //noinspection SuspiciousNameCombination
        bHeight = bWidth;
        dHeight = metrics.heightPixels / 4;
        sq = bWidth / 5;

        dieR = dHeight / 10;
        float dieD = dHeight / 5;
        float off = (width - dHeight) / 2;
        diceCrcls1 = new float[][]{{off + (4 * dieD), dieD}, {off + dieD, 4 * dieD},
                {off + dieD, dieD}, {off + (4 * dieD), 4 * dieD},
                {off + (2 * dieD) + dieR, dieD}, {off + (2 * dieD) + dieR, 4 * dieD},
                {off + (2 * dieD) + dieR, (2 * dieD) + dieR}
        };
        diceCrcls0 = new float[][]{{off + dieD, dieD}, {off + (4 * dieD), 4 * dieD},
                {off + (4 * dieD), dieD}, {off + dieD, 4 * dieD},
                {off + (2 * dieD) + dieR, dieD}, {off + (2 * dieD) + dieR, 4 * dieD},
                {off + (2 * dieD) + dieR, (2 * dieD) + dieR}
        };

        d0 = Bitmap.createBitmap((int) width, (int) dHeight, Bitmap.Config.ARGB_8888);
        d1 = Bitmap.createBitmap((int) width, (int) dHeight, Bitmap.Config.ARGB_8888);
        b = Bitmap.createBitmap((int) bWidth, (int) bHeight, Bitmap.Config.ARGB_8888);
        cd0 = new Canvas(d0);
        cd1 = new Canvas(d1);
        cb = new Canvas(b);

        DrawBoard();
        DrawDice();
        ColourDie(0);

        shuffleArray(diceOrder);

        blockRoll = false;
    }

    public int min(int a, int b) {
        if (a < b) return a;
        return b;
    }

    public void DrawBoard() {
        float x, y;
        int tColour;
        int[] Col = {getResources().getColor(R.color.secondary), getResources().getColor(R.color.primary)};
        Paint p;
        p = new Paint();
        p.setAntiAlias(true);

        cb.drawColor(getResources().getColor(R.color.board));

        DrawGrid();

        p.setStyle(Paint.Style.FILL);

        for (int i = 0; i < 25; ++i) {
            x = sq * (i % 5) + (sq / 2);
            y = sq * (i / 5) + (sq / 2);

            if (board[i] > 10) {
                p.setColor(Col[1]);
                tColour = getResources().getColor(R.color.text_light);
            } else if (board[i] > 0) {
                p.setColor(Col[0]);
                tColour = getResources().getColor(R.color.text);
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
            cb.drawText(board[i] % 10 + "", x, y + (p.getTextSize() / 2), p);
        }

        ImageView iv = (ImageView) findViewById(R.id.board);
        iv.setImageBitmap(b);

    }

    public void DrawGrid() {
        Paint p;
        p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(getResources().getColor(R.color.grid));
        p.setStrokeWidth(3);

        for (int i = 0; i < 6; ++i) {
            cb.drawLine(0, i * sq, bWidth, i * sq, p);
            cb.drawLine(i * sq, 0, i * sq, bHeight, p);
        }

        ImageView iv = (ImageView) findViewById(R.id.board);
        iv.setImageBitmap(b);
    }

    public void DrawDice() {
        Paint p;
        p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);

        cd0.drawColor(getResources().getColor(R.color.dice_off));

        p.setColor(getResources().getColor(R.color.dice_num));
        for (float[] x : diceCrcls0) {
            cd0.drawCircle(x[0], x[1], dieR, p);
        }

        ImageView iv = (ImageView) findViewById(R.id.die0);
        iv.setImageBitmap(d0);

        cd1.drawColor(getResources().getColor(R.color.dice_off));

        p.setColor(getResources().getColor(R.color.dice_num));
        for (float[] x : diceCrcls1) {
            cd1.drawCircle(x[0], x[1], dieR, p);
        }
        iv = (ImageView) findViewById(R.id.die1);
        iv.setImageBitmap(d1);
    }

    public void ColourDie(int c) {
        int[] Colour = {getResources().getColor(R.color.dice_on), getResources().getColor(R.color.dice_off)};
        if (player == 0) {
            cd0.drawColor(Colour[c]);
        }
        if (player == 1) {
            cd1.drawColor(Colour[c]);
        }
        DrawDieRaw(dice[player]);
        if (player == 0) {
            ImageView iv = (ImageView) findViewById(R.id.die0);
            iv.setImageBitmap(d0);
        } else {
            ImageView iv = (ImageView) findViewById(R.id.die1);
            iv.setImageBitmap(d1);
        }
    }

    public void DrawDieRaw(int n) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(getResources().getColor(R.color.dice_num));
        if (player == 0) {
            if (n % 2 == 1) {
                cd0.drawCircle(diceCrcls0[6][0], diceCrcls0[6][1], dieR, p);
                --n;
            }
            while (n > 0) {
                cd0.drawCircle(diceCrcls0[n - 2][0], diceCrcls0[n - 2][1], dieR, p);
                cd0.drawCircle(diceCrcls0[n - 1][0], diceCrcls0[n - 1][1], dieR, p);
                n -= 2;
            }
        }
        if (player == 1) {
            if (n % 2 == 1) {
                cd1.drawCircle(diceCrcls1[6][0], diceCrcls1[6][1], dieR, p);
                --n;
            }
            while (n > 0) {
                cd1.drawCircle(diceCrcls1[n - 2][0], diceCrcls1[n - 2][1], dieR, p);
                cd1.drawCircle(diceCrcls1[n - 1][0], diceCrcls1[n - 1][1], dieR, p);
                n -= 2;
            }
        }
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
            EndingDialog();
        } else {
            blockTouch = true;
            blockRoll = false;
        }
    }

    public void EndingDialog() {
        int[] Col = {getResources().getColor(R.color.secondary), getResources().getColor(R.color.primary)};
        Paint p;
        p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Col[winner]);

        cb.drawColor(Col[winner]);

        p.setTextAlign(Paint.Align.CENTER);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Comfortaa.ttf");
        p.setTypeface(tf);

        if (winner == 0) {
            p.setColor(getResources().getColor(R.color.text));
            p.setTextSize((bWidth * p.getTextSize()) / p.measureText(getString(R.string.plLightWon)));
            cb.drawText(getString(R.string.plLightWon), bWidth / 2, bHeight / 3 + p.getTextSize() / 2, p);
        } else {
            p.setColor(getResources().getColor(R.color.text_light));
            p.setTextSize((bWidth * p.getTextSize()) / p.measureText(getString(R.string.plDarkWon)));
            cb.drawText(getString(R.string.plDarkWon), bWidth / 2, bHeight / 3 + p.getTextSize() / 2, p);
        }

        p.setTypeface(null);
        p.setTextSize((bWidth * p.getTextSize()) / p.measureText(getString(R.string.TapToCont)));
        cb.drawText(getString(R.string.TapToCont), bWidth / 2, (2 * bHeight) / 3 + p.getTextSize() / 2, p);

        ImageView iv = (ImageView) findViewById(R.id.board);
        iv.setImageBitmap(b);

        int[] res = getFile();
        ++res[winner];
        saveFile(res);
    }

    public void diePressed(View v, MotionEvent m) {
        int action = m.getActionMasked();
        if (action == MotionEvent.ACTION_UP && !blockRoll) {
            if (v.getId() == DieImg[player]) {
                blockRoll = true;
                rollDie();
            }
        }
    }

    public void rollDie() {
        shuffleArray(diceOrder);
        dice[player] = diceOrder[5];

        if (player == 0) {
            cd0.drawColor(getResources().getColor(R.color.dice_on));
        }
        if (player == 1) {
            cd1.drawColor(getResources().getColor(R.color.dice_on));
        }

        DrawDie();

        ImageView iv = (ImageView) findViewById(DieImg[player]);
        if (player == 0)
            iv.setImageBitmap(d0);
        if (player == 1)
            iv.setImageBitmap(d1);

        HintStone();
        blockTouch = false;
    }

    public void HintStone() {
        DrawBoard();
        //String tag = "HintStone";

        int[] boardBak = Arrays.copyOf(board, 25);
        Arrays.sort(boardBak);

        int i = 0;
        while (boardBak[i] == 0) ++i;
        for (; i < 25; ++i) {
            if (boardBak[i] >= ((10 * player) + dice[player]))
                break;
        }
        if (i >= 25) i = 24;
        int[] eq = {-1, -1};
        if (boardBak[i] == dice[player] + (10 * player)) {
            eq[0] = boardBak[i];
        } else {
            int k = 0;
            int d = 0;
            if (boardBak[i] / 10 == player && (boardBak[i] % 10) != 0) {
                eq[k] = boardBak[i];
                if (eq[k] > 10 * player + dice[player]) d += 10;
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
        int[] typeColour = {getResources().getColor(R.color.hint_stone), getResources().getColor(R.color.hint_stone), getResources().getColor(R.color.hint_move)};
        int[] tColour = {getResources().getColor(R.color.text), getResources().getColor(R.color.text_light)};
        int[] Col = {getResources().getColor(R.color.secondary), getResources().getColor(R.color.primary)};
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
            cb.drawText(board[pos] % 10 + "", x, y + (p.getTextSize() / 2), p);
        }

        board[pos] %= 100;
        board[pos] += 100;

        DrawGrid();

        ImageView iv = (ImageView) findViewById(R.id.board);
        iv.setImageBitmap(b);
    }

    public void shuffleArray(int[] ar) {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
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

    public void DrawDie() {
        if (player == 0) {
            cd0.drawColor(getResources().getColor(R.color.dice_on));
        }
        if (player == 1) {
            cd1.drawColor(getResources().getColor(R.color.dice_on));
        }
        DrawDieRaw(dice[player]);
    }

    public void handleTouch(MotionEvent m) { //TODO handle touch on square
        ImageView iv = (ImageView) findViewById(R.id.board);

        float offset = (iv.getWidth() - iv.getHeight()) / 2;

        float Isqh = iv.getHeight() / 5;
        float Isqw = iv.getWidth() / 5;

        int x = (int) (m.getX(0) + offset);
        int y = (int) m.getY(0);
        int action = m.getActionMasked();
        int posx = (int) (x / Isqw);
        int posy = (int) (y / Isqh);

        if (action == MotionEvent.ACTION_UP && !blockTouch) {
            if (winner != -1) {
                finish();
            }

            if (at(posx, posy) - 100 > 0 && at(posx, posy) - 100 < 100) {
                ColourDie(1);
                selected[0] = posx;
                selected[1] = posy;
                MoveHint();
            }
            if (at(posx, posy) - 200 >= 0) {
                MoveTo(posx, posy);
                for (int i = 0; i < 25; ++i) {
                    board[i] = board[i] % 100;
                }
                DrawBoard();
                CheckWin();
                player += 1;
                player %= 2;
                ColourDie(0);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public int[] getFile() {
        int[] res = {0, 0};
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(
                    openFileInput("ClokResults")));
            String inputString;
            String[] results;
            if ((inputString = inputReader.readLine()) != null) {
                results = inputString.split(" ");
                for (int i = 0; i < 2; ++i)
                    res[i] = Integer.parseInt(results[i]);
            }
        } catch (Exception ignored) {
        }
        return res;
    }

    public void saveFile(int[] r) {
        String res = r[0] + " " + r[1];
        try {
            FileOutputStream fos = openFileOutput("ClokResults", Context.MODE_PRIVATE);
            fos.write(res.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
