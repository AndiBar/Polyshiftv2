package de.polyshift.polyshift.Game.Interfaces;

import javax.microedition.khronos.opengles.GL10;

import de.polyshift.polyshift.Game.GameActivity;

public interface GameScreen 
{
	public void update( GameActivity activity );
	public void render( GL10 gl, GameActivity activity );
	public boolean isDone( );
	
}