package de.polyshift.polyshift.Game.Objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import de.polyshift.polyshift.Game.Renderer.Mesh;
import de.polyshift.polyshift.Game.Renderer.Vector;

public class Polynomino extends GameObject implements Serializable{
	
	public ArrayList<Block>blocks = new ArrayList<Block>();
	public int size = 0;
	public float movingVelocity = 0.02f;
    public Vector pixel_position = new Vector(0,0,0);
    public Vector border_pixel_position = new Vector(0,0,0);
    public  float []colors = new float[4];
	public boolean isRendered = false;
    private Mesh mesh;

	public Polynomino(float[] color){
		colors[0] = color[0];
		colors[1] = color[1];
		colors[2] = color[2];
		colors[3] = color[3];
	}
	
	public void addBlock(Block block){
		blocks.add(block);
	}

	public void sortBlocks(String direction){
		if(direction.equals("left")){
			Collections.sort(blocks);
		}
		if(direction.equals("right")){
			Collections.sort(blocks, Collections.reverseOrder());
		}
		if(direction.equals("up")){
			Collections.sort(blocks, Collections.reverseOrder(new BlockComparator()));
		}
		if(direction.equals("down")){
			Collections.sort(blocks, new BlockComparator());
		}
	}

	public void blockCounter() {
		this.size++;
	}

}
	
	