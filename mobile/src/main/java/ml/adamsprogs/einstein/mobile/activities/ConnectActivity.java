package ml.adamsprogs.einstein.mobile.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import ml.adamsprogs.einstein.R;

import java.io.*;
import java.net.Socket;

public class ConnectActivity extends AppCompatActivity {
    private Button startButton;
    private EditText serverAddressEdit;
    private EditText serverPortEdit;
    private EditText numberOfPlayersEdit;
    private Switch newGameSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        startButton = (Button) findViewById(R.id.startButton);
        serverAddressEdit = (EditText) findViewById(R.id.serverAddress);
        serverPortEdit = (EditText) findViewById(R.id.serverPort);
        numberOfPlayersEdit = (EditText) findViewById(R.id.numberOfPlayers);
        newGameSwitch = (Switch) findViewById(R.id.newGameSwitch);

        startButton.setOnClickListener((v) -> {
            try {
                Socket socket = new Socket(String.valueOf(serverAddressEdit.getText()),
                        Integer.parseInt(String.valueOf(serverPortEdit.getText())));
                PrintWriter osw = new PrintWriter(socket.getOutputStream());
                if (newGameSwitch.isChecked())
                    osw.println("create " + numberOfPlayersEdit.getText());
                else
                    osw.println("join");

                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = br.readLine();
                if(response.split(" ")[0].equals("error")){
                    System.out.println(response);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        newGameSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                numberOfPlayersEdit.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE));
    }
}
