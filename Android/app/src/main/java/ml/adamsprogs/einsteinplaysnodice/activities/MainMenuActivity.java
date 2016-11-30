package ml.adamsprogs.einsteinplaysnodice.activities;

import android.content.*;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.*;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.*;
import android.view.*;
import android.widget.*;

import ml.adamsprogs.einsteinplaysnodice.R;
import ml.adamsprogs.einsteinplaysnodice.components.Player;
import ml.adamsprogs.einsteinplaysnodice.utilities.ResultsFile;

import static ml.adamsprogs.einsteinplaysnodice.utilities.Utilities.*;

public class MainMenuActivity extends AppCompatActivity {
    private final static String PREFS_NAME = "EINSTEIN_SETTINGS";
    private final static String isFirstRun = "isFirstRun";

    private final Context context = this;

    private ResultsFile resultsFile;

    private Button resetButton;
    private TextView lightPlayerResult;
    private TextView darkPlayerResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpWindow();

        setUpToolbar();
        displayResults();
        applyTypeface();

        setResetOnClickListener();
    }

    private void setUpWindow() {
        setContentView(ml.adamsprogs.einsteinplaysnodice.R.layout.menu_layout);
        lightPlayerResult = (TextView) findViewById(R.id.resultLight);
        darkPlayerResult = (TextView) findViewById(R.id.resultDark);
        resetButton = (Button) findViewById(R.id.resetButton);
    }

    private void displayResults() {
        resultsFile = new ResultsFile(context);
        String[] r = resultsFile.getResults();
        lightPlayerResult.setText(r[Player.COLOUR_LIGHT]);
        darkPlayerResult.setText(r[Player.COLOUR_DARK]);
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void applyTypeface() {
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        TextView playBtn = (TextView) findViewById(R.id.playBtn);
        TextView htpBtn = (TextView) findViewById(R.id.htpBtn);

        playBtn.setTypeface(tf);
        htpBtn.setTypeface(tf);
        lightPlayerResult.setTypeface(tf);
        darkPlayerResult.setTypeface(tf);
    }

    private void setResetOnClickListener() {
        resetButton.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setResetElevation(8);
                    return false;
                case MotionEvent.ACTION_UP:
                    setResetElevation(2);
                    clearResults();
                    displayResults();
                    return false;
            }
            return false;
        });
    }

    private void setResetElevation(int elevation) {
        if (isRunningLollipopOrNewer()) {
            Log.i("MainMenu", "setting elevation");
            resetButton.setElevation(elevation);
        }
    }

    private void clearResults() {
        resultsFile.clear();
        resultsFile.apply();
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.getBoolean(isFirstRun, true))
            createNewResults();
    }

    private void createNewResults() {
        resultsFile.clear();
        resultsFile.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resultsFile.refresh();
        displayResults();
    }

    public void change(@NonNull View v) {
        Intent intent = null;

        if (v.getId() == R.id.playBtn)
            intent = new Intent(this, BoardActivity.class);
        if (v.getId() == R.id.htpBtn)
            intent = new Intent(this, HowToPlayActivity.class);

        startActivity(intent);
    }
}
