package de.polyshift.polyshift.Game;

import java.io.Serializable;

public class Block extends GameObject implements Comparable<Block>,Serializable{
	public int x;
	public int y;
    public Vector block_position;
    public Vector pixel_position;
	
	public Block(int x, int y){
		this.x = x;
		this.y = y;	
		this.pixel_position = new Vector(0,0,0);
		this.block_position = new Vector(0,0,0);
	}

	@Override
	public int compareTo(Block another) {
		
		return Integer.compare(this.x,another.x);
	}
}

