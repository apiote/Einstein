package pl.cba.adamsprogs.einsteinplaysnodice.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.*;

import pl.cba.adamsprogs.einsteinplaysnodice.R;
import pl.cba.adamsprogs.einsteinplaysnodice.components.Player;
import pl.cba.adamsprogs.einsteinplaysnodice.games.*;

public class BoardActivity extends AppCompatActivity implements ServerGame.OnWinListener {
    private ServerGame serverGame;
    private int startPlayer;
    private static final int REQUEST_ENDING_DIALOGUE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.board_layout);

        startPlayer = getIntent().getIntExtra("startPlayer", Player.COLOUR_LIGHT);
        serverGame = new LocalGame(this, startPlayer);
        serverGame.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENDING_DIALOGUE) {
            String result = data.getStringExtra("result");
            if (result.equals("close"))
                finish();
            else if (result.equals("again")) {
                serverGame.destroy();
                serverGame = new LocalGame(this, data.getIntExtra("startPlayer", Player.COLOUR_LIGHT));
                serverGame.start();
            }
        }
    }

    @Override
    public void onWin(int winner) {
        Intent endingIntent = new Intent(this, EndingDialogueActivity.class);
        endingIntent.putExtra("winner", winner);
        endingIntent.putExtra("startPlayer", startPlayer);
        startActivityForResult(endingIntent, REQUEST_ENDING_DIALOGUE);
    }
}
