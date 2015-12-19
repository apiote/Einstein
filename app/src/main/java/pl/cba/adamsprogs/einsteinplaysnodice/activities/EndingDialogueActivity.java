package pl.cba.adamsprogs.einsteinplaysnodice.activities;

import android.content.*;
import android.graphics.*;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
    private static final int NOT_FOUND = -1;

    private int startPlayer;
    private int winner;

    private final Context context = this;

    private ResultsFile resultsFile;

    private ImageView view;

    private int windowWidth, windowHeight;
    private int boundsHeight;

    private String whoWon;

    private Paint p;

    private Bitmap bitmap;
    private Canvas canvas;

    @NonNull
    private int[] playerColours = new int[2];
    private int textColour;
    private String[] results = new String[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpWindow();
        receiveData();
        swapStartPlayer();
        processResults();
        createWonString();
        setUpCanvas();
        drawEndingDialogue();
        setButtonsOnTouchListener();
    }

    private void setUpWindow() {
        setContentView(R.layout.ending_layout);
        view = (ImageView) findViewById(R.id.endingDialog);
        setUpToolbar();
        measureScreen();
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void measureScreen() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        windowWidth = metrics.widthPixels;
        windowHeight = metrics.heightPixels;
    }

    private void receiveData() {
        getDataFromIntent();
        assertLastRoundDataOK();
    }

    private void getDataFromIntent() {
        winner = getIntent().getIntExtra("winner", -1);
        startPlayer = getIntent().getIntExtra("startPlayer", -1);
    }

    private void assertLastRoundDataOK() {
        if (winner == NOT_FOUND || startPlayer == NOT_FOUND)
            finish();
    }

    private void swapStartPlayer() {
        ++startPlayer;
        startPlayer %= 2;
    }

    private void processResults() {
        resultsFile = new ResultsFile(context);
        incrementResultsInFile();
        getResultsFromFile();
    }

    private void incrementResultsInFile() {
        resultsFile.increment(winner);
        resultsFile.apply();
    }

    private void getResultsFromFile() {
        results = resultsFile.getResults();
    }

    private void setUpCanvas() {
        initialiseCanvas();
        initialiseColours();
        createPaint();
        createResultsStringBounds();
    }

    private void initialiseCanvas() {
        bitmap = Bitmap.createBitmap(windowWidth, windowHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    private void initialiseColours() {
        playerColours[Player.COLOUR_DARK] = getColour(this, R.color.dark);
        playerColours[Player.COLOUR_LIGHT] = getColour(this, R.color.light);
        textColour = getColour(context, R.color.text);
    }

    private void createPaint() {
        p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(playerColours[winner]);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        p.setTypeface(tf);
        p.setTextAlign(Paint.Align.CENTER);
    }

    private void createResultsStringBounds() {
        p.setTextSize((2 * windowWidth * p.getTextSize()) / p.measureText(whoWon));
        Rect bounds = new Rect();
        p.getTextBounds(results[Player.COLOUR_LIGHT] + getString(R.string.resultsSeparator) + results[Player.COLOUR_DARK]
                , 0, results[Player.COLOUR_LIGHT].length() + 1 + results[Player.COLOUR_DARK].length(), bounds);
        boundsHeight = bounds.height();
    }

    private void createWonString() {
        if (winner == Player.COLOUR_LIGHT) {
            whoWon = getString(R.string.plLightWon);
        } else {
            whoWon = getString(R.string.plDarkWon);
        }
    }

    private void drawEndingDialogue() {
        printWonMessage();
        printResults();
        drawMirroredBitmap();
        drawButtons();
        view.setImageBitmap(bitmap);
    }

    private void printWonMessage() {
        p.setTextSize((windowWidth * p.getTextSize()) / p.measureText(whoWon));
        canvas.drawText(whoWon, windowWidth / 2, windowHeight / 2 + p.getTextSize() * 2, p);
    }

    private void printResults() {
        p.setTextSize(2 * p.getTextSize());
        printResult(Player.COLOUR_LIGHT, -1);
        printResult(Player.COLOUR_DARK, 1);

        printResultSeparator();

    }

    private void printResult(int colourIndex, int width) {
        p.setColor(playerColours[colourIndex]);
        canvas.drawText(results[colourIndex], 3 * windowWidth / 4 + width * p.measureText(results[colourIndex]), windowHeight / 2 + boundsHeight / 2, p);
    }

    private void printResultSeparator() {
        p.setColor(textColour);
        canvas.drawText(getString(R.string.resultsSeparator), 3 * windowWidth / 4, windowHeight / 2 + boundsHeight / 2, p);
    }

    private void drawMirroredBitmap() {
        Matrix mx = new Matrix();
        mx.postRotate(180);
        Bitmap mirrorPost = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mx, false);
        canvas.drawBitmap(mirrorPost, 0, 0, p);
    }

    private void drawButtons() {
        Bitmap exitBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_close_black_48dp);
        Bitmap replayBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_replay_black_48dp);

        drawButtonBitmap(replayBitmap, 1);
        drawButtonBitmap(exitBitmap, 4);
    }

    private void drawButtonBitmap(@NonNull Bitmap bitmap, int height) {
        int left, right,
                top, bottom;

        int bitmapWidth = bitmap.getWidth(),
                bitmapHeight = bitmap.getHeight();

        left = windowWidth / 2 - bitmapWidth / 2;
        right = windowWidth / 2 + bitmapWidth / 2;
        top = height * windowHeight / 5 - bitmapHeight / 2;
        bottom = height * windowHeight / 5 + bitmapHeight / 2;

        Rect src = new Rect(0, 0, bitmapWidth, bitmapHeight);
        Rect dst = new Rect(left, top, right, bottom);

        canvas.drawBitmap(bitmap, src, dst, null);
    }

    private void setButtonsOnTouchListener() {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, @NonNull MotionEvent event) {
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

    private void goBackToMenu() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", "close");
        setResult(RESULT_OK, returnIntent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out);
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
}
