package ml.adamsprogs.einstein.mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.*;
import android.util.Log;
import android.widget.Toast;

import ml.adamsprogs.einstein.R;
import ml.adamsprogs.einstein.mobile.components.MobilePlayer;
import ml.adamsprogs.einstein.mobile.games.*;

import static ml.adamsprogs.einstein.engine.utils.Utils.stringToPoint;

public class BoardActivity extends AppCompatActivity implements MobileGame.OnWinListener, MobileGame.OnErrorExit {
    private MobileGame mobileGame;
    private int startPlayer;
    private static final int REQUEST_ENDING_DIALOGUE = 101;
    private Einstein application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.board_layout);

        application = (Einstein) getApplication();

        createNewGame(getIntent());
    }

    private void createNewGame(@NonNull Intent intent) {
        boolean online = intent.getBooleanExtra("online", false);
        if (online)
            initialiseNewTeamOnlineGame(intent.getStringExtra("team"));
        else
            initialiseNewLocalGame(intent);
        startGame();
    }

    private void initialiseNewTeamOnlineGame(String team) {
        try {
            mobileGame = new MobileTeamOnlineGame(this, team, application);
        } catch (IllegalStateException e) {
            Toast.makeText(this, getString(R.string.illegalStateCaught), Toast.LENGTH_SHORT).show();
            Log.wtf("IllegalStateException", e.getMessage());
            e.printStackTrace();
        }
    }

    private void initialiseNewLocalGame(@NonNull Intent intent) {
        startPlayer = intent.getIntExtra("startPlayer", MobilePlayer.COLOUR_LIGHT);
        try {
            mobileGame = new MobileLocalGame(this, startPlayer);
        } catch (IllegalStateException e) {
            Toast.makeText(this, getString(R.string.illegalStateCaught), Toast.LENGTH_SHORT).show();
            Log.wtf("IllegalStateException", e.getMessage());
            e.printStackTrace();
        }
    }

    private void startGame() {
        mobileGame.start();
    }

    @Override
    public void onWin(int winner) {
        Intent endingIntent = createEndingIntent(winner);
        mobileGame.destroy();
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
        mobileGame.destroy();
        super.onBackPressed();
    }

    @Override
    public void onError(Exception e) {
        mobileGame.destroy();
        finish();
    }

    public void acknowledgeNewSelectVote(int a, int b, boolean isFinal) {
        //todo draw votes
        if (isFinal) {
            //todo act according to vote result
            /*hintMove(stringToPoint(result));
            selectedStone = stones.get(result);*/
        }
    }

    public void acknowledgeNewMoveVote(int a, int b, boolean isFinal) {
        //todo draw votes
        if(isFinal){
            //todo act accordingly to vote result
            /*target = stringToPoint(result);
            moveStone(target);
            draw();
            movable = false;
            targetablePoints = null;
            clearHint();
            selectableStones = null;
            onStoneMoved.onStoneMoved();*/
        }
    }
}
