package pl.cba.adamsprogs.einsteinplaysnodice.activities;

import android.content.*;
import android.graphics.Typeface;
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
    private SharedPreferences settings;
    private final static String PREFS_NAME = "EINSTEIN_SETTINGS";
    Context context = this;
    Button resetButton;
    ResultsFile resultsFile;
    TextView rL;
    TextView rD;
    Typeface tf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(pl.cba.adamsprogs.einsteinplaysnodice.R.layout.menu_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        resultsFile = new ResultsFile(context);
        resetButton = (Button) findViewById(R.id.resetButton);

        AdView mAdView = (AdView) findViewById(R.id.adView_menu);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        TextView playBtn = (TextView) findViewById(R.id.playBtn);
        TextView htpBtn = (TextView) findViewById(R.id.htpBtn);
        rL = (TextView) findViewById(R.id.resultLight);
        rD = (TextView) findViewById(R.id.resultDark);

        displayResults();

        playBtn.setTypeface(tf);
        htpBtn.setTypeface(tf);
        rL.setTypeface(tf);
        rD.setTypeface(tf);

        resetButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setResetElevation(8);
                        return false;
                    case MotionEvent.ACTION_UP:
                        setResetElevation(2);
                        resultsFile.clear();
                        resultsFile.apply();
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

    @Override
    public void onStart() {
        super.onStart();
        settings = getSharedPreferences(PREFS_NAME, 0);
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        String html = getString(R.string.cookies_message) + "<br /><a href='https://www.google.com/about/company/user-consent-policy.html'>" + getString(R.string.see_details) + "</a>";
        TextView tv = new TextView(this);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(Html.fromHtml(html));
        tv.setPadding(padding, 0, padding, 0);
        tv.setTextSize(14);
        if (settings.getBoolean("isFirstRun", true)) {
            new AlertDialog.Builder(this)
                    .setTitle("Cookies")
                    .setView(tv)
                    .setNeutralButton(getString(R.string.close_message), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            settings.edit().putBoolean("isFirstRun", false).apply();
                        }
                    }).show();
            resultsFile.delete();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdView mAdView = (AdView) findViewById(R.id.adView_menu);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        resultsFile.refresh();
        displayResults();
    }

    public void change(View v) {
        if (v.getId() == R.id.playBtn) {
            Intent intent = new Intent(this, BoardActivity.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.htpBtn) {
            Intent intent = new Intent(this, HowToPlayActivity.class);
            startActivity(intent);
        }
    }

    public void displayResults() {
        int[] r = resultsFile.getResults();
        rL.setText("");
        rL.append(r[Player.COLOUR_LIGHT] + "");
        rD.setText("");
        rD.append(r[Player.COLOUR_DARK] + "");
    }
}
