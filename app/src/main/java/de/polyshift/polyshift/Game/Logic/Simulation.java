package de.polyshift.polyshift.Game.Logic;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;

import java.io.Serializable;
import java.util.ArrayList;

import de.polyshift.polyshift.Game.GameActivity;
import de.polyshift.polyshift.Game.Objects.GameObject;
import de.polyshift.polyshift.Game.Objects.Block;
import de.polyshift.polyshift.Game.Objects.Player;
import de.polyshift.polyshift.Game.Objects.Polynomino;
import de.polyshift.polyshift.Game.PolyshiftActivity;
import de.polyshift.polyshift.Game.Renderer.Vector;
import de.polyshift.polyshift.Game.Sync.GameSync;

/**
 * Erstellt und speichert das Spielfeld und simuliert das Verhalten der Spieler und Spielsteine
 * bei Touch-Eingaben.
 *
 * @author helmsa
 *
 */

public class Simulation implements Serializable{

    final static int PLAYGROUND_MAX_X = 16;
    final static int PLAYGROUND_MIN_X = 0;
    final static int PLAYGROUND_MAX_Y = 8;
    final static int PLAYGROUND_MIN_Y = 0;
    final static int PLAYGROUND_POPULATE = 8;
    final static int POLYNOMIO_SIZE = 4;
    public final static String RIGHT = "right";
    public final static String LEFT = "left";
    public final static String UP = "up";
    public final static String DOWN = "down";

    public boolean hasWinner =  false;
    public Player player;
    public Player player2;
    public Player winner;
    public boolean allLocked = false;
    public boolean loop_detected = false;
    public boolean bump_detected = false;

    private int touchedX = 0;
    private int touchedY = 0;
    private float swipeX = 0;
    private float swipeY = 0;
    private boolean gameObjectSelected = false;

    ArrayList<Polynomino> polynominos = new ArrayList<Polynomino>();

    public GameObject[][] objects = new GameObject[PLAYGROUND_MAX_X+1][PLAYGROUND_MAX_Y+1];

    public GameObject lastMovedObject;
    public Polynomino lastMovedPolynomino;

    public Simulation(GameActivity activity){
        populate();
    }

