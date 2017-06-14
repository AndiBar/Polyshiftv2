package de.polyshift.polyshift.Game.Objects;

import java.io.Serializable;

import de.polyshift.polyshift.Game.Renderer.Mesh;
import de.polyshift.polyshift.Game.Renderer.Vector;

/**
 * Speichert die Position, das Mesh und andere Eigenschaften eines Spielers
 *
 * @author helmsa
 *
 */

public class Player extends GameObject implements Serializable {

    public Vector block_position;
    public Vector pixel_position;
    private Mesh mesh;
    public boolean isLockedIn = false;
    public boolean isPlayerOne = false;

	public Player (boolean isPlayerOne){
		this.isPlayerOne =  isPlayerOne;
		
	}

}
