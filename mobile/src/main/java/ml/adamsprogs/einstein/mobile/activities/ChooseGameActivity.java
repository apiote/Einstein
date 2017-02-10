package ml.adamsprogs.einstein.mobile.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import ml.adamsprogs.einstein.R;

public class ChooseGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_game);

    }

    public void change(View v) {
        if (v.getId() == R.id.localGameText) {
            startActivity(new Intent(this, BoardActivity.class));
        } else {
            startActivity(new Intent(this, ConnectActivity.class));
        }
    }
}