    public void populate(){

        ArrayList<int[]>directions = new ArrayList<int[]>();
        /*ArrayList<float[]> colors = new ArrayList<float[]>();
        float[] color1 = {(51f/255f),(77f/255),(92f/255f),1f};
        float[] color2 = {(71f/255f),(176f/255f),(156f/255f),1f};
        float[] color3 = {(239f/255f),(201f/255f),(76f/255f),1f};
        float[] color4 = {(226f/255f),(122f/255f),(65f/255f),1f};
        float[] color5 = {(223f/255f),(73f/255f),(73f/255f),1f};
        colors.add(color1);
        colors.add(color2);
        colors.add(color3);
        colors.add(color4);
        colors.add(color5);*/
        this.objects = new GameObject[PLAYGROUND_MAX_X+1][PLAYGROUND_MAX_Y+1];

        player = new Player(true);
        player.start_position = new Vector(PLAYGROUND_MIN_X,PLAYGROUND_MAX_Y/2,0);
        player.block_position = player.start_position;
        setGameObject(player, PLAYGROUND_MIN_X, PLAYGROUND_MAX_Y / 2);
        player2 = new Player(false);
        player2.start_position = new Vector(PLAYGROUND_MAX_X,PLAYGROUND_MAX_Y/2,0);
        player2.block_position = player2.start_position;
        setGameObject(player2, PLAYGROUND_MAX_X,PLAYGROUND_MAX_Y/2);

        ArrayList<Integer> intArr = new ArrayList<Integer>();
        for(int i = 0; i < PLAYGROUND_MAX_Y+1; i++){
            intArr.add(0);
        }

        int a = 0;
        while(a<10){
            for(int x = (PLAYGROUND_MAX_X/2)-(PLAYGROUND_POPULATE/2);x <= (PLAYGROUND_MAX_X/2)+(PLAYGROUND_POPULATE/2);x++){
                for(int y = 0;y < PLAYGROUND_MAX_Y;y++){
                    if(!(objects[x][y] instanceof GameObject)){
                        int currentPolynomioSize = 0;
                        //int randomColor =  (int)(Math.random()*colors.size());
                        Polynomino polynomino = new Polynomino(GameSync.recreateColor());
                        int currentX = x;
                        int currentY = y;
                        int lastX = x;
                        int lastY = y;
                        boolean canGoRight = true;
                        boolean canGoLeft = true;
                        boolean canGoUp = true;
                        boolean canGoDown = true;
                        int max_count = 0;
                        while(currentPolynomioSize < POLYNOMIO_SIZE &&
                                max_count <= 10 &&
                                canGoRight &&
                                canGoLeft &&
                                canGoUp &&
                                canGoDown){
                            max_count++;
                            boolean free = true;
                            lastX = currentX;
                            lastY = currentY;
                            int newBlockDirection = (int) (Math.random() * 4 + 1);
                            if (newBlockDirection == 1 && (currentX < (PLAYGROUND_MAX_X / 2) + (PLAYGROUND_POPULATE / 2))) {
                                if ((currentY + 1 <= PLAYGROUND_MAX_Y && currentY - 1 >= 0 && !(objects[currentX + 1][currentY + 1] instanceof GameObject) && !(objects[currentX + 1][currentY - 1] instanceof GameObject))) {
                                    currentX++;
                                } else if(currentY == PLAYGROUND_MAX_Y || currentY == 0){
                                    currentX++;
                                }
                            }
                            else if (newBlockDirection == 2 && (currentX > (PLAYGROUND_MAX_X / 2) - (PLAYGROUND_POPULATE / 2))) {
                                if ((currentY + 1 <= PLAYGROUND_MAX_Y && currentY - 1 >= 0 && !(objects[currentX - 1][currentY + 1] instanceof GameObject) && !(objects[currentX - 1][currentY - 1] instanceof GameObject))) {
                                    currentX--;
                                } else if(currentY == PLAYGROUND_MAX_Y || currentY == 0){
                                    currentX--;
                                }
                            }
                            else if (newBlockDirection == 3 && (currentY < PLAYGROUND_MAX_Y)) {
                                if ((currentX + 1 <= PLAYGROUND_MAX_X && currentX - 1 >= 0 && !(objects[currentX + 1][currentY + 1] instanceof GameObject) && !(objects[currentX - 1][currentY + 1] instanceof GameObject))) {
                                    currentY++;
                                }
                            }
                            else if (newBlockDirection == 4 && (currentY > PLAYGROUND_MIN_Y)) {
                                if ((currentX + 1 <= PLAYGROUND_MAX_X && currentX - 1 >= 0 && !(objects[currentX + 1][currentY - 1] instanceof GameObject) && !(objects[currentX - 1][currentY - 1] instanceof GameObject))) {
                                    currentY--;
                                }
                            }
                            if(!(objects[currentX][currentY] instanceof GameObject)){
                                for(int i = 0; i < polynomino.size; i++){
                                    Block block = polynomino.blocks.get(i);
                                    if(block.x == currentX && block.y == currentY){
                                        free = false;
                                        currentX = lastX;
                                        currentY = lastY;
                                    }
                                }
                                if(free){
                                    polynomino.addBlock(new Block(currentX,currentY));
                                    polynomino.blockCounter();
                                    currentPolynomioSize++;
                                }
                            }else{
                                currentX = lastX;
                                currentY = lastY;
                                if(newBlockDirection == 1 ){
                                    canGoRight = false;
                                }
                                else if(newBlockDirection == 2){
                                    canGoLeft = false;
                                }
                                else if(newBlockDirection == 3){
                                    canGoUp = false;
                                }
                                else if(newBlockDirection == 4){
                                    canGoDown = false;
                                }
                            }
                        }
                        if (polynomino.size == POLYNOMIO_SIZE){
                            polynominos.add(polynomino);
                            int tempY = -1;
                            for(int j = 0; j< polynomino.blocks.size();j++){
                                Block block = polynomino.blocks.get(j);
                                setGameObject(polynomino, block.x, block.y);
                                if(tempY != block.y) {
                                    intArr.set(block.y, intArr.get(block.y) + 1);
                                    tempY = block.y;
                                }
                            }
                        }
                    }
                }
            }
            a++;
        }

        //check if there are at least 2 Polynominos in a row, otherwise repopuate.
        for(int count : intArr){
            Log.d("String", "count: "+  count);
            if(count < 2){
                populate();
                break;
            }
        }

    }

