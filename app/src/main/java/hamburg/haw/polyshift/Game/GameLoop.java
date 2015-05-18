package hamburg.haw.polyshift.Game;

import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

import hamburg.haw.polyshift.Tools.PHPConnector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class GameLoop{

    public boolean PlayerOnesTurn;
    public boolean RoundFinished;
    public boolean PlayerOnesGame;
    public static Thread game_status_thread;

    public GameLoop(String PlayerOnesGame){
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
        updateGameStatus();
    }

    public void update(Simulation simulation, final String opponentID,final String opponentName){
        if(PlayerOnesTurn){
            simulation.player2.isLocked = true;
            if(simulation.player.isMovingRight || simulation.player.isMovingLeft || simulation.player.isMovingUp || simulation.player.isMovingDown){
                RoundFinished = false;
            }
            if(!RoundFinished || simulation.player2.isLockedIn){
                if(!simulation.player.isMovingRight && !simulation.player.isMovingLeft && !simulation.player.isMovingUp && !simulation.player.isMovingDown){
                    RoundFinished = true;
                    PlayerOnesTurn = false;
                    simulation.player.isLocked = true;
                    if(!PlayerOnesGame) {
                        simulation.player2.isLocked = false;
                    }
                    simulation.player.isLockedIn = false;
                    updateGameStatus();
                    GameSync.uploadSimulation(simulation);
                    Log.i("GCM", opponentID);
                    Log.i("GCM", opponentName);
                    String msg = opponentName + " hat einen Zug gemacht";
                    GameSync.SendChangeNotification(opponentID,msg);
                }
            }
        }
        if(!PlayerOnesTurn){
            simulation.player.isLocked = true;
            if(simulation.player2.isMovingRight || simulation.player2.isMovingLeft || simulation.player2.isMovingUp || simulation.player2.isMovingDown){
                RoundFinished = false;
            }
            if(!RoundFinished || simulation.player2.isLockedIn){
                if(!simulation.player2.isMovingRight && !simulation.player2.isMovingLeft && !simulation.player2.isMovingUp && !simulation.player2.isMovingDown){
                    RoundFinished = true;
                    PlayerOnesTurn = true;
                    simulation.player2.isLocked = true;
                    if(PlayerOnesGame) {
                        simulation.player.isLocked = false;
                    }
                    simulation.player2.isLockedIn = false;
                    updateGameStatus();
                    GameSync.uploadSimulation(simulation);
                    Log.i("GCM", "aufruf sendnotification round finished player two");
                    Log.i("GCM", opponentID);
                    Log.i("GCM", opponentName);
                    String msg = opponentName + " hat einen Zug gemacht";
                    GameSync.SendChangeNotification(opponentID,msg);
                }
            }
        }
    }
    public void updateGameStatus(){
        class GameStatusThread extends Thread{
            public void run(){
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("playerOnesTurn", "" + ((PlayerOnesTurn) ? 1 : 0)));
                PHPConnector.doRequest(nameValuePairs, "update_game.php");
            }
        }
        game_status_thread = new GameStatusThread();
        game_status_thread.start();
        try {
            long waitMillis = 10000;
            while (game_status_thread.isAlive()) {
                game_status_thread.join(waitMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
