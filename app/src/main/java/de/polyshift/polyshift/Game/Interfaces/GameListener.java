package de.polyshift.polyshift.Game.Interfaces;

import javax.microedition.khronos.opengles.GL10;

import de.polyshift.polyshift.Game.GameActivity;

public interface GameListener {
	
	public void setup(GameActivity activity, GL10 gl);
	
	public void mainLoopIteration(GameActivity activity, GL10 gl);

}
