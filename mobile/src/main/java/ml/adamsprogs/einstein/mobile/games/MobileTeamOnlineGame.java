package ml.adamsprogs.einstein.mobile.games;

import ml.adamsprogs.einstein.engine.components.Player;
import ml.adamsprogs.einstein.mobile.activities.BoardActivity;
import ml.adamsprogs.einstein.mobile.activities.Einstein;
import ml.adamsprogs.einstein.mobile.components.MobileBoard;
import ml.adamsprogs.einstein.mobile.components.MobileDie;
import ml.adamsprogs.einstein.mobile.components.NetworkMobileBoard;
import ml.adamsprogs.einstein.mobile.components.NetworkMobileDie;

public class MobileTeamOnlineGame extends MobileGame {

    private Einstein application;
    private String team;

    public MobileTeamOnlineGame(BoardActivity context, String team, Einstein application) {
        super(context, team.equals("yellow") ? Player.COLOUR_LIGHT : Player.COLOUR_DARK, false);
        String socketBoard = application.receiveBoard();
        board = new NetworkMobileBoard((MobileBoard) board, application);
        board.fill(socketBoard);
        currentPlayer.setDie(new NetworkMobileDie((MobileDie) currentPlayer.getDie(), application));
        this.application = application;
        this.team = team;
    }

    public void start() {
        super.start();
        String active = application.receiveActive();
        currentPlayer.setActive(active.equals(team));
        if (active.equals(team)) {
            currentPlayer.waitForRoll();
            application.receiveSelectVotes((BoardActivity) context);
            if (!application.receiveVoteStoneNeeded()){
                //todo do nothing
            }
        } else
            swapControls();
    }

    @Override
    public void swapControls() {
        boolean won = false;
        while(!won) {
            String activeOrWon = application.receiveActiveOrWon();
            if (activeOrWon.split(" ")[0].equals("won")) {
                //todo require win dialogue
                won = true;
            } else {
                currentPlayer.setActive(activeOrWon.split(" ")[1].equals(team));
                if (activeOrWon.split(" ")[1].equals(team)) {
                    currentPlayer.waitForRoll();
                    if (application.receiveVoteStoneNeeded())
                        application.receiveSelectVotes((BoardActivity) context);
                }
            }
        }
    }
}
