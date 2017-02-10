package ml.adamsprogs.einstein.mobile.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import ml.adamsprogs.einstein.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectActivity extends AppCompatActivity {
    private Button startButton;
    private EditText serverAddressEdit;
    private EditText serverPortEdit;
    private EditText numberOfPlayersEdit;
    private Switch newGameSwitch;

    class RetrieveFeedTask extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... params) {
            try {
                Socket socket = new Socket(String.valueOf(params[0]),
                        Integer.parseInt(String.valueOf(params[1])));
                PrintWriter osw = new PrintWriter(socket.getOutputStream());
                if (!params[2].equals(""))
                    osw.println("create " + params[2]);
                else
                    osw.println("join");

                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                return br.readLine();
            } catch (Exception e) {
                this.exception = e;

                return null;
            }
        }

        protected void onPostExecute(String response) {
            if(response != null && response.split(" ")[0].equals("error")) {
                System.out.println(response);
            }
        }
    }

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
            new RetrieveFeedTask().execute(String.valueOf(serverAddressEdit.getText()),
                    String.valueOf(serverPortEdit.getText()), newGameSwitch.isChecked()?
                            String.valueOf(numberOfPlayersEdit.getText()): "");
        });

        newGameSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                numberOfPlayersEdit.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE));
    }
}