    public void setGameObject(GameObject object, int x, int y){
        objects[x][y] = object;
    }

    public void getTouch(GameActivity activity){
        float display_y = 0;
        float display_x = 0;

        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        display_x = metrics.widthPixels;
        display_y = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getHeight();

        int swipedX = 0;
        int swipedY = 0;

        if(activity.getSwipedY() != 0 && activity.getSwipedX() != 0) {

            float swipeX_tmp = 0;
            //Berührungspunkt / (Spielfeldbreitenpixel / Spielfeldbreite)
            swipeX_tmp = activity.getSwipedX() / (display_x / objects.length);

            float swipeY_tmp = 0;
            //Spielfeldhöhe - (Berührungspunkt / (Spielfeldhöhenpixel / Spielfeldhöhe)
            swipeY_tmp = (objects[0].length + 1) - (activity.getSwipedY() / (display_y / objects[0].length));

            if (activity.swipedDirection != null && (swipeX != swipeX_tmp && swipeY != swipeY_tmp)){
                swipeX = swipeX_tmp;
                swipeY = swipeY_tmp;

                swipedX = (int) swipeX;
                swipedY = (int) swipeY;

                Log.d("test", "swiped object x: " + swipeX);
                Log.d("test", "swiped object y: " + swipeY);
                Log.d("test", "swiped direction: " + activity.swipedDirection);

                if (gameObjectSelected) {
                    swipedX = touchedX;
                    swipedY = touchedY;
                    gameObjectSelected = false;
                } else {

                    if (swipedX > PLAYGROUND_MAX_X) {
                        swipedX = PLAYGROUND_MAX_X;
                    } else if (swipedX < PLAYGROUND_MIN_X) {
                        swipedX = PLAYGROUND_MIN_X;
                    }
                    if (swipedY > PLAYGROUND_MAX_Y) {
                        swipedY = PLAYGROUND_MAX_Y;
                    } else if (swipedY < PLAYGROUND_MIN_Y) {
                        swipedY = PLAYGROUND_MIN_Y;
                    }
                }

                if (activity.swipedDirection.equals(Simulation.RIGHT)) {
                    activity.swipedDirection = null;
                    if (objects[swipedX][swipedY] instanceof Player && !objects[swipedX][swipedY].isLocked) {
                        movePlayer(swipedX, swipedY, RIGHT);
                    }
                    if (objects[swipedX][swipedY] instanceof Polynomino && !(lastMovedObject instanceof Polynomino) && !objects[swipedX][swipedY].isLocked && !objects[swipedX][swipedY].allLocked) {
                        movePolynomio(swipedX, swipedY, RIGHT);
                    }
                } else if (activity.swipedDirection.equals(Simulation.LEFT)) {
                    activity.swipedDirection = null;
                    if (objects[swipedX][swipedY] instanceof Player && !objects[swipedX][swipedY].isLocked) {
                        movePlayer(swipedX, swipedY, LEFT);
                    }
                    if (objects[swipedX][swipedY] instanceof Polynomino && !(lastMovedObject instanceof Polynomino) && !objects[swipedX][swipedY].isLocked && !objects[swipedX][swipedY].allLocked) {
                        movePolynomio(swipedX, swipedY, LEFT);
                    }
                } else if (activity.swipedDirection.equals(Simulation.UP)) {
                    activity.swipedDirection = null;
                    if (objects[swipedX][swipedY] instanceof Player && !objects[swipedX][swipedY].isLocked) {
                        movePlayer(swipedX, swipedY, UP);
                    }
                    if (objects[swipedX][swipedY] instanceof Polynomino && !(lastMovedObject instanceof Polynomino) && !objects[swipedX][swipedY].isLocked && !objects[swipedX][swipedY].allLocked) {
                        movePolynomio(swipedX, swipedY, UP);
                    }
                } else if (activity.swipedDirection.equals(Simulation.DOWN)) {
                    activity.swipedDirection = null;
                    if (objects[swipedX][swipedY] instanceof Player && !objects[swipedX][swipedY].isLocked) {
                        movePlayer(swipedX, swipedY, DOWN);
                    }
                    if (objects[swipedX][swipedY] instanceof Polynomino && !(lastMovedObject instanceof Polynomino) && !objects[swipedX][swipedY].isLocked && !objects[swipedX][swipedY].allLocked) {
                        movePolynomio(swipedX, swipedY, DOWN);
                    }
                }
            }
            activity.resetSwipe();

        }else if (activity.getTouchedX() != 0 && activity.getTouchedX() != 0) {
            float touchX = 0;
            //Berührungspunkt / (Spielfeldbreitenpixel / Spielfeldbreite)
            touchX = activity.getTouchedX() / (display_x / objects.length);

            float touchY = 0;
            //Spielfeldhöhe - (Berührungspunkt / (Spielfeldhöhenpixel / Spielfeldhöhe)
            touchY = (objects[0].length + 1) - (activity.getTouchedY() / (display_y / objects[0].length));

            touchedX = (int) touchX;
            touchedY = (int) touchY;

            Log.d("test", "touched object x: " + touchX);
            Log.d("test", "touched object y: " + touchY);

            activity.resetTouch();

            if (touchedX > PLAYGROUND_MAX_X) {
                touchedX = PLAYGROUND_MAX_X;
            } else if (touchedX < PLAYGROUND_MIN_X) {
                touchedX = PLAYGROUND_MIN_X;
            }
            if (touchedY > PLAYGROUND_MAX_Y) {
                touchedY = PLAYGROUND_MAX_Y;
            } else if (touchedY < PLAYGROUND_MIN_Y) {
                touchedY = PLAYGROUND_MIN_Y;
            }

            if (objects[touchedX][touchedY] != null && (!(lastMovedObject instanceof Polynomino) || objects[touchedX][touchedY] instanceof Player)) {
                objects[touchedX][touchedY].isSelected = true;
            }
        }
    }

