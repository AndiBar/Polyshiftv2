package de.polyshift.polyshift.Game.Renderer;

import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLU;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;

import de.polyshift.polyshift.Game.GameActivity;
import de.polyshift.polyshift.Game.Logic.Simulation;
import de.polyshift.polyshift.Game.Objects.GameObject;
import de.polyshift.polyshift.Game.Objects.Player;
import de.polyshift.polyshift.Game.Objects.Polynomino;
import de.polyshift.polyshift.Game.Renderer.Mesh.PrimitiveType;

/**
 * Diese Klasse rendert das in der Simulation enthaltene Spielfeld und stellt es dreidimesional
 * dar.
 *
 * @author helmsa
 *
 */

public class Renderer3D extends Renderer {
	
	float object_depth;
	float added_depth;
	float width;
	float height;
	ArrayList<Mesh> finish_list;
	int count;
    Texture border_vertical;


	public Renderer3D(final GameActivity activity, GL10 gl, GameObject[][] objects){

        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float display_x = metrics.widthPixels;
        float display_y = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getHeight();

        width = 6.4f*(display_x/display_y);
        height = 14.7f/1.5f/1.5f;

		block_width = width / objects.length;
		block_height = height / objects[0].length;

		object_width = width / objects.length;
		object_height = height / objects[0].length;

		object_depth = -0.5f;

		finish_list = new ArrayList<Mesh>();

        Bitmap bitmap_border_vertical = null;

		gl.glEnable(GL10.GL_DEPTH_TEST);

		Mesh blockMesh = null;
		try {
			blockMesh = MeshLoader.loadObj(gl, activity.getAssets().open( "block.obj" ) );
		} catch (IOException e1) {
			Log.d("Fehler:","Error loading cube");
		}

        for(int i = 0; i < objects.length; i++){
			for(int j = 0; j < objects[i].length; j++){
				if(objects[i][j] instanceof Player){
					try {

						Mesh mesh;
						mesh = MeshLoader.loadObj(gl, activity.getAssets().open( "sphere.obj" ) );
                        Player player = (Player) objects[i][j];
						player.setMesh(mesh);
                        objects[i][j] = player;

					} catch (IOException e1) {
						Log.d("Fehler:","..loading sphere");
					}
				}
				if(objects[i][j] instanceof Polynomino){
					Polynomino polynomio = (Polynomino) objects[i][j];
					polynomio.setMesh(blockMesh);

                    float line_depth = 0.04f;
                    if (i + 1 < objects.length && objects[i + 1][j] != objects[i][j]) {
                        Mesh border_mesh = new Mesh(gl, 2, true, false, false);
                        border_mesh.color(0f, 0f, 0f, 1);
                        border_mesh.vertex(0, 0, line_depth);
                        border_mesh.color(0f, 0f, 0f, 1);
                        border_mesh.vertex(0, block_height, line_depth);
                        polynomio.setBorder(border_mesh);
                        polynomio.setBorderPixel(new Vector(block_width * (i + 1), block_height * j, line_depth));
                        polynomio.isRendered = true;
                    }
                    if (i - 1 >= 0 && objects[i - 1][j] != objects[i][j]) {
                        Mesh border_mesh = new Mesh(gl, 2, true, false, false);
                        border_mesh.color(0f, 0f, 0f, 1);
                        border_mesh.vertex(0, 0, line_depth);
                        border_mesh.color(0f, 0f, 0f, 1);
                        border_mesh.vertex(0, block_height, line_depth);
                        polynomio.setBorder(border_mesh);
                        polynomio.setBorderPixel(new Vector(block_width * i, block_height * j, line_depth));
                        polynomio.isRendered = true;

                    }
                    if (j + 1 < objects[0].length && objects[i][j + 1] != objects[i][j]) {
                        Mesh border_mesh = new Mesh(gl, 2, true, false, false);
                        border_mesh.color(0f, 0f, 0f, 1);
                        border_mesh.vertex(0, 0, line_depth);
                        border_mesh.color(0f, 0f, 0f, 1);
                        border_mesh.vertex(block_width, 0, line_depth);
                        polynomio.setBorder(border_mesh);
                        polynomio.setBorderPixel(new Vector(block_width * i, block_height * (j + 1), line_depth));
                        polynomio.isRendered = true;
                    }
                    if (j - 1 >= 0 && objects[i][j - 1] != objects[i][j]) {
                        Mesh border_mesh = new Mesh(gl, 2, true, false, false);
                        border_mesh.color(0.4f, 0.4f, 0.4f, 1);
                        border_mesh.vertex(0, 0, line_depth);
                        border_mesh.color(0.4f, 0.4f, 0.4f, 1);
                        border_mesh.vertex(block_width, 0, line_depth);
                        polynomio.setBorder(border_mesh);
                        polynomio.setBorderPixel(new Vector(block_width * i, block_height * j, line_depth));
                        polynomio.isRendered = true;
                    }
                }
			}
		}
	}
	
