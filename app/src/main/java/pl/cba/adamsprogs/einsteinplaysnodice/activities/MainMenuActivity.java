package pl.cba.adamsprogs.einsteinplaysnodice.activities;

import android.content.*;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.*;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.google.android.gms.ads.*;

import pl.cba.adamsprogs.einsteinplaysnodice.R;
import pl.cba.adamsprogs.einsteinplaysnodice.components.Player;
import pl.cba.adamsprogs.einsteinplaysnodice.utilities.ResultsFile;

import static pl.cba.adamsprogs.einsteinplaysnodice.utilities.Utilities.*;

public class MainMenuActivity extends AppCompatActivity {
    private final static String PREFS_NAME = "EINSTEIN_SETTINGS";
    private final static String isFirstRun = "isFirstRun";

    private SharedPreferences settings;

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
        setUpAd();
        displayResults();
        applyTypeface();

        setResetOnClickListener();
    }

    private void setUpWindow() {
        setContentView(pl.cba.adamsprogs.einsteinplaysnodice.R.layout.menu_layout);
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

    private void setUpAd() {
        AdView mAdView = (AdView) findViewById(R.id.adView_menu);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
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
        resetButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
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
            }
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
        settings = getSharedPreferences(PREFS_NAME, 0);
        TextView popUp = setUpPopUp();
        addPopUpText(popUp);
        addPopUpPadding(popUp);
        if (settings.getBoolean(isFirstRun, true)) {
            showPopUp(popUp);
            createNewResults();
        }
    }

    private void createNewResults() {
        resultsFile.clear();
        resultsFile.apply();
    }

    @NonNull
    private TextView setUpPopUp() {
        TextView tv = new TextView(this);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setTextSize(14);
        return tv;
    }

    private void addPopUpText(@NonNull TextView popUp) {
        String html = getString(R.string.cookies_message) + "<br /><a href='https://www.google.com/about/company/user-consent-policy.html'>" + getString(R.string.see_details) + "</a>";
        popUp.setText(Html.fromHtml(html));
    }

    private void addPopUpPadding(@NonNull TextView popUp) {
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        popUp.setPadding(padding, 0, padding, 0);
    }

    private void showPopUp(TextView popUp) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.Cookies))
                .setView(popUp)
                .setNeutralButton(getString(R.string.close_message), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        settings.edit().putBoolean(isFirstRun, false).apply();
                    }
                }).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpAd();
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
