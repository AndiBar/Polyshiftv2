package de.polyshift.polyshift.Game.Logic;

import java.util.Random;

import de.polyshift.polyshift.Game.TrainingActivity;

/**
 * Aktualisiert und speichert den aktuellen Status des Spiels bei Spieler-Aktionen.
 *
 * @author helmsa
 *
 */

public class OfflineGameLoop{

    public boolean PlayerOnesTurn;
    public boolean RoundFinished;
    public boolean PlayerOnesGame;
    public static Thread game_status_thread;
    private String opponentID;
    private String opponentName;
    private String notificationGameID;
    public int roundCount;

    public OfflineGameLoop(String PlayerOnesGame){
        RoundFinished = true;
        roundCount = 0;
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

    public void update(Simulation simulation){
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
                    simulation.player2.isLocked = false;
                    simulation.player.isLockedIn = false;
                    TrainingActivity.statusUpdated = false;
                    roundCount++;
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
                    simulation.player.isLocked = false;
                    simulation.player2.isLockedIn = false;
                    TrainingActivity.statusUpdated = false;
                    roundCount++;
                }
            }
        }
    }
}