    public void moveObject(int x, int y, String direction){

        if(objects[x][y] != null){
            lastMovedObject  = objects[x][y];
            if(objects[x][y] instanceof Polynomino){
                if(lastMovedPolynomino instanceof Polynomino){
                    lastMovedPolynomino.isLocked = false;
                }
                lastMovedPolynomino = (Polynomino) objects[x][y];
                objects[x][y].isLocked = true;
            }

            if(RIGHT.equals(direction)){
                objects[x][y].isMovingRight = true;
                objects[x+1][y] = objects[x][y];
                objects[x][y] = null;
            }
            if(LEFT.equals(direction)){
                objects[x][y].isMovingLeft = true;
                objects[x-1][y] = objects[x][y];
                objects[x][y] = null;
            }
            if(UP.equals(direction)){
                objects[x][y].isMovingUp = true;
                objects[x][y+1] = objects[x][y];
                objects[x][y] = null;
            }
            if(DOWN.equals(direction)){
                objects[x][y].isMovingDown = true;
                objects[x][y-1] = objects[x][y];
                objects[x][y] = null;
            }
        }

    }
    public boolean predictCollision(int x, int y, String direction){
        boolean collision = false;
        if(x <= PLAYGROUND_MAX_X && x >= PLAYGROUND_MIN_X && y <= PLAYGROUND_MAX_Y && y >= PLAYGROUND_MIN_Y){
            if(objects[x][y] instanceof Player){
                if(RIGHT.equals(direction)){
                    if((x+1 > PLAYGROUND_MAX_X || objects[x+1][y] != null)){
                        collision = true;
                    }
                }
                if(LEFT.equals(direction)){
                    if((x-1 < PLAYGROUND_MIN_X || objects[x-1][y] != null)){
                        collision = true;
                    }
                }
                if(UP.equals(direction)){
                    if((y+1 > PLAYGROUND_MAX_Y || objects[x][y+1] != null)){
                        collision = true;
                    }
                }
                if(DOWN.equals(direction)){
                    if((y-1 < PLAYGROUND_MIN_Y || objects[x][y-1] != null)){
                        collision = true;
                    }
                }
            }
            if(objects[x][y] instanceof Polynomino){
                if(direction.equals(RIGHT)){
                    if(x+1 > PLAYGROUND_MAX_X){
                        collision = true;
                    }
                    else if(objects[x+1][y] != null){
                        collision = true;
                        if(objects[x+1][y] == objects[x][y]){
                            if(!predictCollision(x+2,y,RIGHT)){
                                collision = false;
                            }
                        }
                    }
                }
                if(direction.equals(LEFT)){
                    if(x-1 < PLAYGROUND_MIN_X){
                        collision = true;
                    }
                    else if(objects[x-1][y] != null){
                        collision = true;
                        if(objects[x-1][y] == objects[x][y]){
                            if(!predictCollision(x-2,y,LEFT)){
                                collision = false;
                            }
                        }
                    }
                }
                if(direction.equals(UP)){
                    if(y+1 > PLAYGROUND_MAX_Y){
                        collision = true;
                    }
                    else if(objects[x][y+1] != null){
                        collision = true;
                        if(objects[x][y+1] == objects[x][y]){
                            if(!predictCollision(x,y+2,UP)){
                                collision = false;
                            }
                        }
                    }
                }
                if(direction.equals(DOWN)){
                    if(y-1 < PLAYGROUND_MIN_Y){
                        collision = true;
                    }
                    else if(objects[x][y-1] != null){
                        collision = true;
                        if(objects[x][y-1] == objects[x][y]){
                            if(!predictCollision(x,y-2,DOWN)){
                                collision = false;
                            }
                        }
                    }
                }
            }
        }
        return collision;
    }
    public void movePolynomio(int x, int y, String direction){
        boolean collision = false;
        PolyshiftActivity.statusUpdated = false;
        Polynomino polynomino = (Polynomino) objects[x][y];
        polynomino.block_position = new Vector(x,y,0);
        polynomino.sortBlocks(direction);
        polynomino.blocks.get(3).block_position.x = polynomino.blocks.get(3).x;
        polynomino.blocks.get(0).block_position.x = polynomino.blocks.get(0).x;
        polynomino.blocks.get(3).block_position.y = polynomino.blocks.get(3).y;
        polynomino.blocks.get(0).block_position.y = polynomino.blocks.get(0).y;
        while(!collision){
            for(int i = 0; i < polynomino.blocks.size(); i++){
                if(predictCollision(polynomino.blocks.get(i).x, polynomino.blocks.get(i).y, direction)){
                    collision = true;
                }
            }
            if(!collision){
                for(int i = 0; i < polynomino.blocks.size(); i++){
                    moveObject(polynomino.blocks.get(i).x, polynomino.blocks.get(i).y, direction);
                    Block block = polynomino.blocks.get(i);
                    if(direction.equals(RIGHT)){
                        block.x++;
                    }
                    else if(direction.equals(LEFT)){
                        block.x--;
                    }
                    else if(direction.equals(UP)){
                        block.y++;
                    }
                    else if(direction.equals(DOWN)){
                        block.y--;
                    }
                    polynomino.blocks.set(i, block);
                }
            }
        }
    }

