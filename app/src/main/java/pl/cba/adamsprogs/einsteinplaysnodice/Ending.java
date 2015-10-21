package pl.cba.adamsprogs.einsteinplaysnodice;

import android.content.*;
import android.graphics.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.ImageView;

import java.io.*;

import static pl.cba.adamsprogs.einsteinplaysnodice.Utilities.getColour;

public class Ending extends AppCompatActivity {
    private int startPlayer;

    private Context context;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ending_layout);

        int winner = getIntent().getIntExtra("winner", -1);
        startPlayer = getIntent().getIntExtra("startPlayer", -1);

        if (winner == -1 || startPlayer == -1)
            finish();

        context = this;

        int[] res = getFile();
        ++res[winner];
        saveFile(res);

        ImageView view = (ImageView) findViewById(R.id.endingDialog);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int windowWidth = metrics.widthPixels;
        final int windowHeight = metrics.heightPixels;
        Bitmap bitmap = Bitmap.createBitmap(windowWidth, windowHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Matrix mx = new Matrix();
        mx.postRotate(180);
        Bitmap mirrorPre = Bitmap.createBitmap(windowWidth, windowHeight, Bitmap.Config.ARGB_8888);
        Canvas mirrorCanvas = new Canvas(mirrorPre);

        int[] Col = {getColour(this, R.color.secondary), getColour(this, R.color.primary)};
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Col[winner]);

        String onceAgain, goToMenu, won, lRes = res[0] + "", dRes = res[1] + "";
        onceAgain = getString(R.string.playOnceAgain);
        goToMenu = getString(R.string.goToMenu);


        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        p.setTypeface(tf);
        p.setTextAlign(Paint.Align.CENTER);

        if (winner == 0) {
            won = getString(R.string.plLightWon);
        } else {
            won = getString(R.string.plDarkWon);
        }

        p.setTextSize((windowWidth * p.getTextSize()) / p.measureText(won));
        canvas.drawText(won, windowWidth / 2, windowHeight / 2 + p.getTextSize() * 2, p);
        mirrorCanvas.drawText(won, windowWidth / 2, windowHeight / 2 + p.getTextSize() * 2, p);

        p.setColor(getColour(context, R.color.text));

        p.setTextSize((windowWidth * p.getTextSize()) / p.measureText(onceAgain));
        canvas.drawText(onceAgain, windowWidth / 2, (windowHeight / 5) + (p.getTextSize()), p);
        mirrorCanvas.drawText(onceAgain, windowWidth / 2, (4 * windowHeight / 5) + (p.getTextSize()), p);

        p.setTextSize((windowWidth * p.getTextSize()) / p.measureText(goToMenu));
        canvas.drawText(goToMenu, windowWidth / 2, (4 * windowHeight / 5) + p.getTextSize(), p);
        mirrorCanvas.drawText(goToMenu, windowWidth / 2, (windowHeight / 5) + (p.getTextSize()), p);

        p.setTextSize(86);

        p.setColor(Col[0]);
        canvas.drawText(lRes, windowWidth / 2 - p.measureText(lRes), (float) (windowHeight / 2 + p.getTextSize()*.9), p);
        mirrorCanvas.drawText(lRes, windowWidth / 2 - p.measureText(lRes), (float) (windowHeight / 2 + p.getTextSize()*.9), p);

        p.setColor(getColour(context, R.color.text));
        canvas.drawText(getString(R.string.resultsSeparator), windowWidth / 2, (float) (windowHeight / 2 + p.getTextSize()*.9), p);
        mirrorCanvas.drawText(getString(R.string.resultsSeparator), windowWidth / 2, (float) (windowHeight / 2 + p.getTextSize()*.9), p);

        p.setColor(Col[1]);
        canvas.drawText(dRes, windowWidth / 2 + p.measureText(lRes), (float) (windowHeight / 2 + p.getTextSize()*.9), p);
        mirrorCanvas.drawText(dRes, windowWidth / 2 + p.measureText(lRes), (float) (windowHeight / 2 + p.getTextSize()*.9), p);

        Bitmap mirrorPost = Bitmap.createBitmap(mirrorPre, 0, 0, mirrorPre.getWidth(), mirrorPre.getHeight(), mx, false);
        canvas.drawBitmap(mirrorPost, 0, 0, p);

        view.setImageBitmap(bitmap);

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float y = event.getY();
                if (y >= windowHeight / 2) {
                    Intent returnIntent = new Intent();
                    setResult(-1, returnIntent);
                    finish();
                } else {
                    ++startPlayer;
                    startPlayer %= 2;
                    intent = new Intent(context, Board.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.putExtra("startPlayer", startPlayer);
                    startActivity(intent);
                }
                return true;
            }
        });
    }

    public int[] getFile() {
        int[] res = {0, 0};
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(
                    openFileInput("EinsteinResults")));
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
            FileOutputStream fos = openFileOutput("EinsteinResults", Context.MODE_PRIVATE);
            fos.write(res.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {

    }
}
