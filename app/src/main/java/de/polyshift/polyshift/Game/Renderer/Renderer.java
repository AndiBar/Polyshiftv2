package de.polyshift.polyshift.Game.Renderer;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import de.polyshift.polyshift.Game.GameActivity;
import de.polyshift.polyshift.Game.Objects.GameObject;
import de.polyshift.polyshift.Game.Renderer.Mesh;
import de.polyshift.polyshift.Game.Renderer.Texture;

/**
 * Diese Klasse stellt eine Schnittstelle zur Implementierung von Renderern zur Verfügung.
 *
 * @author helmsa
 *
 */

public abstract class Renderer {
	
	float block_width;
	float block_height;
	float object_width;
	float object_height;
	int count = 0;
	Texture texturePlayerOne;
	Texture texturePlayerTwo;
	Texture texturePlayerOneLock;
	Texture texturePlayerTwoLock;
	Texture textureLocker;
	ArrayList<Mesh> coordinates_list;

	public abstract void renderObjects(GameActivity activity, GL10 gl, GameObject[][] objects);
	
	public abstract void setPerspective(GameActivity activity, GL10 gl);
	
	public abstract void enableCoordinates(GL10 gl, GameObject[][] objects);
	
	public abstract void renderLight(GL10 gl);
}