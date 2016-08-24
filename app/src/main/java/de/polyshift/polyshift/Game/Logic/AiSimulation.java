package de.polyshift.polyshift.Game.Logic;


import java.io.Serializable;

import de.polyshift.polyshift.Game.GameActivity;
import de.polyshift.polyshift.Game.Objects.Player;
import de.polyshift.polyshift.Game.Objects.Polynomino;

/**
 * Erstellt und speichert das Spielfeld und simuliert das Verhalten der Spieler und Spielsteine
 * bei Touch-Eingaben.
 *
 * @author helmsa
 *
 */

public class AiSimulation extends Simulation implements Serializable{

    public AiSimulation(GameActivity activity){
        super(activity);
    }

    public void checkPlayerPosition(GameActivity activity){
        for(int i = 0; i < objects.length; i++){
            for(int j = 0; j < objects[0].length; j++){
                if(objects[i][j] instanceof Player) {
                    //Check if Player is locked in. If true, skip his turn
                    if (!objects[i][j].isLocked  && lastMovedObject instanceof Polynomino && (predictCollision(i, j, RIGHT) && predictCollision(i, j, LEFT) && predictCollision(i, j, UP) && predictCollision(i, j, DOWN))) {
                        if (objects[i][j].isPlayerOne) {
                            player.isLockedIn = true;
                            lastMovedObject = player;
                        } else if (!objects[i][j].isPlayerOne) {
                            player2.isLockedIn = true;
                            lastMovedObject = player2;
                        }
                    } else if (lastMovedObject != null && objects[i][j] == lastMovedObject) {
                        //Check if player has stopped moving
                        if (!objects[i][j].isMovingRight && !objects[i][j].isMovingLeft && !objects[i][j].isMovingUp && !objects[i][j].isMovingDown) {
                            //Check if a player has won the game
                            if (objects[i][j].isPlayerOne && i == PLAYGROUND_MAX_X) {
                                setWinner((Player) objects[i][j]);
                            } else if (!objects[i][j].isPlayerOne && i == PLAYGROUND_MIN_X) {
                                setWinner((Player) objects[i][j]);
                            //Check if player collides while moving. If true, change moving direction
                            } else if (!loop_detected && ((predictCollision(i, j, UP) && objects[i][j].lastState.equals(UP)) || (predictCollision(i, j, DOWN) && objects[i][j].lastState.equals(DOWN)))) {
                                if (j + 1 < objects[0].length && objects[i][j + 1] instanceof Player && !predictCollision(i, j + 1, UP)) {
                                    movePlayer(i, j + 1, UP);
                                    bump_detected = true;
                                } else if (j - 1 >= 0 && objects[i][j - 1] instanceof Player && !predictCollision(i, j - 1, DOWN)) {
                                    movePlayer(i, j - 1, DOWN);
                                    bump_detected = true;
                                } else if (!predictCollision(i, j, RIGHT) && predictCollision(i, j, LEFT)) {
                                    movePlayer(i, j, RIGHT);
                                } else if (predictCollision(i, j, RIGHT) && !predictCollision(i, j, LEFT)) {
                                    movePlayer(i, j, LEFT);
                                }
                            } else if (!loop_detected && ((predictCollision(i, j, RIGHT) && objects[i][j].lastState.equals(RIGHT)) || (predictCollision(i, j, LEFT) && objects[i][j].lastState.equals(LEFT)))) {
                                if (i + 1 < objects.length && objects[i + 1][j] instanceof Player && !predictCollision(i + 1, j, RIGHT)){
                                    movePlayer(i + 1, j, RIGHT);
                                    bump_detected = true;
                                } else if (i - 1 >= 0 && objects[i - 1][j] instanceof Player && !predictCollision(i - 1, j, LEFT)) {
                                    movePlayer(i - 1, j, LEFT);
                                    bump_detected = true;
                                } else if (!predictCollision(i, j, UP) && predictCollision(i, j, DOWN)) {
                                    movePlayer(i, j, UP);
                                } else if (predictCollision(i, j, UP) && !predictCollision(i, j, DOWN)) {
                                    movePlayer(i, j, DOWN);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
