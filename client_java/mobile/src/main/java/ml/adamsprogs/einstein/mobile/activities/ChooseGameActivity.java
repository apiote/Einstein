package ml.adamsprogs.einstein.mobile.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.design.widget.Snackbar;
import android.widget.TextView;
import ml.adamsprogs.einstein.R;

public class ChooseGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_game);
        setUpToolbar();
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void change(View v) {
        if (v.getId() == R.id.localGameText) {
            startActivity(new Intent(this, BoardActivity.class));
        } else {
            if (isOnline())
                startActivity(new Intent(this, ConnectActivity.class));
            else {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_choose_game),
                        getString(R.string.no_internet_connection), Snackbar.LENGTH_LONG);
                ((TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text))
                        .setTextColor(Color.WHITE);
                snackbar.show();
            }

        }
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
