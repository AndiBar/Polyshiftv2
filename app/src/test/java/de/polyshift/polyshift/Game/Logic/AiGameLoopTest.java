package de.polyshift.polyshift.Game.Logic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import de.polyshift.polyshift.BuildConfig;
import de.polyshift.polyshift.Game.Objects.Block;
import de.polyshift.polyshift.Game.Objects.GameObject;
import de.polyshift.polyshift.Game.Objects.Player;
import de.polyshift.polyshift.Game.Objects.Polynomino;
import de.polyshift.polyshift.Game.Renderer.Vector;

import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * Created by Andi on 12.03.2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,sdk= 21,  manifest = "src/main/AndroidManifest.xml")
public class AiGameLoopTest {

    String simulationString = "rO0ABXNyACZkZS5wb2x5c2hpZnQucG9seXNoaWZ0LkdhbWUuU2ltdWxhdGlvbkMTSABPAl82AgAWSQAQUExBWUdST1VORF9NQVhfWEkAEFBMQVlHUk9VTkRfTUFYX1lJABBQTEFZR1JPVU5EX01JTl9YSQAQUExBWUdST1VORF9NSU5fWUkAE1BMQVlHUk9VTkRfUE9QVUxBVEVJAA5QT0xZTk9NSU9fU0laRVoACWFsbExvY2tlZFoADWJ1bXBfZGV0ZWN0ZWRaABJnYW1lT2JqZWN0U2VsZWN0ZWRaAAloYXNXaW5uZXJaAA1sb29wX2RldGVjdGVkRgAGc3dpcGVYRgAGc3dpcGVZSQAIdG91Y2hlZFhJAAh0b3VjaGVkWUwAD2xhc3RNb3ZlZE9iamVjdHQAKExkZS9wb2x5c2hpZnQvcG9seXNoaWZ0L0dhbWUvR2FtZU9iamVjdDtMABNsYXN0TW92ZWRQb2x5bm9taW5vdAAoTGRlL3BvbHlzaGlmdC9wb2x5c2hpZnQvR2FtZS9Qb2x5bm9taW5vO1sAB29iamVjdHN0ACpbW0xkZS9wb2x5c2hpZnQvcG9seXNoaWZ0L0dhbWUvR2FtZU9iamVjdDtMAAZwbGF5ZXJ0ACRMZGUvcG9seXNoaWZ0L3BvbHlzaGlmdC9HYW1lL1BsYXllcjtMAAdwbGF5ZXIycQB+AARMAAtwb2x5bm9taW5vc3QAFUxqYXZhL3V0aWwvQXJyYXlMaXN0O0wABndpbm5lcnEAfgAEeHAAAAAQAAAACAAAAAAAAAAAAAAACAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAABwcHVyACpbW0xkZS5wb2x5c2hpZnQucG9seXNoaWZ0LkdhbWUuR2FtZU9iamVjdDunXveZTDzoyAIAAHhwAAAAEXVyAClbTGRlLnBvbHlzaGlmdC5wb2x5c2hpZnQuR2FtZS5HYW1lT2JqZWN0OyZD15H4MUyeAgAAeHAAAAAJcHBwcHNyACJkZS5wb2x5c2hpZnQucG9seXNoaWZ0LkdhbWUuUGxheWVy0K/ivc/7Jh8CAARaAAppc0xvY2tlZEluTAAOYmxvY2tfcG9zaXRpb250ACRMZGUvcG9seXNoaWZ0L3BvbHlzaGlmdC9HYW1lL1ZlY3RvcjtMAARtZXNodAAiTGRlL3BvbHlz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";

    Simulation simulation;

    private AiGameLoop aiGameLoop;

    @Before
    public void setUp() throws Exception {
        aiGameLoop = new AiGameLoop("yes");
        simulation = new Simulation(null);
        simulationString = aiGameLoop.serializeSimulation(simulation);
    }