    public void movePlayer(int x, int y, String direction){
        boolean moving_started = false;
        loop_detected = false;
        if(lastMovedObject != objects[x][y]) {
            objects[x][y].start_position = new Vector(x, y, 0);
            moving_started = true;
        }
        Player player = (Player) objects[x][y];
        objects[x][y].block_position = new Vector(x,y,0);
        while(!predictCollision(x, y, direction) && !loop_detected){
            moveObject(x, y, direction);
            if(moving_started) {
                if(player.isMovingRight){
                    player.start_direction = RIGHT;
                }else if(player.isMovingLeft){
                    player.start_direction = LEFT;
                }else if(player.isMovingUp){
                    player.start_direction = UP;
                }else if(player.isMovingDown){
                    player.start_direction = DOWN;
                }
                moving_started = false;
            }
            //Move player if no loop was detected, else break
            if(RIGHT.equals(direction)){
                player.block_position = new Vector(x+1,y,0);
                if(x+1 == player.start_position.x && y == player.start_position.y && predictNextMovement(x+1, y, direction).equals(player.start_direction)) {
                    loop_detected = true;
                }else{
                    x++;
                }
            }
            else if(LEFT.equals(direction)){
                player.block_position = new Vector(x-1,y,0);
                if(x-1 == player.start_position.x && y == player.start_position.y && predictNextMovement(x-1, y, direction).equals(player.start_direction)){
                    loop_detected = true;
                }else{
                    x--;
                }
            }
            else if(UP.equals(direction)){
                player.block_position = new Vector(x,y+1,0);
                if(y+1 == player.start_position.y && x == player.start_position.x && predictNextMovement(x, y + 1, direction).equals(player.start_direction)){
                    loop_detected = true;
                }else {
                    y++;
                }
            }
            else if(DOWN.equals(direction)){
                player.block_position = new Vector(x,y-1,0);
                if(y-1 == player.start_position.y && x == player.start_position.x && predictNextMovement(x, y-1 ,direction).equals(player.start_direction)){
                    loop_detected = true;
                }else {
                    y--;
                }
            }
        }
    }
    public String predictNextMovement(int x, int y, String direction){
        if(direction.equals(UP)){
            if(!predictCollision(x, y, UP)){
                return UP;
            }else if(predictCollision(x, y, LEFT) && !predictCollision(x, y, RIGHT)){
                return RIGHT;
            }else if(predictCollision(x, y, RIGHT) && !predictCollision(x, y, LEFT)){
                return LEFT;
            }else{
                return "";
            }
        }else if(direction.equals(DOWN)){
            if(!predictCollision(x, y, DOWN)){
                return DOWN;
            }else if(predictCollision(x, y, LEFT) && !predictCollision(x, y, RIGHT)){
                return RIGHT;
            }else if(predictCollision(x, y, RIGHT) && !predictCollision(x, y, LEFT)){
                return LEFT;
            }else{
                return "";
            }
        }else if(direction.equals(RIGHT)){
            if(!predictCollision(x, y, RIGHT)){
                return RIGHT;
            }else if(predictCollision(x, y, UP) && !predictCollision(x, y, DOWN)){
                return DOWN;
            }else if(predictCollision(x, y, DOWN) && !predictCollision(x, y, UP)){
                return UP;
            }else{
                return "";
            }
        }else if(direction.equals(LEFT)){
            if(!predictCollision(x, y, LEFT)){
                return LEFT;
            }else if(predictCollision(x, y, UP) && !predictCollision(x, y, DOWN)){
                return DOWN;
            }else if(predictCollision(x, y, DOWN) && !predictCollision(x, y, UP)){
                return UP;
            }else{
                return "";
            }
        }else{
            return "";
        }
    }

