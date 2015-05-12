package hamburg.haw.polyshift.Game;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLU;
import android.util.Log;

public class EndScreen implements GameScreen {

	private Player winner;
	public boolean isDone = false;
	Mesh pictureMesh;
	Texture textureWinnerPlayerTwo;
	Texture textureWinnerPlayerOne;
	
	public EndScreen( GL10 gl, GameActivity activity )
	{			
		pictureMesh = new Mesh( gl, 4, true, true, false );
		pictureMesh.color(238f/255f/1.1f, 233f/255f/1.1f, 192f/255f/1.1f, 1.0f );
		pictureMesh.texCoord(0, 0);
		pictureMesh.vertex(-1, 1, 0 );
		pictureMesh.color(238f/255f/1.1f, 233f/255f/1.1f, 192f/255f/1.1f, 1.0f );
		pictureMesh.texCoord(1, 0);
		pictureMesh.vertex(1, 1, 0 );
		pictureMesh.color(238f/255f/1.1f, 233f/255f/1.1f, 192f/255f/1.1f, 1.0f );
		pictureMesh.texCoord(1, 1);
		pictureMesh.vertex(1, -1, 0 );
		pictureMesh.color(238f/255f/1.1f, 233f/255f/1.1f, 192f/255f/1.1f, 1.0f );
		pictureMesh.texCoord(0, 1);
		pictureMesh.vertex(-1, -1, 0 );
		
		try
		{
			Bitmap bitmap = BitmapFactory.decodeStream( activity.getAssets().open( "winnerPlayerOne.png" ) );
			textureWinnerPlayerOne = new Texture( gl, bitmap, Texture.TextureFilter.MipMap, Texture.TextureFilter.Nearest, Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge );
			bitmap = BitmapFactory.decodeStream( activity.getAssets().open( "winnerPlayerTwo.png" ) );
			textureWinnerPlayerTwo = new Texture( gl, bitmap, Texture.TextureFilter.MipMap, Texture.TextureFilter.Nearest, Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge );
			
			
		}
		catch( Exception ex )
		{
			Log.d( "Space Invaders", "couldn't load textures" );
			throw new RuntimeException( ex );
		}
	}	

	@Override
	public boolean isDone() 
	{	
		return isDone;
	}

	@Override
	public void update(GameActivity activity) 
	{	
		if( activity.isTouched() )
			isDone = true;
	}
	
	@Override
	public void render(GL10 gl, GameActivity activity) 
	{	
		gl.glViewport( 0, 0, activity.getViewportWidth(), activity.getViewportHeight() );
		gl.glClearColor( 238f/255f/1.1f, 233f/255f/1.1f, 192f/255f/1.1f, 0.0f );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable( GL10.GL_TEXTURE_2D );
		gl.glMatrixMode( GL10.GL_PROJECTION );
		gl.glLoadIdentity();
		float aspectRatio = (float)activity.getViewportWidth() / activity.getViewportHeight();
		GLU.gluPerspective( gl, 67, aspectRatio, 1, 100 );
		GLU.gluLookAt(gl, 5.5f, 3.38f, 4.8f, 5.5f, 3.38f, -5f, 0f, 1f, 0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glLoadIdentity();
		if(winner.isPlayerOne){
			textureWinnerPlayerOne.bind();
		}else{
			textureWinnerPlayerTwo.bind();
		}
		
		pictureMesh.render(Mesh.PrimitiveType.TriangleFan );
		gl.glDisable( GL10.GL_TEXTURE_2D );
	}

	public void setWinner(Player winner) {
		// TODO Auto-generated method stub
		this.winner = winner;
	}




}
