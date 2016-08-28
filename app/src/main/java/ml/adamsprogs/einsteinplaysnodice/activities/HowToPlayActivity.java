package ml.adamsprogs.einsteinplaysnodice.activities;

import android.graphics.Typeface;
import android.support.v7.app.*;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import ml.adamsprogs.einsteinplaysnodice.R;


public class HowToPlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ml.adamsprogs.einsteinplaysnodice.R.layout.htp_layout);

        setUpToolbar();
        setTitleTypeface();
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setTitleTypeface() {
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        TextView htpTitle = (TextView) findViewById(R.id.htpTitle);
        htpTitle.setTypeface(tf);
    }
}
