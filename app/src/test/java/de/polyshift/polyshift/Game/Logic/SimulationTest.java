package de.polyshift.polyshift.Game.Logic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import de.polyshift.polyshift.BuildConfig;

import static org.junit.Assert.*;

/**
 * Created by Andi on 12.03.2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class SimulationTest {

    private Simulation simulation;

    @Before
    public void setUp() throws Exception {
        simulation = new Simulation(null);
    }

    @Test
    public void populate() throws Exception {
        simulation.populate();
    }

    @Test
    public void setGameObject() throws Exception {

    }

    @Test
    public void getTouch() throws Exception {

    }

    @Test
    public void moveObject() throws Exception {

    }

    @Test
    public void predictCollision() throws Exception {

    }

    @Test
    public void movePolynomio() throws Exception {

    }

    @Test
    public void movePlayer() throws Exception {

    }

    @Test
    public void predictNextMovement() throws Exception {

    }

    @Test
    public void checkPlayerPosition() throws Exception {

    }

    @Test
    public void setWinner() throws Exception {

    }

    @Test
    public void update() throws Exception {

    }

}