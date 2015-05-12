package hamburg.haw.polyshift.Game;

import java.io.Serializable;

public class Player extends GameObject implements Serializable {

    public Vector block_position;
    public Vector pixel_position;
    private Mesh mesh;

	public Player (boolean isPlayerOne){
		this.isPlayerOne =  isPlayerOne;
		
	}

}
