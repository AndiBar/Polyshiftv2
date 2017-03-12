package de.polyshift.polyshift.Game.Logic;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import de.polyshift.polyshift.Game.Objects.GameObject;
import de.polyshift.polyshift.Game.Objects.Player;
import de.polyshift.polyshift.Game.Objects.Polynomino;
import de.polyshift.polyshift.Game.TrainingActivity;

/**
 * Aktualisiert und speichert den aktuellen Status des Spiels bei Spieler-Aktionen.
 *
 * @author helmsa
 *
 */

public class AiGameLoop {

    public boolean PlayerOnesTurn;
    public boolean RoundFinished;
    public boolean PlayerOnesGame;
    public boolean aiRunning;
    public static Thread game_status_thread;
    private String opponentID;
    private String opponentName;
    private String notificationGameID;
    public int roundCount;

    public AiGameLoop(String PlayerOnesGame){
        RoundFinished = true;
        aiRunning = false;
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
                }
            }
        }
        if(!PlayerOnesTurn){
            simulation.player.isLocked = true;
            if(simulation.player2.isMovingRight || simulation.player2.isMovingLeft || simulation.player2.isMovingUp || simulation.player2.isMovingDown){
                RoundFinished = false;
            }
            if(!aiRunning && !simulation.hasWinner && RoundFinished && (!simulation.player2.isMovingRight || !simulation.player2.isMovingLeft || !simulation.player2.isMovingUp || !simulation.player2.isMovingDown)) {
                aiRunning = true;
                final Simulation threadSim = simulation;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(!checkIfPlayerWins(threadSim)) {
                            Log.d("test","player does not win");
                            doPolynominoAndPlayerMovement(threadSim);
                        }else{
                            Log.d("test","player wins");
                            if(!doPolinominoMovement(threadSim)){
                                doPolynominoAndPlayerMovement(threadSim);
                            }else{
                                doRandomPlayerMovement(threadSim);
                            }
                        }
                    }
                }).start();
            }
            if(simulation.bump_detected) {
                simulation.bump_detected = false;
                PlayerOnesTurn = true;
            }else if(!RoundFinished || simulation.player2.isLockedIn){
                if(!simulation.player2.isMovingRight && !simulation.player2.isMovingLeft && !simulation.player2.isMovingUp && !simulation.player2.isMovingDown){
                    RoundFinished = true;
                    PlayerOnesTurn = true;
                    aiRunning = false;
                    simulation.player2.isLocked = true;
                    simulation.player.isLocked = false;
                    simulation.player2.isLockedIn = false;
                    TrainingActivity.statusUpdated = false;
                    roundCount++;
                }
            }
        }
    }

    public static Simulation doRandomPlayerMovement(Simulation simulation){
        Random random = new Random();
        int randomNo = random.nextInt(4) + 1;
        int x = (int) simulation.player2.block_position.x;
        int y = (int) simulation.player2.block_position.y;
        String direction = Simulation.LEFT;
        do {
            randomNo = random.nextInt(4) + 1;
            switch (randomNo) {
                case (1):
                    direction = Simulation.LEFT;
                    break;
                case (2):
                    direction = Simulation.RIGHT;
                    break;
                case (3):
                    direction = Simulation.UP;
                    break;
                case (4):
                    direction = Simulation.DOWN;
                    break;
            }
        }while (simulation.predictCollision(x,y,direction));
        simulation.movePlayer(x,y,direction);
        return simulation;
    }

    public String serializeSimulation(Simulation simulation){
        String serializedObjects = "";
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(simulation);
            so.flush();

            serializedObjects = Base64.encodeToString(bo.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
        }
        return serializedObjects;
    }

    public Simulation deserializeSimulation(String serializedObjects){
        Simulation simulation = null;
        try {
            byte b[] = Base64.decode(serializedObjects,Base64.DEFAULT);
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            simulation = (Simulation) si.readObject();
        } catch (Exception e) {
            System.out.println(e);
        }
        return simulation;
    }

    public Simulation doPolynominoAndPlayerMovement(Simulation simulation){
        String serializedSimulation = serializeSimulation(simulation);
        Simulation ai_simulation;
        GameObject lastGameObject = null;
        int temp_diff = simulation.PLAYGROUND_MAX_X;
        String polynomino_dir = "";
        String player_dir = "";
        int polynomino_x = 0;
        int polynomino_y = 0;
        int player_x = 0;
        int player_y = 0;
        int min_x = 0;
        for(int i = min_x; i < simulation.objects.length; i++){
            for(int j = 0; j < simulation.objects[0].length; j++){
                if(simulation.objects[i][j] instanceof Polynomino && !simulation.objects[i][j].isLocked && simulation.objects[i][j] != lastGameObject) {
                    lastGameObject = simulation.objects[i][j];
                    Random random = new Random();
                    String direction = null;
                    for (int m = 1; m < 6; m++) {
                        switch (m) {
                            case (1):
                                direction = Simulation.LEFT;
                                break;
                            case (2):
                                direction = Simulation.RIGHT;
                                break;
                            case (3):
                                direction = Simulation.UP;
                                break;
                            case (4):
                                direction = Simulation.DOWN;
                                break;
                            case (5):
                                break;
                        }
                            if (simulation.objects[(int) simulation.player2.block_position.x][(int) simulation.player2.block_position.y] != null) {
                                for (int l = 1; l < 5; l++) {
                                    String playerDirection = "";
                                    switch (l) {
                                        case (1):
                                            playerDirection = Simulation.LEFT;
                                            break;
                                        case (2):
                                            playerDirection = Simulation.RIGHT;
                                            break;
                                        case (3):
                                            playerDirection = Simulation.UP;
                                            break;
                                        case (4):
                                            playerDirection = Simulation.DOWN;
                                            break;
                                    }
                                    ai_simulation = deserializeSimulation(serializedSimulation);
                                    int x = (int) ai_simulation.player2.block_position.x;
                                    int y = (int) ai_simulation.player2.block_position.y;
                                    Log.d("poly_x", "player_x: " + x);
                                    Log.d("poly_x", "player_y: " + y);
                                    if(direction != null) {
                                        ai_simulation.movePolynomio(i, j, direction);
                                    }
                                    ai_simulation.movePlayer(x, y, playerDirection);
                                    for(int z = 0; z < 10; z++){
                                        checkPlayerPosition(ai_simulation);
                                    }

                                    int diff = (int) ai_simulation.player2.block_position.x;
                                    Log.d("diff:", "diff:" + diff);
                                    Log.d("dir:", "player_dir:" + playerDirection);
                                    Log.d("dir:", "poly_dir:" + direction);
                                    Log.d("poly_x", "poly_x: " + i);
                                    Log.d("poly_x", "poly_y: " + j);
                                    if (diff < temp_diff) {
                                        temp_diff = diff;
                                        polynomino_x = i;
                                        polynomino_y = j;
                                        polynomino_dir = direction;
                                        player_dir = playerDirection;
                                    }
                                }
                                player_x = (int) simulation.player2.block_position.x;
                                player_y = (int) simulation.player2.block_position.y;
                            }

                    }
                }
            }
        }
        Log.d("dir:", "player_dir:" + player_dir);
        Log.d("dir:", "poly_dir:" + polynomino_dir);
        Log.d("poly_x", "poly_x: " + polynomino_x);
        Log.d("poly_x", "poly_y: " + polynomino_y);
        simulation.movePolynomio(polynomino_x, polynomino_y, polynomino_dir);
        if(simulation.predictCollision(player_x,player_y,player_dir)){
            doRandomPlayerMovement(simulation);
            Log.d("test","test123random " + player_dir);
        }else {
            Log.d("test","test123");
            simulation.movePlayer(player_x,player_y,player_dir);
        }
        return simulation;
    }

    private void checkPlayerPosition(Simulation simulation){
        for(int i = 0; i < simulation.objects.length; i++){
            for(int j = 0; j < simulation.objects[0].length; j++){
                if(simulation.objects[i][j] instanceof Player) {
                    Player player = (Player) simulation.objects[i][j];
                    Log.d("test", "player " + (player.isPlayerOne ? "1" : "2") + " found:" + "x: " + i + "y :" + j);
                    Log.d("test", "x: " + player.block_position.x);
                    //Check if Player is locked in. If true, skip his turn
                    if (!simulation.loop_detected && ((simulation.predictCollision(i, j, Simulation.UP)) || (simulation.predictCollision(i, j, Simulation.DOWN)))) {
                        if (j + 1 < simulation.objects[0].length && simulation.objects[i][j + 1] instanceof Player && !simulation.predictCollision(i, j + 1, Simulation.UP)) {
                            simulation.movePlayer(i, j + 1, Simulation.UP);
                            simulation.bump_detected = true;
                            simulation.objects[i][j] = null;
                            Log.d("bla","blabla");
                        } else if (j - 1 >= 0 && simulation.objects[i][j - 1] instanceof Player && !simulation.predictCollision(i, j - 1, Simulation.DOWN)) {
                            simulation.movePlayer(i, j - 1, Simulation.DOWN);
                            simulation.bump_detected = true;
                            simulation.objects[i][j] = null;
                            Log.d("bla","blabla");
                        } else if (!simulation.predictCollision(i, j, Simulation.RIGHT) && simulation.predictCollision(i, j, Simulation.LEFT)) {
                            simulation.movePlayer(i, j, Simulation.RIGHT);
                            simulation.objects[i][j] = null;
                            Log.d("bla","blabla");
                        } else if (simulation.predictCollision(i, j, Simulation.RIGHT) && !simulation.predictCollision(i, j, Simulation.LEFT)) {
                            simulation.movePlayer(i, j, Simulation.LEFT);
                            simulation.objects[i][j] = null;
                            Log.d("bla","blabla");
                        }
                    } else if (!simulation.loop_detected && ((simulation.predictCollision(i, j, Simulation.RIGHT)) || (simulation.predictCollision(i, j, Simulation.LEFT)))) {
                        if (i + 1 < simulation.objects.length && simulation.objects[i + 1][j] instanceof Player && !simulation.predictCollision(i + 1, j, Simulation.RIGHT)) {
                            simulation.movePlayer(i + 1, j, Simulation.RIGHT);
                            simulation.bump_detected = true;
                            simulation.objects[i][j] = null;
                            Log.d("bla","blabla");
                        } else if (i - 1 >= 0 && simulation.objects[i - 1][j] instanceof Player && !simulation.predictCollision(i - 1, j, Simulation.LEFT)) {
                            simulation.movePlayer(i - 1, j, Simulation.LEFT);
                            simulation.bump_detected = true;
                            simulation.objects[i][j] = null;
                            Log.d("bla","blabla");
                        } else if (!simulation.predictCollision(i, j, Simulation.UP) && simulation.predictCollision(i, j, Simulation.DOWN)) {
                            simulation.movePlayer(i, j, Simulation.UP);
                            simulation.objects[i][j] = null;
                            Log.d("bla","blabla");
                        } else if (simulation.predictCollision(i, j, Simulation.UP) && !simulation.predictCollision(i, j, Simulation.DOWN)) {
                            simulation.movePlayer(i, j, Simulation.DOWN);
                            simulation.objects[i][j] = null;
                            Log.d("bla","blabla");
                        }
                    }
                }
            }
        }
    }

    public boolean checkIfPlayerWins(Simulation simulation) {
        String serializedSimulation = serializeSimulation(simulation);
        Simulation ai_simulation = null;
        GameObject lastGameObject = null;
        int min_x = 0;
        for(int i = min_x; i < simulation.objects.length; i++){
            for(int j = 0; j < simulation.objects[0].length; j++){
                if(simulation.objects[i][j] instanceof Polynomino && !simulation.objects[i][j].isLocked && simulation.objects[i][j] != lastGameObject) {
                    lastGameObject = simulation.objects[i][j];
                    Random random = new Random();
                    int randomNo = random.nextInt(4) + 1;
                    String direction = null;
                    boolean collision = false;
                    for (int m = 1; m < 6; m++) {
                        collision = false;
                        switch (m) {
                            case (1):
                                direction = Simulation.LEFT;
                                break;
                            case (2):
                                direction = Simulation.RIGHT;
                                break;
                            case (3):
                                direction = Simulation.UP;
                                break;
                            case (4):
                                direction = Simulation.DOWN;
                                break;
                            case (5):
                                break;
                        }
                        Polynomino polynomino = (Polynomino) simulation.objects[i][j];
                        for (int k = 0; k < polynomino.blocks.size(); k++) {
                            if (simulation.predictCollision(polynomino.blocks.get(k).x, polynomino.blocks.get(k).y, direction)) {
                                collision = true;
                            }
                        }
                        if(!collision) {
                            if (simulation.objects[(int) simulation.player.block_position.x][(int) simulation.player.block_position.y] != null) {
                                for (int l = 1; l < 5; l++) {
                                    String playerDirection = "";
                                    switch (l) {
                                        case (1):
                                            playerDirection = Simulation.LEFT;
                                            break;
                                        case (2):
                                            playerDirection = Simulation.RIGHT;
                                            break;
                                        case (3):
                                            playerDirection = Simulation.UP;
                                            break;
                                        case (4):
                                            playerDirection = Simulation.DOWN;
                                            break;
                                    }
                                    ai_simulation = deserializeSimulation(serializedSimulation);
                                    int x = (int) ai_simulation.player.block_position.x;
                                    int y = (int) ai_simulation.player.block_position.y;
                                    if(direction != null){
                                        ai_simulation.movePolynomio(i, j, direction);
                                    }
                                    ai_simulation.movePlayer(x, y, playerDirection);

                                    checkPlayerPosition(ai_simulation);

                                    Log.d("dir:", "player_dir:" + playerDirection);
                                    Log.d("dir:", "poly_dir:" + direction);
                                    Log.d("poly_x", "poly_x: " + i);
                                    Log.d("poly_x", "poly_y: " + j);
                                    if (ai_simulation.winner != null && ai_simulation.winner.isPlayerOne) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean doPolinominoMovement(Simulation simulation){
        String serializedSimulation = serializeSimulation(simulation);
        Simulation ai_simulation;
        GameObject lastGameObject = null;
        int min_x = 0;
        for(int i = min_x; i < simulation.objects.length; i++) {
            for (int j = 0; j < simulation.objects[0].length; j++) {
                if (simulation.objects[i][j] instanceof Polynomino && !simulation.objects[i][j].isLocked && simulation.objects[i][j] != lastGameObject) {
                    lastGameObject = simulation.objects[i][j];
                    Random random = new Random();
                    String direction = Simulation.LEFT;
                    boolean collision;
                    for (int m = 1; m < 5; m++) {
                        collision = false;
                        switch (m) {
                            case (1):
                                direction = Simulation.LEFT;
                                break;
                            case (2):
                                direction = Simulation.RIGHT;
                                break;
                            case (3):
                                direction = Simulation.UP;
                                break;
                            case (4):
                                direction = Simulation.DOWN;
                                break;
                        }
                        Polynomino polynomino = (Polynomino) simulation.objects[i][j];
                        for (int k = 0; k < polynomino.blocks.size(); k++) {
                            if (simulation.predictCollision(polynomino.blocks.get(k).x, polynomino.blocks.get(k).y, direction)) {
                                collision = true;
                            }
                        }

                        ai_simulation = deserializeSimulation(serializedSimulation);
                        ai_simulation.movePolynomio(i, j, direction);
                        if (!collision && !checkIfPlayerWins(ai_simulation)) {
                            simulation.movePolynomio(i, j, direction);
                            return true;
                        }
                    }

                }

            }
        }
        return false;
    }
}