	public void setPerspective(GameActivity activity, GL10 gl){    
        gl.glViewport( 0, 0, activity.getViewportWidth(), activity.getViewportHeight() );
        gl.glClearColor(209f/255f, 223f/255f, 230f/255f, 0f);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT  | GL10.GL_DEPTH_BUFFER_BIT);
		
		gl.glMatrixMode( GL10.GL_PROJECTION );
		gl.glLoadIdentity();
		float aspectRatio = (float)activity.getViewportWidth() / activity.getViewportHeight();
		GLU.gluPerspective( gl, 67, aspectRatio, 1, 100 );
        GLU.gluLookAt(gl, 3.2f*aspectRatio, 3.25f, 4.8f, 3.2f*aspectRatio, 3.25f, -5f, 0f, 1f, 0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
	}

	
	public void renderObjects(GameActivity activity, GL10 gl, GameObject[][] objects){
		
		if(coordinates_list != null){
			for(Mesh mesh : coordinates_list){
				mesh.render( PrimitiveType.Lines );
			}
		}
		
		if(finish_list != null){
			for(Mesh mesh : finish_list){
				mesh.render( PrimitiveType.TriangleStrip );
			}
		}
	
		for(int i = 0; i < objects.length; i++){
			for(int j = 0; j < objects[i].length; j++){
				if(objects[i][j] instanceof Player){
					Player player = (Player) objects[i][j];
					if(player.isPlayerOne){
						gl.glColor4f((51f/255f*1.5f),(77f/255*1.5f),(100f/255f*1.5f),1f);
						if(objects[i][j].isLocked){
							
							gl.glColor4f((51f/255f/1.5f),(77f/255/1.5f),(92f/255f/1.5f),1f);
						}
						else if(objects[i][j].isSelected){
							gl.glColor4f((51f/255f*1.9f),(77f/255*1.9f),(92f/255f*1.9f),1f);
						}
					}else{
						gl.glColor4f((223f/255f),(73f/255f),(73f/255f),1.0f);
						if(objects[i][j].isLocked){
							
							gl.glColor4f((223f/255f/2.5f),(73f/255f/2.5f),(73f/255f/2.5f),1f);
						}
						else if(objects[i][j].isSelected){
							gl.glColor4f((223f/255f*1.3f),(73f/255f*1.3f),(73f/255f*1.3f),1.0f);
						}
					}
					if(objects[i][j].isMovingLeft){
						if(objects[i][j].pixel_position.x == -1){
							objects[i][j].pixel_position.x = objects[i][j].block_position.x*block_width;
						}
						if(objects[i][j].pixel_position.x > i*block_width){
							renderPlayer(gl, objects[i][j],objects[i][j].pixel_position.x, j*block_height, 0 );

							objects[i][j].pixel_position.x -= block_width * objects[i][j].movingVelocity;
						}
						else{
							objects[i][j].isMovingLeft = false;
							objects[i][j].lastState = Simulation.LEFT;
							objects[i][j].pixel_position.x = -1;
						}
					}
					else if(objects[i][j].isMovingRight){
						if(objects[i][j].pixel_position.x == -1){
							objects[i][j].pixel_position.x = objects[i][j].block_position.x*block_width;
						}
						if(objects[i][j].pixel_position.x < i*block_width){
							renderPlayer(gl, objects[i][j],objects[i][j].pixel_position.x, j*block_height, 0 );
							objects[i][j].pixel_position.x += block_width * objects[i][j].movingVelocity;
						}
						else{
							objects[i][j].isMovingRight = false;
							objects[i][j].lastState = Simulation.RIGHT;
							objects[i][j].pixel_position.x = -1;
						}
					}
					else if(objects[i][j].isMovingUp){
						if(objects[i][j].pixel_position.y == -1){
							objects[i][j].pixel_position.y = objects[i][j].block_position.y*block_height;
						}
						if(objects[i][j].pixel_position.y < j*block_height){
							renderPlayer(gl, objects[i][j],i*block_width,objects[i][j].pixel_position.y, 0 );
							objects[i][j].pixel_position.y += block_height * objects[i][j].movingVelocity;
						}
						else{
							objects[i][j].isMovingUp = false;
							objects[i][j].lastState = Simulation.UP;
							objects[i][j].pixel_position.y = -1;
						}
					}
					else if(objects[i][j].isMovingDown){
						if(objects[i][j].pixel_position.y == -1){
							objects[i][j].pixel_position.y = objects[i][j].block_position.y*block_height;
						}
						if(objects[i][j].pixel_position.y > j*block_height){
							renderPlayer(gl, objects[i][j],i*block_width,objects[i][j].pixel_position.y, 0 );
							objects[i][j].pixel_position.y -= block_height * objects[i][j].movingVelocity;
						}
						else{
							objects[i][j].isMovingDown = false;
							objects[i][j].lastState = Simulation.DOWN;
							objects[i][j].pixel_position.y = -1;
						}
					}
					else{
						renderPlayer(gl, objects[i][j],i*block_width, j*block_height, 0 );
					}
					
					gl.glDisable(GL10.GL_BLEND);
					gl.glEnable(GL10.GL_COLOR_MATERIAL);
					gl.glColor4f(1, 1, 1, 1);
				}
				if(objects[i][j] instanceof Polynomino) {
                    Polynomino polynomino = (Polynomino) objects[i][j];
                    if (polynomino.isMovingRight) {
                        polynomino.pixel_position.x += (i - polynomino.blocks.get(polynomino.blocks.size() - 1).block_position.x) * block_width;
                        float temp_x = polynomino.border_pixel_list.get(0).x;
                        polynomino.border_pixel_position.x = temp_x;
                        for(Vector vector : polynomino.border_pixel_list){
                            if(vector.x > temp_x) {
                                polynomino.border_pixel_position.x += (vector.x - (polynomino.blocks.get(polynomino.blocks.size() - 1).block_position.x) * block_height);
                            }
                        }
                        polynomino.isMovingRight = false;
                    } else if (polynomino.isMovingLeft) {
                        polynomino.pixel_position.x += (i - polynomino.blocks.get(0).block_position.x) * block_width;
                        float temp_x = polynomino.border_pixel_list.get(0).x;
                        polynomino.border_pixel_position.x = temp_x;
                        for(Vector vector : polynomino.border_pixel_list){
                            if(vector.x < temp_x) {
                                polynomino.border_pixel_position.x += (vector.x - (polynomino.blocks.get(0).block_position.y) * block_height);
                            }
                        }
                        polynomino.isMovingLeft = false;
                    } else if (polynomino.isMovingUp) {
                        polynomino.pixel_position.y += (j - polynomino.blocks.get(polynomino.blocks.size() - 1).block_position.y) * block_height;
                        float temp_y = polynomino.border_pixel_list.get(0).y;
                        polynomino.border_pixel_position.y = temp_y;
                        for(Vector vector : polynomino.border_pixel_list){
                            if(vector.y > temp_y) {
                                polynomino.border_pixel_position.y += (vector.y - (polynomino.blocks.get(polynomino.blocks.size() - 1).block_position.y) * block_height);
                            }
                        }
                        polynomino.isMovingUp = false;
                    } else if (polynomino.isMovingDown) {
                        polynomino.pixel_position.y += (j - polynomino.blocks.get(0).block_position.y) * block_height;
                        float temp_y = polynomino.border_pixel_list.get(0).y;
                        polynomino.border_pixel_position.y = temp_y;
                        for(Vector vector : polynomino.border_pixel_list){
                            if(vector.y < temp_y) {
                                polynomino.border_pixel_position.y += (vector.y - (polynomino.blocks.get(0).block_position.y) * block_height);
                            }
                        }
                        polynomino.isMovingDown = false;
                    } else {
                        polynomino.pixel_position.x = i * block_width;
                        polynomino.pixel_position.y = j * block_height;
                        polynomino.pixel_position.z = 0;
                    }
                    if (polynomino.isLocked || polynomino.allLocked) {
                        gl.glColor4f(objects[i][j].colors[0] / 2.5f, objects[i][j].colors[1] / 2.5f, objects[i][j].colors[2] / 2.5f, 0.5f);
                        blockRenderer(gl, polynomino, polynomino.pixel_position.x, polynomino.pixel_position.y, polynomino.pixel_position.z);
                        gl.glColor4f(1, 1, 1, 1);
                        if (!polynomino.isRendered) {
                            for (int k = 0; k < polynomino.border_list.size(); k++) {
                                gl.glPushMatrix();
                                polynomino.border_pixel_list.set(k, new Vector(polynomino.border_pixel_list.get(k).x + polynomino.border_pixel_position.x,polynomino.border_pixel_list.get(k).y + polynomino.border_pixel_position.y,0));
                                gl.glTranslatef(polynomino.border_pixel_list.get(k).x, polynomino.border_pixel_list.get(k).y, 0);
                                polynomino.border_list.get(k).render(PrimitiveType.Lines);
                                gl.glPopMatrix();
                            }
                            polynomino.isRendered = true;
                        }
                    } else {
						if(polynomino.isSelected){
							gl.glColor4f(objects[i][j].colors[0] * 1.5f, objects[i][j].colors[1] * 1.5f, objects[i][j].colors[2] * 1.5f, 0.5f);
						}else {
							gl.glColor4f(objects[i][j].colors[0], objects[i][j].colors[1], objects[i][j].colors[2], 0.5f);
						}
						blockRenderer(gl, polynomino, polynomino.pixel_position.x, polynomino.pixel_position.y, polynomino.pixel_position.z);
                        gl.glColor4f(1, 1, 1, 1);
                        if (!polynomino.isRendered) {
                            for (int k = 0; k < polynomino.border_list.size(); k++) {
                                gl.glPushMatrix();
                                polynomino.border_pixel_list.set(k, new Vector(polynomino.border_pixel_list.get(k).x + polynomino.border_pixel_position.x,polynomino.border_pixel_list.get(k).y + polynomino.border_pixel_position.y,0));
                                gl.glTranslatef(polynomino.border_pixel_list.get(k).x, polynomino.border_pixel_list.get(k).y, 0);
                                polynomino.border_list.get(k).render(PrimitiveType.Lines);
                                gl.glPopMatrix();
                            }
                            polynomino.isRendered = true;
                        }
                    }
                }
			}
		}
	}
	
