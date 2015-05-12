package hamburg.haw.polyshift.Game;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class Main extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		
		Intent intent = new Intent( this, PolyshiftActivity.class );
		
		startActivity( intent );
		
	}

		
}
