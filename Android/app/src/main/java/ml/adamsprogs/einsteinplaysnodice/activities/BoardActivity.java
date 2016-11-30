package ml.adamsprogs.einsteinplaysnodice.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.*;
import android.util.Log;
import android.widget.Toast;

import ml.adamsprogs.einsteinplaysnodice.R;
import ml.adamsprogs.einsteinplaysnodice.components.Player;
import ml.adamsprogs.einsteinplaysnodice.games.*;

public class BoardActivity extends AppCompatActivity implements ServerGame.OnWinListener, ServerGame.OnErrorExit{
    private ServerGame serverGame;
    private int startPlayer;
    private static final int REQUEST_ENDING_DIALOGUE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.board_layout);

        createNewGame(getIntent());
    }

    private void createNewGame(@NonNull Intent intent) {
        initialiseNewLocalGame(intent);
        startLocalGame();
    }

    private void initialiseNewLocalGame(@NonNull Intent intent) {
        startPlayer = intent.getIntExtra("startPlayer", Player.COLOUR_LIGHT);
        try {
            serverGame = new LocalGame(this, startPlayer);
        } catch(IllegalStateException e){
            Toast.makeText(this, getString(R.string.illegalStateCaught), Toast.LENGTH_SHORT).show();
            Log.wtf("IllegalStateException", e.getMessage());
            e.printStackTrace();
        }
    }

    private void startLocalGame() {
        serverGame.start();
    }

    @Override
    public void onWin(int winner) {
        Intent endingIntent = createEndingIntent(winner);
        serverGame.destroy();
        goToEndingDialogue(endingIntent);
    }

    @NonNull
    private Intent createEndingIntent(int winner) {
        Intent endingIntent = new Intent(this, EndingDialogueActivity.class);
        endingIntent.putExtra("winner", winner);
        endingIntent.putExtra("startPlayer", startPlayer);
        return endingIntent;
    }

    private void goToEndingDialogue(Intent intent) {
        startActivityForResult(intent, REQUEST_ENDING_DIALOGUE);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        if (requestCode == REQUEST_ENDING_DIALOGUE) {
            decideNewRound(data);
        }
    }

    private void decideNewRound(@NonNull Intent data) {
        String result = data.getStringExtra("result");
        if (result.equals("close"))
            finish();
        else if (result.equals("again")) {
            createNewGame(data);
        }
    }

    @Override
    public void onBackPressed() {
        serverGame.destroy();
        super.onBackPressed();
    }

    @Override
    public void onError(Exception e) {
        serverGame.destroy();
        finish();
    }
}
