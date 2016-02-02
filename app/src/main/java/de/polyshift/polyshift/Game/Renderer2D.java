package de.polyshift.polyshift.Game;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.opengl.GLU;
import android.util.Log;
import android.view.Display;

import de.polyshift.polyshift.Game.Mesh.PrimitiveType;

public class Renderer2D extends Renderer {

	public Renderer2D(GameActivity activity, GL10 gl, GameObject[][] objects){
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		
		block_width = activity.getViewportWidth() / objects.length;
		block_height = activity.getViewportHeight() / objects[0].length;
		
		object_width = width / objects.length;
		object_height = height / objects[0].length;
		
		for(int i = 0; i < objects.length; i++){
			for(int j = 0; j < objects[i].length; j++){
				if(objects[i][j] instanceof Player){
					Bitmap bitmapPlayerOne = null;
					Bitmap bitmapPlayerTwo = null;
					Bitmap bitmapPlayerOneLock = null;
					Bitmap bitmapPlayerTwoLock = null;

					
					try
					{
					    bitmapPlayerOne = BitmapFactory.decodeStream( activity.getAssets().open( "player1.png" ) );
					    bitmapPlayerOneLock = BitmapFactory.decodeStream( activity.getAssets().open( "player1_lock.png" ) );
					    bitmapPlayerTwo = BitmapFactory.decodeStream( activity.getAssets().open( "player2.png" ) );
					    bitmapPlayerTwoLock = BitmapFactory.decodeStream( activity.getAssets().open( "player2_lock.png" ) );
					}
					catch( Exception ex )
					{
						Log.d("Sample", "Failed loading player texture.");
			            System.exit(-1);
					}	
					
					Mesh mesh;
					mesh = new Mesh( gl, 4, false, true, false );				
					mesh.texCoord(0f, 1f);
					mesh.vertex( i*block_width, j*block_height, 0 );
					mesh.texCoord(1f, 1f);
			        mesh.vertex( i*block_width + object_width, j*block_height, 0 );
			        mesh.texCoord(1f, 0f);
			        mesh.vertex( i*block_width + object_width, j*block_height + object_height, 0 );
			        mesh.texCoord(0f, 0f);
			        mesh.vertex( i*block_width, j*block_height + object_height, 0 );
			        
			        if(objects[i][j].isPlayerOne){
			        	texturePlayerOne = new Texture(gl, bitmapPlayerOne, Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
			        	texturePlayerOneLock = new Texture(gl, bitmapPlayerOneLock, Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
			        }else{
			        	texturePlayerTwo = new Texture(gl, bitmapPlayerTwo, Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
			        	texturePlayerTwoLock = new Texture(gl, bitmapPlayerTwoLock, Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
			        }

                    Player player = (Player) objects[i][j];
			        player.setMesh(mesh);
                    objects[i][j] = player;
				}
				if(objects[i][j] instanceof Polynomino){
					Bitmap bitmapLocker = null;
					
					try
					{
					    bitmapLocker = BitmapFactory.decodeStream( activity.getAssets().open( "locker.png" ) );
					}
					catch( Exception ex )
					{
						Log.d("Sample", "Failed loading locker texture.");
			            System.exit(-1);
					}
					
					Mesh mesh;
					mesh = new Mesh( gl, 5, false, true, false );
					mesh.texCoord(0f, 1f);
					mesh.vertex( i*block_width, j*block_height, 0 );
					mesh.texCoord(1f, 1f);
					mesh.vertex( i*block_width + object_width, j*block_height, block_height );
					mesh.texCoord(1f, 0f);
			        mesh.vertex( i*block_width + object_width, j*block_height + object_height, 0 );
			        mesh.texCoord(0f, 0f);
			        mesh.vertex( i*block_width, j*block_height + object_height, 0 );
			        mesh.texCoord(0f, 1f);
			        mesh.vertex( i*block_width, j*block_height, 0 );
			       
			        textureLocker = new Texture(gl, bitmapLocker, Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
                    Polynomino polynomino = (Polynomino) objects[i][j];
			        polynomino.setMesh(mesh);
                    objects[i][j] = polynomino;
			        
				}
			}
		}
		
	}
	
	public void setPerspective(GameActivity activity, GL10 gl){
		gl.glViewport(0, 0, activity.getViewportWidth(), activity.getViewportHeight());
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		gl.glMatrixMode( GL10.GL_PROJECTION );
		gl.glLoadIdentity();
		GLU.gluOrtho2D( gl, 0, activity.getViewportWidth(), 0, activity.getViewportHeight());
		
		gl.glMatrixMode( GL10.GL_MODELVIEW );
        gl.glLoadIdentity();
		
	}

	public void renderObjects(GameActivity activity, GL10 gl, GameObject[][] objects){
		
		block_width = activity.getViewportWidth() / objects.length;
		block_height = activity.getViewportHeight() / objects[0].length;
	
		if(coordinates_list != null){
			for(Mesh mesh : coordinates_list){
				mesh.render( PrimitiveType.Lines );
			}
		}
		
		for(int i = 0; i < objects.length; i++){
			for(int j = 0; j < objects[i].length; j++){
				if(objects[i][j] instanceof Player){
					if(objects[i][j].isPlayerOne){
						if(objects[i][j].isLocked){
							texturePlayerOneLock.bind();
						}
						else{
							texturePlayerOne.bind();
						}
					}else{
						if(objects[i][j].isLocked){
							texturePlayerTwoLock.bind();
						}
						else{
							texturePlayerTwo.bind();
						}
					}
					if(objects[i][j].isMovingLeft){
						if(objects[i][j].pixel_position.x == -1){
							objects[i][j].pixel_position.x = objects[i][j].block_position.x*block_width;
						}
						if(objects[i][j].pixel_position.x > i*block_width){
							gl.glEnable( GL10.GL_TEXTURE_2D );
							gl.glPushMatrix();
							gl.glTranslatef(objects[i][j].pixel_position.x, j*block_height, 0 );
                            Player player = (Player) objects[i][j];
							player.getMesh().render(PrimitiveType.TriangleFan);
							gl.glPopMatrix();
							gl.glDisable( GL10.GL_TEXTURE_2D );
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
							gl.glEnable( GL10.GL_TEXTURE_2D );
							gl.glPushMatrix();
							gl.glTranslatef(objects[i][j].pixel_position.x, j*block_height, 0 );
                            Player player = (Player) objects[i][j];
							player.getMesh().render(PrimitiveType.TriangleFan);
							gl.glPopMatrix();
							gl.glDisable( GL10.GL_TEXTURE_2D );
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
							gl.glEnable( GL10.GL_TEXTURE_2D );
							gl.glPushMatrix();
							gl.glTranslatef(i*block_width,objects[i][j].pixel_position.y, 0 );
                            Player player = (Player) objects[i][j];
							player.getMesh().render(PrimitiveType.TriangleFan);
							gl.glPopMatrix();
							gl.glDisable( GL10.GL_TEXTURE_2D );
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
							gl.glEnable( GL10.GL_TEXTURE_2D );
							gl.glPushMatrix();
							gl.glTranslatef(i*block_width,objects[i][j].pixel_position.y, 0 );
                            Player player = (Player) objects[i][j];
							player.getMesh().render(PrimitiveType.TriangleFan);
							gl.glPopMatrix();
							gl.glDisable( GL10.GL_TEXTURE_2D );
							objects[i][j].pixel_position.y -= block_height * objects[i][j].movingVelocity;
						}
						else{
							objects[i][j].isMovingDown = false;
							objects[i][j].lastState = Simulation.DOWN;
							objects[i][j].pixel_position.y = -1;
						}
					}
					else{
						gl.glEnable( GL10.GL_TEXTURE_2D );
						gl.glPushMatrix();
						gl.glTranslatef(i*block_width, j*block_height, 0 );
                        Player player = (Player) objects[i][j];
						player.getMesh().render(PrimitiveType.TriangleFan);
						gl.glPopMatrix();
						gl.glDisable( GL10.GL_TEXTURE_2D );
					}
				}
				if(objects[i][j] instanceof Polynomino){
					Polynomino polynomio = (Polynomino) objects[i][j];
					if(polynomio.isLocked){
						gl.glEnable( GL10.GL_TEXTURE_2D );
						gl.glEnable(GL10.GL_BLEND);
						gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE);
						textureLocker.bind();
						gl.glColor4f(objects[i][j].colors[0],objects[i][j].colors[1],objects[i][j].colors[2],objects[i][j].colors[3]);
						gl.glPushMatrix();
						gl.glTranslatef(i*block_width, j*block_height, 0 );
                        Polynomino polynomino = (Polynomino) objects[i][j];
						polynomino.getMesh().render(PrimitiveType.TriangleFan);
						gl.glPopMatrix();
						gl.glColor4f(1, 1, 1, 1);
						gl.glDisable(GL10.GL_BLEND);
						gl.glDisable( GL10.GL_TEXTURE_2D );
					}
					else{
						gl.glColor4f(objects[i][j].colors[0],objects[i][j].colors[1],objects[i][j].colors[2],objects[i][j].colors[3]);
						gl.glPushMatrix();
						gl.glTranslatef(i*block_width, j*block_height, 0 );
                        Polynomino polynomino = (Polynomino) objects[i][j];
						polynomino.getMesh().render(PrimitiveType.TriangleStrip);
						gl.glPopMatrix();
						gl.glColor4f(1, 1, 1, 1);
					}
				}
			}
		}
	}

	public void enableCoordinates(GL10 gl, GameObject[][] objects) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void renderLight(GL10 gl) {
		// TODO Auto-generated method stub
		
	}
}
