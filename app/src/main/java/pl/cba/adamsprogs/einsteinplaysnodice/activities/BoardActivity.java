package pl.cba.adamsprogs.einsteinplaysnodice.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.*;
import android.util.Log;

import pl.cba.adamsprogs.einsteinplaysnodice.R;
import pl.cba.adamsprogs.einsteinplaysnodice.components.Player;
import pl.cba.adamsprogs.einsteinplaysnodice.games.*;

public class BoardActivity extends AppCompatActivity implements ServerGame.OnWinListener {
    private ServerGame serverGame;
    private int startPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.board_layout);

        startPlayer = getIntent().getIntExtra("startPlayer", Player.COLOUR_LIGHT);
        serverGame = new LocalGame(this, startPlayer);
        serverGame.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //FIXME startPlayer
        if (requestCode == 101) {
            String result = data.getStringExtra("result");
            startPlayer = data.getIntExtra("startPlayer", Player.COLOUR_LIGHT);
            Log.i("OnActivityResult", "startPlayer=" + startPlayer);
            if (result.equals("close"))
                finish();
            else if (result.equals("again")) {
                serverGame.destroy();
                serverGame = new LocalGame(this, startPlayer);
                serverGame.start();
            }
        }
    }

    @Override
    public void onWin(int winner) {
        Intent endingIntent = new Intent(this, EndingDialogueActivity.class);
        endingIntent.putExtra("winner", winner);
        endingIntent.putExtra("startPlayer", startPlayer);
        startActivityForResult(endingIntent, 101);
    }
}