    public void checkPlayerPosition(GameActivity activity){
        gameObjectSelected = false;
        for(int i = 0; i < objects.length; i++){
            for(int j = 0; j < objects[0].length; j++){
                if(objects[i][j] != null) {
                    if (objects[touchedX][touchedY] != null && objects[i][j] == objects[touchedX][touchedY] &&
                       (!(lastMovedObject instanceof Polynomino ) ||
                        objects[touchedX][touchedY] instanceof Player)) {
                        objects[i][j].isSelected = true;
                        gameObjectSelected = true;
                    } else {
                        objects[i][j].isSelected = false;
                    }
                }
                if(objects[i][j] instanceof Polynomino){
                    Polynomino polynomino = (Polynomino) objects[i][j];
                    polynomino.isRendered = false;
                    if(allLocked){
                        polynomino.allLocked = true;
                    }else{
                        polynomino.allLocked = false;
                    }
                }
                if(objects[i][j] instanceof Player) {
                    Player player = (Player) objects[i][j];
                    //Check if Player is locked in. If true, skip his turn
                    if (!objects[i][j].isLocked  && lastMovedObject instanceof Polynomino && (predictCollision(i, j, RIGHT) && predictCollision(i, j, LEFT) && predictCollision(i, j, UP) && predictCollision(i, j, DOWN))) {
                        if (player.isPlayerOne) {
                            player.isLockedIn = true;
                            lastMovedObject = player;
                        } else if (!player.isPlayerOne) {
                            player2.isLockedIn = true;
                            lastMovedObject = player2;
                        }
                    } else if (lastMovedObject != null && objects[i][j] == lastMovedObject) {
                        //Check if player has stopped moving
                        if (!objects[i][j].isMovingRight && !objects[i][j].isMovingLeft && !objects[i][j].isMovingUp && !objects[i][j].isMovingDown) {
                            //Check if a player has won the game
                            if (player.isPlayerOne && i == PLAYGROUND_MAX_X) {
                                setWinner((Player) objects[i][j]);
                            } else if (!player.isPlayerOne && i == PLAYGROUND_MIN_X) {
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

    public void setWinner(Player winner){
        this.winner = winner;
        hasWinner = true;
    }

    public void update(GameActivity activity){
        getTouch(activity);
        checkPlayerPosition(activity);
    }
}