    @Test
    public void setRandomPlayer() throws Exception {
        aiGameLoop.setRandomPlayer();
    }

    @Test
    public void update() throws Exception {

    }

    @Test
    public void doRandomPlayerMovement() throws Exception {
        int x = (int) simulation.player2.block_position.x;
        int y = (int) simulation.player2.block_position.y;
        Simulation simulation = AiGameLoop.doRandomPlayerMovement(this.simulation);
        int x2 = (int) simulation.player2.block_position.x;
        int y2 = (int) simulation.player2.block_position.y;
        assertTrue(x != x2 || y != y2);
    }

    @Test
    public void serializeSimulation() throws Exception {
        String simulationString = aiGameLoop.serializeSimulation(simulation);
        assertTrue(simulationString.equals(this.simulationString));
    }

    @Test
    public void deserializeSimulation() throws Exception {
        Simulation simulation = aiGameLoop.deserializeSimulation(simulationString);
        assertThat(simulation).isEqualTo(simulation);
    }

    @Test
    public void doPolynominoAndPlayerMovement() throws Exception {
        String oldSimulationString = aiGameLoop.serializeSimulation(simulation);
        simulation.player.block_position = new Vector();
        simulation.player.block_position.x = 0;
        simulation.player.block_position.y = Simulation.PLAYGROUND_MAX_Y / 2;
        int x = (int) simulation.player2.block_position.x;
        int y = (int) simulation.player2.block_position.y;
        Simulation simulation = aiGameLoop.doPolynominoAndPlayerMovement(this.simulation);
        int x2 = (int) simulation.player2.block_position.x;
        int y2 = (int) simulation.player2.block_position.y;
        assertThat(simulation.player2.block_position).isNotNull();
        assertTrue(x != x2 || y != y2);
        Polynomino polynomino = simulation.lastMovedPolynomino;
        ArrayList<Block> blocks = simulation.lastMovedPolynomino.blocks;
        assertThat(polynomino).isNotNull();
        Simulation oldSimulation = aiGameLoop.deserializeSimulation(oldSimulationString);
        assertThat(simulation.lastMovedPolynomino.blocks).isNotNull();
        boolean moved = false;
        for(Block block : blocks){
            assertThat(simulation.objects[block.x][block.y]).isNotNull();
            if(simulation.objects[block.x][block.y] != oldSimulation.objects[block.x][block.y]){
                moved = true;
            }
            assertThat(simulation.objects[block.x][block.y]).
                    isNotEqualTo(oldSimulation.objects[block.x][block.y]);
        }
        assertThat(moved).isTrue();
    }

    @Test
    public void checkIfPlayerWins() throws Exception {
        int playerX = Simulation.PLAYGROUND_MAX_X-2;
        int playerY = Simulation.PLAYGROUND_MAX_Y;
        Simulation simulation = new Simulation(null);
        Player player1 = (Player) simulation.objects[Simulation.PLAYGROUND_MIN_X][Simulation.PLAYGROUND_MAX_Y/2];
        simulation.objects[Simulation.PLAYGROUND_MIN_X][Simulation.PLAYGROUND_MAX_Y/2] = null;
        simulation.player.block_position = new Vector();
        simulation.player.block_position.x = Simulation.PLAYGROUND_MAX_X-2;
        simulation.player.block_position.y = Simulation.PLAYGROUND_MAX_Y;
        simulation.player.start_position.x = Simulation.PLAYGROUND_MAX_X-2;
        simulation.player.start_position.y = Simulation.PLAYGROUND_MAX_Y;
        simulation.player.isPlayerOne = true;
        player1.isPlayerOne = true;
        simulation.objects[playerX][playerY] = player1;
        boolean wins = aiGameLoop.checkIfPlayerWins(simulation);
        assertThat(simulation.objects[Simulation.PLAYGROUND_MAX_X-2][playerY]).isNotNull();
        assertThat(wins).isTrue();
    }

