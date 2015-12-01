package hamburg.haw.polyshift.Game;

import android.app.ProgressDialog;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

import hamburg.haw.polyshift.Tools.PHPConnector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class OfflineGameLoop{

    public boolean PlayerOnesTurn;
    public boolean RoundFinished;
    public boolean PlayerOnesGame;
    public static Thread game_status_thread;
    private String opponentID;
    private String opponentName;
    private String notificationGameID;

    public OfflineGameLoop(String PlayerOnesGame){
        RoundFinished = true;
        if(PlayerOnesGame.equals("yes")){
            this.PlayerOnesGame = true;
        }else{
            this.PlayerOnesGame = false;
        }
    }

    public void setRandomPlayer(){
        Random random = new Random();
        PlayerOnesTurn = random.nextBoolean();
    }

    public void update(Simulation simulation, final String opponentID,final String opponentName,final String notificationGameID){
        if(PlayerOnesTurn){
            simulation.player2.isLocked = true;
            if(simulation.player.isMovingRight || simulation.player.isMovingLeft || simulation.player.isMovingUp || simulation.player.isMovingDown){
                RoundFinished = false;
            }
            if(simulation.bump_detected) {
                simulation.bump_detected = false;
                PlayerOnesTurn = false;
            }else if(!RoundFinished || simulation.player.isLockedIn){
                if(!simulation.player.isMovingRight && !simulation.player.isMovingLeft && !simulation.player.isMovingUp && !simulation.player.isMovingDown){
                    RoundFinished = true;
                    PlayerOnesTurn = false;
                    simulation.player.isLocked = true;
                    if(!PlayerOnesGame) {
                        simulation.player2.isLocked = false;
                    }
                    simulation.player.isLockedIn = false;
                    PolyshiftActivity.statusUpdated = false;
                }
            }
        }
        if(!PlayerOnesTurn){
            simulation.player.isLocked = true;
            if(simulation.player2.isMovingRight || simulation.player2.isMovingLeft || simulation.player2.isMovingUp || simulation.player2.isMovingDown){
                RoundFinished = false;
            }
            if(simulation.bump_detected) {
                simulation.bump_detected = false;
                PlayerOnesTurn = true;
            }else if(!RoundFinished || simulation.player2.isLockedIn){
                if(!simulation.player2.isMovingRight && !simulation.player2.isMovingLeft && !simulation.player2.isMovingUp && !simulation.player2.isMovingDown){
                    RoundFinished = true;
                    PlayerOnesTurn = true;
                    simulation.player2.isLocked = true;
                    if(PlayerOnesGame) {
                        simulation.player.isLocked = false;
                    }
                    simulation.player2.isLockedIn = false;
                    PolyshiftActivity.statusUpdated = false;
                }
            }
        }
    }
}
