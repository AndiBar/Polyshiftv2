package de.polyshift.polyshift.Game.Logic;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Random;

import de.polyshift.polyshift.Game.PolyshiftActivity;
import de.polyshift.polyshift.Game.Sync.GameSync;
import de.polyshift.polyshift.R;
import de.polyshift.polyshift.Tools.PHPConnector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 * Aktualisiert und speichert den aktuellen Status des Spiels bei Spieler-Aktionen und
 * synchronisiert diese mit dem Server.
 *
 * @author helmsa
 *
 */

public class GameLoop{

    public boolean PlayerOnesTurn;
    public boolean RoundFinished;
    public boolean PlayerOnesGame;
    public Thread game_status_thread;
    private boolean move_again = false;
    private Activity mContext;
    private boolean initialSaveDone = false;
    private boolean intermediateSaveDone = false;
    private ArrayList<Simulation> savedSimulations;

    public GameLoop(String PlayerOnesGame, Activity activity){
        mContext = activity;
        game_status_thread = new Thread();
        RoundFinished = true;
        savedSimulations = new ArrayList<>();
        if(PlayerOnesGame.equals("yes")){
            this.PlayerOnesGame = true;
        }else{
            this.PlayerOnesGame = false;
        }
    }

    public boolean setRandomPlayer(){
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
        return PlayerOnesTurn;
    }

    public void update(final Simulation simulation, final String opponentID,final String opponentName,final String notificationGameID){
        //Wenn Spieler 1 ander Reihe ist, prüfe ob er sich bewegt. Wenn ja läuft die Runde noch
        if(PlayerOnesTurn){
            if(!initialSaveDone){
                savedSimulations.clear();
                savedSimulations.add(simulation);
                initialSaveDone = true;
            }
            simulation.player2.isLocked = true;
            if(simulation.player.isMovingRight || simulation.player.isMovingLeft || simulation.player.isMovingUp || simulation.player.isMovingDown){
                RoundFinished = false;
            }else if(!intermediateSaveDone){
                savedSimulations.add(simulation);
                intermediateSaveDone = true;
            }
            //Wurde ein Anstoßen eines anderen Spielers festgestellt, ist dieser and der Reihe, ohne dass die Runde von Spieler 1 beendet wird
            if(simulation.bump_detected) {
                simulation.bump_detected = false;
                move_again = true;
                PlayerOnesTurn = false;
            }else if(!RoundFinished || simulation.player.isLockedIn){
                //Wenn sich Spieler 1 nicht mehr bewegt, ist die Runde beendet
                if(!simulation.player.isMovingRight && !simulation.player.isMovingLeft && !simulation.player.isMovingUp && !simulation.player.isMovingDown){
                    initialSaveDone = false;
                    intermediateSaveDone = false;
                    RoundFinished = true;
                    PlayerOnesTurn = false;
                    simulation.player.isLocked = true;
                    if(!PlayerOnesGame) {
                        simulation.player2.isLocked = false;
                    }
                    simulation.player.isLockedIn = false;
                    PolyshiftActivity.statusUpdated = false;
                    if(!move_again || simulation.hasWinner) {
                        simulation.allLocked = true;
                        class GameStatusThread extends Thread{
                            public void run(){
                                GameSync.uploadSimulations(savedSimulations);
                                String msg = opponentName + mContext.getString(R.string.has_done_a_move);
                                GameSync.SendChangeNotification(opponentID, msg, notificationGameID, PolyshiftActivity.class.getName());
                                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                nameValuePairs.add(new BasicNameValuePair("playerOnesTurn", "" + ((PlayerOnesTurn) ? 1 : 0)));
                                PHPConnector.doRequest(nameValuePairs, "update_game.php");
                            }
                        }
                        game_status_thread = new GameStatusThread();
                        game_status_thread.start();
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
            if(!initialSaveDone){
                savedSimulations.clear();
                savedSimulations.add(simulation);
                initialSaveDone = true;
            }
            simulation.player.isLocked = true;
            if(simulation.player2.isMovingRight || simulation.player2.isMovingLeft || simulation.player2.isMovingUp || simulation.player2.isMovingDown){
                RoundFinished = false;
            }else if(!intermediateSaveDone){
                savedSimulations.add(simulation);
                intermediateSaveDone = true;
            }
            //Wurde ein Anstoßen eines anderen Spielers festgestellt, ist dieser and der Reihe, ohne dass die Runde von Spieler 2 beendet wird
            if(simulation.bump_detected) {
                simulation.bump_detected = false;
                move_again = true;
                PlayerOnesTurn = true;
            }else if(!RoundFinished || simulation.player2.isLockedIn){
                //Wenn sich Spieler 2 nicht mehr bewegt, ist die Runde beendet
                if(!simulation.player2.isMovingRight && !simulation.player2.isMovingLeft && !simulation.player2.isMovingUp && !simulation.player2.isMovingDown){
                    initialSaveDone = false;
                    intermediateSaveDone = false;
                    RoundFinished = true;
                    PlayerOnesTurn = true;
                    simulation.player2.isLocked = true;
                    if(PlayerOnesGame) {
                        simulation.player.isLocked = false;
                    }
                    simulation.player2.isLockedIn = false;
                    PolyshiftActivity.statusUpdated = false;
                    if(!move_again || simulation.hasWinner) {
                        simulation.allLocked = true;
                        class GameStatusThread extends Thread{
                            public void run(){
                                if(GameSync.uploadSimulations(savedSimulations).equals("playground uploaded.")) {
                                    String msg = opponentName + mContext.getString(R.string.has_done_a_move);
                                    GameSync.SendChangeNotification(opponentID, msg, notificationGameID, PolyshiftActivity.class.getName());
                                    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                    nameValuePairs.add(new BasicNameValuePair("playerOnesTurn", "" + ((PlayerOnesTurn) ? 1 : 0)));
                                    PHPConnector.doRequest(nameValuePairs, "update_game.php");
                                }
                            }
                        }
                        game_status_thread = new GameStatusThread();
                        game_status_thread.start();
                    //Es werden keine Obkjekte blockiert, da Spieler 1 noch einmal dran ist
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
