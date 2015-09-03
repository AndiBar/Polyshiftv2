package hamburg.haw.polyshift.Game;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;

import java.io.Serializable;
import java.util.ArrayList;


public class Simulation implements Serializable{

    final int PLAYGROUND_MAX_X = 16;
    final int PLAYGROUND_MIN_X = 0;
    final int PLAYGROUND_MAX_Y = 8;
    final int PLAYGROUND_MIN_Y = 0;
    final int PLAYGROUND_POPULATE = 8;
    final int POLYNOMIO_SIZE = 4;
    final static String RIGHT = "right";
    final static String LEFT = "left";
    final static String UP = "up";
    final static String DOWN = "down";

    public boolean hasWinner =  false;
    public Player player;
    public Player player2;
    public Player winner;
    public boolean allLocked = false;


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
        setGameObject(player, PLAYGROUND_MIN_X, PLAYGROUND_MAX_Y / 2);
        player2 = new Player(false);
        player2.start_position = new Vector(PLAYGROUND_MAX_X,PLAYGROUND_MAX_Y/2,0);
        setGameObject(player2, PLAYGROUND_MAX_X,PLAYGROUND_MAX_Y/2);

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
                        while(currentPolynomioSize < POLYNOMIO_SIZE &&
                                canGoRight &&
                                canGoLeft &&
                                canGoUp &&
                                canGoDown){
                            boolean free = true;
                            lastX = currentX;
                            lastY = currentY;
                            int newBlockDirection =   (int)(Math.random()*4+1);
                            if(newBlockDirection == 1 && (currentX < (PLAYGROUND_MAX_X/2)+(PLAYGROUND_POPULATE/2))){
                                currentX++;
                            }
                            else if(newBlockDirection == 2 && (currentX > (PLAYGROUND_MAX_X/2)-(PLAYGROUND_POPULATE/2))){
                                currentX--;
                            }
                            else if(newBlockDirection == 3 && (currentY < PLAYGROUND_MAX_Y)){
                                currentY++;
                            }
                            else if(newBlockDirection == 4 && (currentY > PLAYGROUND_MIN_Y)){
                                currentY--;
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
                            for(int j = 0; j< polynomino.blocks.size();j++){
                                Block block = polynomino.blocks.get(j);
                                setGameObject(polynomino, block.x, block.y);
                            }
                        }
                    }
                }
            }
            a++;
        }
    }

    public void setGameObject(GameObject object, int x, int y){
        objects[x][y] = object;
    }

    public void getTouch(GameActivity activity){
        /*if(activity.isSwiped()){
            int x = Math.round(activity.getTouchX() / (activity.getViewportWidth() / objects.length));
            int y = Math.round(objects[0].length - (activity.getTouchY() / (activity.getViewportHeight() / objects[0].length)) - 1);
            int touchedX = Math.round(activity.getTouchedX() / (activity.getViewportWidth() / objects.length));
            int touchedY = Math.round(objects[0].length - (activity.getTouchedY() / (activity.getViewportHeight() / objects[0].length)) - 1);

            if(x > touchedX && y == touchedY){
                activity.isSwiped = false;
                if(touchedX >= PLAYGROUND_MIN_X && objects[touchedX][touchedY] instanceof Player && !objects[touchedX][touchedY].isLocked){
                    movePlayer(touchedX, touchedY, RIGHT);
                }
                if(touchedX >= PLAYGROUND_MIN_X && objects[touchedX][touchedY] instanceof Polynomino && !(lastMovedObject instanceof Polynomino) && !objects[touchedX][touchedY].isLocked && !objects[touchedX][touchedY].allLocked){
                    movePolynomio(touchedX, touchedY, RIGHT);
                }
            }
            else if(x < touchedX && y == touchedY){
                activity.isSwiped = false;
                if(touchedX <= PLAYGROUND_MAX_X && objects[touchedX][touchedY] instanceof Player && !objects[touchedX][touchedY].isLocked){
                    movePlayer(touchedX, touchedY, LEFT);
                }
                if(touchedX <= PLAYGROUND_MAX_X &&objects[touchedX][touchedY] instanceof Polynomino && !(lastMovedObject instanceof Polynomino) && !objects[touchedX][touchedY].isLocked && !objects[touchedX][touchedY].allLocked){
                    movePolynomio(touchedX, touchedY, LEFT);
                }
            }
            else if(y > touchedY && x == touchedX){
                activity.isSwiped = false;
                if(touchedY >= PLAYGROUND_MIN_Y && objects[touchedX][touchedY] instanceof Player && !objects[touchedX][touchedY].isLocked){
                    movePlayer(touchedX, touchedY, UP);
                }
                if(touchedY >= PLAYGROUND_MIN_Y && objects[touchedX][touchedY] instanceof Polynomino && !(lastMovedObject instanceof Polynomino) && !objects[touchedX][touchedY].isLocked && !objects[touchedX][touchedY].allLocked){
                    movePolynomio(touchedX, touchedY, UP);
                }
            }
            else if(y < touchedY && x == touchedX){
                activity.isSwiped = false;
                if(touchedY <= PLAYGROUND_MAX_Y && objects[touchedX][touchedY] instanceof Player && !objects[touchedX][touchedY].isLocked){
                    movePlayer(touchedX, touchedY, DOWN);
                }
                if(touchedY <= PLAYGROUND_MAX_Y && objects[touchedX][touchedY] instanceof Polynomino && !(lastMovedObject instanceof Polynomino) && !objects[touchedX][touchedY].isLocked && !objects[touchedX][touchedY].allLocked){
                    movePolynomio(touchedX, touchedY, DOWN);
                }
            }
        }*/
        if(activity.swipedDirection != null){
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            float display_x = metrics.widthPixels;
            float display_y = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getHeight();

            int touchedX = Math.round(activity.getTouchedX() / (display_x / objects.length));
            int touchedY = Math.round(objects[0].length - ((activity.getTouchedY() / (display_y / objects[0].length)) - 1));

            Log.d("test","x: " + touchedX);
            Log.d("test","y: " + touchedY);

            if(activity.swipedDirection.equals(Simulation.RIGHT)){
                activity.swipedDirection = null;
                if(objects[touchedX][touchedY] instanceof Player && !objects[touchedX][touchedY].isLocked){
                    movePlayer(touchedX, touchedY, RIGHT);
                }
                if(objects[touchedX][touchedY] instanceof Polynomino && !(lastMovedObject instanceof Polynomino) && !objects[touchedX][touchedY].isLocked && !objects[touchedX][touchedY].allLocked){
                    movePolynomio(touchedX, touchedY, RIGHT);
                }
            }
            else if(activity.swipedDirection.equals(Simulation.LEFT)){
                activity.swipedDirection = null;
                if(objects[touchedX][touchedY] instanceof Player && !objects[touchedX][touchedY].isLocked){
                    movePlayer(touchedX, touchedY, LEFT);
                }
                if(objects[touchedX][touchedY] instanceof Polynomino && !(lastMovedObject instanceof Polynomino) && !objects[touchedX][touchedY].isLocked && !objects[touchedX][touchedY].allLocked){
                    movePolynomio(touchedX, touchedY, LEFT);
                }
            }
            else if(activity.swipedDirection.equals(Simulation.UP)){
                activity.swipedDirection = null;
                if(objects[touchedX][touchedY] instanceof Player && !objects[touchedX][touchedY].isLocked){
                    movePlayer(touchedX, touchedY, UP);
                }
                if(objects[touchedX][touchedY] instanceof Polynomino && !(lastMovedObject instanceof Polynomino) && !objects[touchedX][touchedY].isLocked && !objects[touchedX][touchedY].allLocked){
                    movePolynomio(touchedX, touchedY, UP);
                }
            }
            else if(activity.swipedDirection.equals(Simulation.DOWN)){
                activity.swipedDirection = null;
                if(objects[touchedX][touchedY] instanceof Player && !objects[touchedX][touchedY].isLocked){
                    movePlayer(touchedX, touchedY, DOWN);
                }
                if(objects[touchedX][touchedY] instanceof Polynomino && !(lastMovedObject instanceof Polynomino) && !objects[touchedX][touchedY].isLocked && !objects[touchedX][touchedY].allLocked){
                    movePolynomio(touchedX, touchedY, DOWN);
                }
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

            if(direction.equals(RIGHT)){
                objects[x][y].isMovingRight = true;
                objects[x+1][y] = objects[x][y];
                objects[x][y] = null;
            }
            if(direction.equals(LEFT)){
                objects[x][y].isMovingLeft = true;
                objects[x-1][y] = objects[x][y];
                objects[x][y] = null;
            }
            if(direction.equals(UP)){
                objects[x][y].isMovingUp = true;
                objects[x][y+1] = objects[x][y];
                objects[x][y] = null;
            }
            if(direction.equals(DOWN)){
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
                if(direction.equals(RIGHT)){
                    if((x+1 > PLAYGROUND_MAX_X || objects[x+1][y] != null)){
                        collision = true;
                    }
                }
                if(direction.equals(LEFT)){
                    if((x-1 < PLAYGROUND_MIN_X || objects[x-1][y] != null)){
                        collision = true;
                    }
                }
                if(direction.equals(UP)){
                    if((y+1 > PLAYGROUND_MAX_Y || objects[x][y+1] != null)){
                        collision = true;
                    }
                }
                if(direction.equals(DOWN)){
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
        if(lastMovedObject != objects[x][y]) {
            objects[x][y].start_position = new Vector(x, y, 0);
        }
        Player player = (Player) objects[x][y];
        objects[x][y].block_position = new Vector(x,y,0);
        while(!predictCollision(x, y, direction)){
            moveObject(x, y, direction);
            if(direction.equals(RIGHT)){
                if(x+1 == player.start_position.x && y == player.start_position.y) {
                    break;
                }else{
                    x++;
                }
            }
            else if(direction.equals(LEFT)){
                if(x-1 == player.start_position.x && y == player.start_position.y){
                    break;
                }else{
                    x--;
                }
            }
            else if(direction.equals(UP)){
                if(y+1 == player.start_position.y && x == player.start_position.x){
                    break;
                }else {
                    y++;
                }
            }
            else if(direction.equals(DOWN)){
                if(y-1 == player.start_position.y && x == player.start_position.x){
                    break;
                }else {
                    y--;
                }
            }
        }
    }

    public void checkPlayerPosition(){
        for(int i = 0; i < objects.length; i++){
            for(int j = 0; j < objects[0].length; j++){
                if(objects[i][j] instanceof Polynomino){
                    Polynomino polynomino = (Polynomino) objects[i][j];
                    polynomino.isRendered = false;
                    if(allLocked){
                        polynomino.allLocked = true;
                    }
                }
                if(objects[i][j] instanceof Player) {
                    if (!objects[i][j].isLocked  && lastMovedObject instanceof Polynomino && (predictCollision(i, j, RIGHT) && predictCollision(i, j, LEFT) && predictCollision(i, j, UP) && predictCollision(i, j, DOWN))) {
                        if (objects[i][j].isPlayerOne) {
                            player.isLockedIn = true;
                            lastMovedObject = player;
                        } else if (!objects[i][j].isPlayerOne) {
                            player2.isLockedIn = true;
                            lastMovedObject = player2;
                        }
                    } else if (lastMovedObject != null && objects[i][j] == lastMovedObject) {
                        if (!objects[i][j].isMovingRight && !objects[i][j].isMovingLeft && !objects[i][j].isMovingUp && !objects[i][j].isMovingDown) {
                            if (objects[i][j].isPlayerOne && i == PLAYGROUND_MAX_X) {
                                setWinner((Player) objects[i][j]);
                            } else if (!objects[i][j].isPlayerOne && i == PLAYGROUND_MIN_X) {
                                setWinner((Player) objects[i][j]);
                            } else if ((predictCollision(i, j, UP) && objects[i][j].lastState.equals(UP)) || (predictCollision(i, j, DOWN) && objects[i][j].lastState.equals(DOWN))) {
                                if (j + 1 < objects[0].length && objects[i][j + 1] instanceof Player && !predictCollision(i, j + 1, UP)) {
                                    movePlayer(i, j + 1, UP);
                                } else if (j - 1 >= 0 && objects[i][j - 1] instanceof Player && !predictCollision(i, j - 1, DOWN)) {
                                    movePlayer(i, j - 1, DOWN);
                                } else if (!predictCollision(i, j, RIGHT) && predictCollision(i, j, LEFT)) {
                                    movePlayer(i, j, RIGHT);
                                } else if (predictCollision(i, j, RIGHT) && !predictCollision(i, j, LEFT)) {
                                    movePlayer(i, j, LEFT);
                                }
                            } else if ((predictCollision(i, j, RIGHT) && objects[i][j].lastState.equals(RIGHT)) || (predictCollision(i, j, LEFT) && objects[i][j].lastState.equals(LEFT))) {
                                if (i + 1 < objects.length && objects[i + 1][j] instanceof Player && !predictCollision(i + 1, j, RIGHT)){
                                    movePlayer(i + 1, j, RIGHT);
                                } else if (i - 1 >= 0 && objects[i - 1][j] instanceof Player && !predictCollision(i - 1, j, LEFT)) {
                                    movePlayer(i - 1, j, LEFT);
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
        checkPlayerPosition();

    }
}