	private void blockRenderer(GL10 gl,GameObject polynomio, float x, float y, float z){
		gl.glPushMatrix();
		gl.glTranslatef(x+(block_width/2),y+(block_height/2),z+(object_depth/2) );
		gl.glScalef(object_width*0.88f,object_height*3.4f,object_depth);
        Polynomino poly = (Polynomino) polynomio;
		poly.getMesh().render(PrimitiveType.Triangles);
		gl.glPopMatrix();
	}
	public void renderPlayer(GL10 gl, GameObject player, float x, float y, float z){
		gl.glPushMatrix();
		gl.glTranslatef(x+(block_width/2),y+(block_height/2),z+(object_depth/2) );
		gl.glScalef(object_width/2.2f,object_height/2.2f,object_depth/2.2f);
        Player play = (Player) player;
		play.getMesh().render(PrimitiveType.Triangles);
		gl.glPopMatrix();
		
	}
	
	public void enableCoordinates(GL10 gl, GameObject[][] objects){
		coordinates_list = new ArrayList<Mesh>();
		
		for(int x = 0; x < objects.length + 1; x++){
			Mesh mesh = new Mesh(gl, 2, true, false, false);
			mesh.color( 0.4f, 0.4f, 0.4f, 1 );
			mesh.vertex( block_width * x, 0, object_depth );
			mesh.color( 0.4f, 0.4f, 0.4f, 1 );
			mesh.vertex( block_width * x, block_height * objects[0].length, object_depth );
			coordinates_list.add(mesh);
			Mesh mesh_d = new Mesh(gl, 2, true, false, false);
			mesh_d.color( 0.4f, 0.4f, 0.4f, 1 );
			mesh_d.vertex( block_width * x, 0, object_depth );
			mesh_d.color( 0.4f, 0.4f, 0.4f, 1 );
			mesh_d.vertex( block_width * x, 0, 0 );
			coordinates_list.add(mesh_d);
			Mesh mesh_u = new Mesh(gl, 2, true, false, false);
			mesh_u.color( 0.4f, 0.4f, 0.4f, 1 );
			mesh_u.vertex( block_width * x, block_height * objects[0].length, object_depth );
			mesh_u.color( 0.4f, 0.4f, 0.4f, 1 );
			mesh_u.vertex( block_width * x, block_height * objects[0].length, 0 );
			coordinates_list.add(mesh_u);
			
		}
		for(int y = 0; y < objects[0].length + 1; y++){
			Mesh mesh = new Mesh(gl, 2, true, false, false);
			mesh.color( 0.4f, 0.4f, 0.4f, 1 );
			mesh.vertex( 0, block_height * y, object_depth);
			mesh.color( 0.4f, 0.4f, 0.4f, 1 );
			mesh.vertex( block_width * objects.length, block_height * y, object_depth );
			coordinates_list.add(mesh);
			Mesh mesh_l = new Mesh(gl, 5, true, false, true);
			mesh_l.color((223f/255f)*2.8f,(73f/255f)*2.8f,(73f/255f)*2.8f,1.0f);
			mesh_l.vertex( 0, block_height * y, object_depth);
			mesh_l.color((223f/255f)*2.8f,(73f/255f)*2.8f,(73f/255f)*2.8f,1.0f);
			mesh_l.vertex( 0, block_height * y, 0 );
			mesh_l.color((223f/255f)*2.8f,(73f/255f)*2.8f,(73f/255f)*2.8f,1.0f);
			if(y < objects[0].length){
				mesh_l.vertex( 0, block_height * (y+1), 0 );
			}
			else{
				mesh_l.vertex( 0, block_height * y, 0 );
			}
			mesh_l.color((223f/255f)*2.8f,(73f/255f)*2.8f,(73f/255f)*2.8f,1.0f);
			if(y < objects[0].length){
				mesh_l.vertex( 0, block_height * (y+1), object_depth);
			}
			else{
				mesh_l.vertex( 0, block_height * y, object_depth);
			}
			mesh_l.color((223f/255f)*2.8f,(73f/255f)*2.8f,(73f/255f)*2.8f,1.0f);
			mesh_l.vertex( 0, block_height * y, object_depth);
			finish_list.add(mesh_l);
			coordinates_list.add(mesh_l);
			Mesh mesh_r = new Mesh(gl, 5, true, false, true);
			mesh_r.color((51f/255f*3),(77f/255*3),(92f/255f*3),1f);
			mesh_r.vertex( block_width * objects.length, block_height * y, object_depth);
			mesh_r.color((51f/255f*3),(77f/255*3),(92f/255f*3),1f);
			mesh_r.vertex( block_width * objects.length, block_height * y, 0 );
			mesh_r.color((51f/255f*3),(77f/255*3),(92f/255f*3),1f);
			if(y < objects[0].length){
				mesh_r.vertex( block_width * objects.length, block_height * (y+1), 0 );
			}
			else{
				mesh_r.vertex( block_width * objects.length, block_height * y, 0 );
			}
			mesh_r.color((51f/255f*3),(77f/255*3),(92f/255f*3),1f);
			if(y < objects[0].length){
				mesh_r.vertex( block_width * objects.length, block_height * (y+1), object_depth);
			}
			else{
				mesh_r.vertex( block_width * objects.length, block_height * y, object_depth);
			}
			mesh_r.color((51f/255f*3),(77f/255*3),(92f/255f*3),1f);
			mesh_r.vertex( block_width * objects.length, block_height * y, object_depth);
			finish_list.add(mesh_r);
		}
	}
	public void renderLight(GL10 gl){
	
		gl.glEnable( GL10.GL_LIGHTING );
		float[] lightColor = { 1, 1, 1, 1 };
		float[] ambientLightColor = {0.3f, 0.3f, 0.3f, 1 };
		gl.glLightfv( GL10.GL_LIGHT0, GL10.GL_AMBIENT, ambientLightColor,0 );
		gl.glLightfv( GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightColor,0 );
		gl.glLightfv( GL10.GL_LIGHT0, GL10.GL_SPECULAR, lightColor,0 );
		float[] direction = { 0f, 0f, -10f, 0 };
		gl.glLightfv( GL10.GL_LIGHT0, GL10.GL_POSITION, direction,0 );
		gl.glEnable( GL10.GL_LIGHT0 );
		gl.glEnable( GL10.GL_COLOR_MATERIAL );
		gl.glShadeModel(GL10.GL_FLAT);
		gl.glEnable(GL10.GL_NORMALIZE);
	    
	}
}
