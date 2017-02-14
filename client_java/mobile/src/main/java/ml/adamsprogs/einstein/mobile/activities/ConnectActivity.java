package ml.adamsprogs.einstein.mobile.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import ml.adamsprogs.einstein.R;

public class ConnectActivity extends AppCompatActivity {
    private Button startButton;
    private EditText serverAddressEdit;
    private EditText serverPortEdit;
    private EditText numberOfPlayersEdit;
    private Switch newGameSwitch;
    private TextView errorTextView;
    private Context context;
    private Einstein application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        setUpToolbar();

        application = (Einstein) getApplication();

        context = this;

        startButton = (Button) findViewById(R.id.startButton);
        serverAddressEdit = (EditText) findViewById(R.id.serverAddress);
        serverPortEdit = (EditText) findViewById(R.id.serverPort);
        numberOfPlayersEdit = (EditText) findViewById(R.id.numberOfPlayers);
        newGameSwitch = (Switch) findViewById(R.id.newGameSwitch);
        errorTextView = (TextView) findViewById(R.id.errorText);

        startButton.setOnClickListener(v -> {
            try {
                String team = application.connectToGame(
                        String.valueOf(serverAddressEdit.getText()), String.valueOf(serverPortEdit.getText()),
                        newGameSwitch.isChecked() ? String.valueOf(numberOfPlayersEdit.getText()) : null);
                application.waitForGameBegin();
                Intent intent = new Intent(context, BoardActivity.class);
                intent.putExtra("online", true);
                intent.putExtra("team", team);
                startActivity(intent);
            } catch (IllegalStateException e) {
                switch (e.getMessage()){
                    case "already_exists":
                        errorTextView.setText(R.string.create_already_exists);
                        break;
                    case "invalid_count":
                        errorTextView.setText(R.string.create_invalid_count);
                        break;
                    case "full":
                        errorTextView.setText(R.string.join_full);
                        break;
                    case "not_started":
                        errorTextView.setText(R.string.join_not_started);
                        break;
                    case "already_joined":
                        errorTextView.setText(R.string.join_already_joined);
                        break;
                }
            }
        });

        newGameSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                numberOfPlayersEdit.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE));
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
}
