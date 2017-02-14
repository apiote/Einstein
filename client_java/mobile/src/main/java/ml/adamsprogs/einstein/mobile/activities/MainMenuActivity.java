package ml.adamsprogs.einstein.mobile.activities;

import android.content.*;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.*;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.*;
import android.view.*;
import android.widget.*;

import ml.adamsprogs.einstein.R;
import ml.adamsprogs.einstein.mobile.components.MobilePlayer;
import ml.adamsprogs.einstein.mobile.utilities.ResultsFile;

import static ml.adamsprogs.einstein.mobile.utilities.Utilities.*;

public class MainMenuActivity extends AppCompatActivity {

    private final Context context = this;

    private ResultsFile resultsFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpWindow();

        setUpToolbar();
        applyTypeface();

        resultsFile = new ResultsFile(context);
        clearResults();
    }

    private void setUpWindow() {
        setContentView(ml.adamsprogs.einstein.R.layout.menu_layout);
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
    }

    private void clearResults() {
        resultsFile.clear();
        resultsFile.apply();
    }

    public void change(@NonNull View v) {
        Intent intent = null;

        if (v.getId() == R.id.playBtn)
            intent = new Intent(this, ChooseGameActivity.class);
        if (v.getId() == R.id.htpBtn)
            intent = new Intent(this, HowToPlayActivity.class);

        startActivity(intent);
    }
}
