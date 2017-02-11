package ml.adamsprogs.einstein.mobile.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import ml.adamsprogs.einstein.R;

public class ConnectActivity extends AppCompatActivity {
    private Button startButton;
    private EditText serverAddressEdit;
    private EditText serverPortEdit;
    private EditText numberOfPlayersEdit;
    private Switch newGameSwitch;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        context = this;

        startButton = (Button) findViewById(R.id.startButton);
        serverAddressEdit = (EditText) findViewById(R.id.serverAddress);
        serverPortEdit = (EditText) findViewById(R.id.serverPort);
        numberOfPlayersEdit = (EditText) findViewById(R.id.numberOfPlayers);
        newGameSwitch = (Switch) findViewById(R.id.newGameSwitch);

        startButton.setOnClickListener(v -> {
            String response = ((Einstein) getApplication()).connectToGame(
                    String.valueOf(serverAddressEdit.getText()), String.valueOf(serverPortEdit.getText()),
                    newGameSwitch.isChecked() ? String.valueOf(numberOfPlayersEdit.getText()) : null);
            if (response.split(" ")[0].equals("error")) {
                //todo show
            } else {
                //todo wait for `game started`
                Intent intent = new Intent(context, BoardActivity.class);
                //todo add networking options to intent
                startActivity(intent);
            }
        });
        newGameSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                numberOfPlayersEdit.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE));
    }
}
