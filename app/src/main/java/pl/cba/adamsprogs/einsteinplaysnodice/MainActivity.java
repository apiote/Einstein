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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    SharedPreferences settings;
    final static String PREFS_NAME="EPND_SETTINGS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }

        setContentView(R.layout.menu_layout);

        AdView mAdView = (AdView) findViewById(R.id.adView_menu);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Comfortaa.ttf");
        TextView playBtn = (TextView) findViewById(R.id.playBtn);
        TextView htpBtn = (TextView) findViewById(R.id.htpBtn);
        TextView rL = (TextView) findViewById(R.id.resultLight);
        TextView rD = (TextView) findViewById(R.id.resultDark);
        playBtn.setTypeface(tf);
        htpBtn.setTypeface(tf);
        rL.setTypeface(tf);
        rD.setTypeface(tf);

        displayResults();

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            final Button reset = (Button) findViewById(R.id.resetRes);
            reset.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("NewApi")
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch(motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            reset.setElevation(8);
                            return false;
                        case MotionEvent.ACTION_UP:
                            reset.setElevation(2);
                            String res = "";
                            try {
                                FileOutputStream fos = openFileOutput("ClokResults", Context.MODE_PRIVATE);
                                fos.write(res.getBytes());
                                fos.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            displayResults();
                            return false;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        settings = getSharedPreferences(PREFS_NAME, 0);
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        String html=getString(R.string.cookies_message)+"<br /><a href='https://www.google.com/about/company/user-consent-policy.html'>" + getString(R.string.see_details) + "</a>";
        TextView tv  = new TextView(this);
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
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdView mAdView = (AdView) findViewById(R.id.adView_menu);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        displayResults();
    }

    public void change(View v) {
        if (v.getId() == R.id.playBtn) {
            Intent intent = new Intent(this, Board.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.htpBtn) {
            Intent intent = new Intent(this, HowToPlay.class);
            startActivity(intent);
        }
    }

    public void displayResults() {
        int[] r = getFile();
        TextView rL = (TextView) findViewById(R.id.resultLightNum);
        TextView rD = (TextView) findViewById(R.id.resultDarkNum);
        rL.setText("");
        rL.append(r[0] + "");
        rD.setText("");
        rD.append(r[1] + "");
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Comfortaa.ttf");
        rL.setTypeface(tf);
        rD.setTypeface(tf);
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
}
