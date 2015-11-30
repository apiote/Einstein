package pl.cba.adamsprogs.einsteinplaysnodice.activities;

import android.content.*;
import android.graphics.*;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.ImageView;

import pl.cba.adamsprogs.einsteinplaysnodice.R;
import pl.cba.adamsprogs.einsteinplaysnodice.components.Player;
import pl.cba.adamsprogs.einsteinplaysnodice.utilities.ResultsFile;

import static pl.cba.adamsprogs.einsteinplaysnodice.utilities.Utilities.getColour;

public class EndingDialogueActivity extends AppCompatActivity {
    private int startPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ending_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int winner = getIntent().getIntExtra("winner", -1);
        startPlayer = getIntent().getIntExtra("startPlayer", -1);

        if (winner == -1 || startPlayer == -1)
            finish();

        ++startPlayer;
        startPlayer %= 2;

        Context context = this;

        ResultsFile resultsFile = new ResultsFile(context);

        resultsFile.increment(winner);
        resultsFile.apply();
        int res[] = resultsFile.getResults();

        ImageView view = (ImageView) findViewById(R.id.endingDialog);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int windowWidth = metrics.widthPixels;
        final int windowHeight = metrics.heightPixels;
        Bitmap bitmap = Bitmap.createBitmap(windowWidth, windowHeight, Bitmap.Config.ARGB_8888),
                mirrorPre = Bitmap.createBitmap(windowWidth, windowHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap),
                mirrorCanvas = new Canvas(mirrorPre);
        Matrix mx = new Matrix();
        mx.postRotate(180);

        int[] Colour = new int[2];
        Colour[Player.COLOUR_DARK] = getColour(this, R.color.dark);
        Colour[Player.COLOUR_LIGHT] = getColour(this, R.color.light);

        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Colour[winner]);

        String won, lRes = res[Player.COLOUR_LIGHT] + "", dRes = res[Player.COLOUR_DARK] + "";


        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        p.setTypeface(tf);
        p.setTextAlign(Paint.Align.CENTER);

        if (winner == Player.COLOUR_LIGHT) {
            won = getString(R.string.plLightWon);
        } else {
            won = getString(R.string.plDarkWon);
        }

        p.setTextSize((windowWidth * p.getTextSize()) / p.measureText(won));
        canvas.drawText(won, windowWidth / 2, windowHeight / 2 + p.getTextSize() * 2, p);
        mirrorCanvas.drawText(won, windowWidth / 2, windowHeight / 2 + p.getTextSize() * 2, p); //WON

        p.setTextSize(p.getTextSize() * 2);
        Rect bounds = new Rect();
        p.getTextBounds(lRes + getString(R.string.resultsSeparator) + dRes, 0, lRes.length() + 1 + dRes.length(), bounds); //RES Bounds

        p.setColor(Colour[Player.COLOUR_LIGHT]);
        canvas.drawText(lRes, 3 * windowWidth / 4 - p.measureText(lRes), windowHeight / 2 + bounds.height() / 2, p);
        mirrorCanvas.drawText(lRes, 3 * windowWidth / 4 - p.measureText(lRes), windowHeight / 2 + bounds.height() / 2, p); //LRES

        p.setColor(getColour(context, R.color.text));
        canvas.drawText(getString(R.string.resultsSeparator), 3 * windowWidth / 4, windowHeight / 2 + bounds.height() / 2, p);
        mirrorCanvas.drawText(getString(R.string.resultsSeparator), 3 * windowWidth / 4, windowHeight / 2 + bounds.height() / 2, p); //:

        p.setColor(Colour[Player.COLOUR_DARK]);
        canvas.drawText(dRes, 3 * windowWidth / 4 + p.measureText(lRes), windowHeight / 2 + bounds.height() / 2, p);
        mirrorCanvas.drawText(dRes, 3 * windowWidth / 4 + p.measureText(lRes), windowHeight / 2 + bounds.height() / 2, p); //RRES

        Bitmap mirrorPost = Bitmap.createBitmap(mirrorPre, 0, 0, mirrorPre.getWidth(), mirrorPre.getHeight(), mx, false);
        canvas.drawBitmap(mirrorPost, 0, 0, p);

        Bitmap exitBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_close_black_48dp);
        Bitmap replayBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_replay_black_48dp);

        int squareSide = Math.min(windowHeight / 5, windowWidth / 5);
        int left = windowWidth / 2 - squareSide;
        int right = windowWidth / 2 + squareSide;

        int top = windowHeight / 5 - squareSide;
        int bottom = windowHeight / 5 + squareSide;

        Rect src = new Rect(0, 0, exitBitmap.getWidth(), exitBitmap.getHeight());
        Rect dst = new Rect(left, top, right, bottom);
        canvas.drawBitmap(replayBitmap, src, dst, null);

        top = 4 * windowHeight / 5 - squareSide;
        bottom = 4 * windowHeight / 5 + squareSide;

        dst = new Rect(left, top, right, bottom);
        canvas.drawBitmap(exitBitmap, src, dst, null);

        view.setImageBitmap(bitmap);

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float y = event.getY();
                if (y >= windowHeight / 2) {
                    goBackToMenu();
                } else {
                    playAgain();
                }
                return true;
            }
        });
    }

    private void playAgain() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("startPlayer", startPlayer);
        returnIntent.putExtra("result", "again");
        setResult(RESULT_OK, returnIntent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        goBackToMenu();
    }

    private void goBackToMenu() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", "close");
        setResult(RESULT_OK, returnIntent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out);
    }
}
