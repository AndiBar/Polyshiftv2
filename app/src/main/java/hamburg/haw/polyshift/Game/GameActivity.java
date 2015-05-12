package hamburg.haw.polyshift.Game;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;




import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class GameActivity extends Activity implements GLSurfaceView.Renderer, OnTouchListener {
	
	private GLSurfaceView glsv;
	private int x;
	private int y;
	private float deltaTime;
	private long lastFrameStart;
	private GameListener gameListener;
	private int touchX;
	private int touchY;
	private int touchedX;
	private int touchedY;
	private boolean isTouched;
	public boolean isSwiped;
	private int width;
	private int height;

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public void setGameListener(GameListener gameListener){
		this.gameListener = gameListener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		glsv = new GLSurfaceView(this);
		
		glsv.setRenderer( this );
		
		glsv.setOnTouchListener(this);
		
		setContentView(glsv);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		lastFrameStart = System.nanoTime();
		if(gameListener != null){
			this.gameListener.setup(this, gl);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		
		this.width = width;
		this.height = height;
		
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		long currentFrameStart = System.nanoTime();
		deltaTime = (currentFrameStart-lastFrameStart) / 1000000000.0f;
		lastFrameStart = currentFrameStart;
		
		if(gameListener != null){
			this.gameListener.mainLoopIteration(this, gl);
		}
	}

	@Override
	
	public boolean onTouch(View v, MotionEvent event) {
		if( event.getAction() == MotionEvent.ACTION_DOWN)
		{
			touchedX = (int)event.getX();
			touchedY = (int)event.getY();
			isTouched = true;
			isSwiped = false;
		}

		else if( event.getAction() == MotionEvent.ACTION_UP ){
			touchX = (int)event.getX();
			touchY = (int)event.getY();
			isTouched = false;
			isSwiped = true;
		}
		try
		{
			Thread.sleep( 30 );
		}
		catch( Exception ex )
		{

		}			
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		//glsv.onPause();		
	}

	/**
	 * Called when the application is resumed. We need to
	 * also resume the GLSurfaceView.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		//glsv.onResume();		
	}		
	
	public int getTouchX() {
		return touchX;
	}

	public int getTouchY() {
		return touchY;
	}
	
	public int getTouchedX() {
		return touchedX;
	}
	
	public int getTouchedY() {
		return touchedY;
	}
	
	public boolean isTouched( )
	{
		return isTouched;
	}
	
	public boolean isSwiped( )
	{
		return isSwiped;
	}
	
	public int getViewportWidth( )
	{
		return width;
	}

	public int getViewportHeight( )
	{
		return height;
	}
	
	public float getDeltaTime( )
	{
		return deltaTime;
	}
}
