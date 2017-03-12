package de.polyshift.polyshift.Game.Logic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import de.polyshift.polyshift.BuildConfig;

import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * Created by Andi on 12.03.2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class AiGameLoopTest {

    @Mock Simulation simulation;

    private AiGameLoop aiGameLoop;

    @Before
    public void setUp() throws Exception {
        aiGameLoop = new AiGameLoop("yes");

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
        AiGameLoop.doRandomPlayerMovement(simulation);
        verify(simulation).predictCollision(x,y,any(String.class));
    }

    @Test
    public void serializeSimulation() throws Exception {

    }

    @Test
    public void deserializeSimulation() throws Exception {

    }

    @Test
    public void doPolynominoAndPlayerMovement() throws Exception {

    }

    @Test
    public void checkIfPlayerWins() throws Exception {

    }

    @Test
    public void doPolinominoMovement() throws Exception {

    }

}