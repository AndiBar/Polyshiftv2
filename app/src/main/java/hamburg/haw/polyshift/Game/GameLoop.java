package hamburg.haw.polyshift.Game;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

import hamburg.haw.polyshift.R;
import hamburg.haw.polyshift.Tools.PHPConnector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class GameLoop{

    public boolean PlayerOnesTurn;
    public boolean RoundFinished;
    public boolean PlayerOnesGame;
    public Thread game_status_thread;
    private String opponentID;
    private String opponentName;
    private String notificationGameID;
    private boolean move_again = false;
    private Activity mContext;

    public GameLoop(String PlayerOnesGame, Activity activity){
        mContext = activity;
        game_status_thread = new Thread();
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

    public void update(final Simulation simulation, final String opponentID,final String opponentName,final String notificationGameID){
        //Wenn Spieler 1 ander Reihe ist, prüfe ob er sich bewegt. Wenn ja läuft die Runde noch
        if(PlayerOnesTurn){
            simulation.player2.isLocked = true;
            if(simulation.player.isMovingRight || simulation.player.isMovingLeft || simulation.player.isMovingUp || simulation.player.isMovingDown){
                RoundFinished = false;
            }
            //Wurde ein Anstoßen eines anderen Spielers festgestellt, ist dieser and der Reihe, ohne dass die Runde von Spieler 1 beendet wird
            if(simulation.bump_detected) {
                simulation.bump_detected = false;
                move_again = true;
                PlayerOnesTurn = false;
            }else if(!RoundFinished || simulation.player.isLockedIn){
                //Wenn sich Spieler 1 nicht mehr bewegt, ist die Runde beendet
                if(!simulation.player.isMovingRight && !simulation.player.isMovingLeft && !simulation.player.isMovingUp && !simulation.player.isMovingDown){
                    RoundFinished = true;
                    PlayerOnesTurn = false;
                    simulation.player.isLocked = true;
                    if(!PlayerOnesGame) {
                        simulation.player2.isLocked = false;
                    }
                    simulation.player.isLockedIn = false;
                    PolyshiftActivity.statusUpdated = false;
                    class GameStatusThread extends Thread{
                        public void run(){
                            GameSync.uploadSimulation(simulation);
                            String msg = opponentName + mContext.getString(R.string.has_done_a_move);
                            GameSync.SendChangeNotification(opponentID, msg, notificationGameID, PolyshiftActivity.class.getName());
                            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                            nameValuePairs.add(new BasicNameValuePair("playerOnesTurn", "" + ((PlayerOnesTurn) ? 1 : 0)));
                            PHPConnector.doRequest(nameValuePairs, "update_game.php");
                        }
                    }
                    game_status_thread = new GameStatusThread();
                    game_status_thread.start();
                    if(!move_again) {
                        simulation.allLocked = true;
                    }else{
                    //Es werden keine Obkjekte blockiert, da Spieler 2 noch einmal dran ist.
                    //Als letztbewegtes Objekt wird ein Polynomino gesetzt, da Spieler 2 nur seinen Spieler noch einmal bewegen darf
                        move_again = false;
                        simulation.lastMovedObject = simulation.lastMovedPolynomino;
                    }
                }
            }
        }
        //Wenn Spieler 2 ander Reihe ist, prüfe ob er sich bewegt. Wenn ja läuft die Runde noch
        if(!PlayerOnesTurn){
            simulation.player.isLocked = true;
            if(simulation.player2.isMovingRight || simulation.player2.isMovingLeft || simulation.player2.isMovingUp || simulation.player2.isMovingDown){
                RoundFinished = false;
            }
            //Wurde ein Anstoßen eines anderen Spielers festgestellt, ist dieser and der Reihe, ohne dass die Runde von Spieler 2 beendet wird
            if(simulation.bump_detected) {
                simulation.bump_detected = false;
                move_again = true;
                PlayerOnesTurn = true;
            }else if(!RoundFinished || simulation.player2.isLockedIn){
                //Wenn sich Spieler 2 nicht mehr bewegt, ist die Runde beendet
                if(!simulation.player2.isMovingRight && !simulation.player2.isMovingLeft && !simulation.player2.isMovingUp && !simulation.player2.isMovingDown){
                    RoundFinished = true;
                    PlayerOnesTurn = true;
                    simulation.player2.isLocked = true;
                    if(PlayerOnesGame) {
                        simulation.player.isLocked = false;
                    }
                    simulation.player2.isLockedIn = false;
                    PolyshiftActivity.statusUpdated = false;
                    class GameStatusThread extends Thread{
                        public void run(){
                            GameSync.uploadSimulation(simulation);
                            String msg = opponentName + mContext.getString(R.string.has_done_a_move);
                            GameSync.SendChangeNotification(opponentID, msg, notificationGameID, PolyshiftActivity.class.getName());
                            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                            nameValuePairs.add(new BasicNameValuePair("playerOnesTurn", "" + ((PlayerOnesTurn) ? 1 : 0)));
                            PHPConnector.doRequest(nameValuePairs, "update_game.php");
                        }
                    }
                    game_status_thread = new GameStatusThread();
                    game_status_thread.start();
                    if(!move_again) {
                        simulation.allLocked = true;
                    //Es werden keine Obkjekte blockiert, da Spieler 1 noch einmal dran ist.
                    //Als letztbewegtes Objekt wird ein Polynomino gesetzt, da Spieler 1 nur seinen Spieler noch einmal bewegen darf
                    }else{
                        move_again = false;
                        simulation.lastMovedObject = simulation.lastMovedPolynomino;
                    }
                }
            }
        }
    }
}