    @Test
    public void checkIfPlayerNotWins() throws Exception {
        Simulation simulation = new Simulation(null);
        simulation.player.block_position = new Vector();
        simulation.player.block_position.x = 0;
        simulation.player.block_position.y = Simulation.PLAYGROUND_MAX_Y/2;
        boolean wins = aiGameLoop.checkIfPlayerWins(simulation);
        assertThat(simulation.objects[0][Simulation.PLAYGROUND_MAX_Y/2]).isNotNull();
        assertThat(wins).isFalse();
    }

    @Test
    public void checkIfPlayerWins2() throws Exception {
        int playerX = Simulation.PLAYGROUND_MAX_X-2;
        int playerY = (Simulation.PLAYGROUND_MAX_Y/2) -1;
        Simulation simulation = new Simulation(null);
        Player player1 = (Player) simulation.objects[Simulation.PLAYGROUND_MIN_X][Simulation.PLAYGROUND_MAX_Y/2];
        simulation.objects[Simulation.PLAYGROUND_MIN_X][Simulation.PLAYGROUND_MAX_Y/2] = null;
        simulation.player.block_position = new Vector();
        simulation.player.block_position.x = Simulation.PLAYGROUND_MAX_X-2;
        simulation.player.block_position.y = playerY;
        simulation.player.start_position.x = Simulation.PLAYGROUND_MAX_X-2;
        simulation.player.start_position.y = playerY;
        simulation.objects[playerX][playerY] = player1;
        player1.block_position.x = Simulation.PLAYGROUND_MAX_X-2;
        player1.block_position.y = playerY;
        player1.isPlayerOne = true;
        boolean wins = aiGameLoop.checkIfPlayerWins(simulation);
        assertThat(simulation.objects[Simulation.PLAYGROUND_MAX_X-2][playerY]).isNotNull();
        assertThat(wins).isTrue();
    }

    @Test
    public void doPolinominoMovement() throws Exception {

    }

    @Test
    public void checkPlayerPosition(){
        Simulation simulation = new Simulation(null);
        //Player1
        GameObject player1 = simulation.objects[Simulation.PLAYGROUND_MIN_X][Simulation.PLAYGROUND_MAX_Y/2];
        simulation.objects[Simulation.PLAYGROUND_MIN_X][Simulation.PLAYGROUND_MAX_Y/2] = null;
        simulation.player.block_position = new Vector();
        simulation.player.block_position.x = Simulation.PLAYGROUND_MAX_X-1;
        simulation.player.block_position.y = 0;
        simulation.player.start_position.x = Simulation.PLAYGROUND_MAX_X-1;
        simulation.player.start_position.y = 0;
        player1.block_position = new Vector();
        player1.block_position.x = Simulation.PLAYGROUND_MAX_X-1;
        player1.block_position.y = 0;
        simulation.objects[Simulation.PLAYGROUND_MAX_X-1][0] = player1;
        simulation.lastMovedObject = player1;
        player1.lastState = Simulation.DOWN;

        //Player2
        GameObject player2 = simulation.objects[Simulation.PLAYGROUND_MAX_X][Simulation.PLAYGROUND_MAX_Y/2];
        simulation.objects[Simulation.PLAYGROUND_MAX_X][Simulation.PLAYGROUND_MAX_Y/2] = null;
        simulation.player.block_position = new Vector();
        simulation.objects[Simulation.PLAYGROUND_MAX_X-2][(0)] = player2;

        aiGameLoop.checkPlayerPosition(simulation);
        assertThat(simulation.objects[Simulation.PLAYGROUND_MAX_X][0]).isNotNull();
        assertTrue(simulation.objects[Simulation.PLAYGROUND_MAX_X][0] instanceof  Player);
        assertTrue(((Player) simulation.objects[Simulation.PLAYGROUND_MAX_X][0]).isPlayerOne);
    }

}