package hamburg.haw.polyshift.Menu;

import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import hamburg.haw.polyshift.R;
import hamburg.haw.polyshift.Tools.AlertDialogs;
import hamburg.haw.polyshift.Tools.PasswordHash;
import hamburg.haw.polyshift.Tools.PHPConnector;

public class SignupActivity extends Activity {
	
	Button signupButton;
    EditText editUsername,editPassword,editName,editLastname,editEmail,repeatPassword;
    HttpPost httppost;
    StringBuffer buffer;
    HttpResponse response;
    HttpClient httpclient;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setTheme(android.R.style.Theme_Holo_NoActionBar);
		setContentView(R.layout.activity_signup);
		
		signupButton = (Button)findViewById(R.id.SignupButton);
        editUsername = (EditText)findViewById(R.id.EditUsername);
        editEmail = (EditText)findViewById(R.id.EditEmail);
        editPassword= (EditText)findViewById(R.id.EditPassword);
        repeatPassword= (EditText)findViewById(R.id.RepeatPassword);

        signupButton.setOnClickListener(
    		new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	            	if(editUsername.getText().toString().trim().equals("")){
	            		Toast.makeText(SignupActivity.this,"Kein Benutzername angegeben", Toast.LENGTH_SHORT).show();
	            	}
	            	else if(editEmail.getText().toString().trim().equals("")){
	            		Toast.makeText(SignupActivity.this,"Keine E-Mail-Adresse angegeben", Toast.LENGTH_SHORT).show();
	            	}
	            	else if((editEmail.getText().toString().trim().split("@").length < 2) || (editEmail.getText().toString().split("\\.").length < 2)){
	            		Toast.makeText(SignupActivity.this,"Ungültige E-Mail-Adresse", Toast.LENGTH_SHORT).show();
	            	}
	            	else if(editPassword.getText().toString().trim().equals("")){
	            		Toast.makeText(SignupActivity.this,"Kein Passwort angegeben", Toast.LENGTH_SHORT).show();
	            	}
	            	else if(!editPassword.getText().toString().trim().equals(repeatPassword.getText().toString().trim())){
	            		Toast.makeText(SignupActivity.this,"Passwörter stimmen nicht überein", Toast.LENGTH_SHORT).show();
	            		
	            	}else{
	            		WelcomeActivity.dialog = ProgressDialog.show(SignupActivity.this, "","Login läuft", true);
	            		new Thread(
		                		new Runnable(){
		                			public void run(){
		                				userSignup();
		                			}
		                		}
		                ).start();
	            	}
	            }
    		}
    	);
	}

	void userSignup(){        	
        	
        	ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("username",editUsername.getText().toString().trim()));
            nameValuePairs.add(new BasicNameValuePair("email",editEmail.getText().toString().trim())); 
            nameValuePairs.add(new BasicNameValuePair("password",PasswordHash.toHash(editPassword.getText().toString().trim())));
            
            String response = PHPConnector.doRequest(nameValuePairs, "create_user.php");

            if(response.equalsIgnoreCase("Success")){
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(SignupActivity.this,"Registrierung erfolgreich.", Toast.LENGTH_SHORT).show();
                    }
                });
                WelcomeActivity.userLogin(editUsername.getText().toString().trim(),editPassword.getText().toString().trim(), this);
                this.finish();
            }else if(response.equalsIgnoreCase("Error: Username already exists.")){
                AlertDialogs.showAlert(this, "Fehler", "Der Benutzername ist bereits vergeben.");
                WelcomeActivity.dialog.dismiss();
            }else{
                AlertDialogs.showAlert(this, "Fehler", "Bitte versuchen sie es später erneut.");
                WelcomeActivity.dialog.dismiss();
            }
    }
    public void onBackPressed() {
        final Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
        this.finish();
    }
}