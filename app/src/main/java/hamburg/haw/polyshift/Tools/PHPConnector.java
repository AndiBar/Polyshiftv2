package hamburg.haw.polyshift.Tools;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import hamburg.haw.polyshift.Menu.HandleSharedPreferences;
import hamburg.haw.polyshift.Menu.MainMenuActivity;
import hamburg.haw.polyshift.Menu.WelcomeActivity;

public class PHPConnector {
	
	public static String stringResponse;
	public static HttpClient httpclient = new DefaultHttpClient();
	public static HttpGet httpget;
	public static HttpPost httppost;
	public static ArrayList<NameValuePair> nameValuePairs;
	public static ResponseHandler<String> responseHandler;
    public static final String server = "http://game.polyshift.de/";
	public static HttpEntity entity;
	public static HttpResponse httpResponse;
	public static String response;
	private final static String tag="Response von ";
    private static Context context;
	
	public static String doRequest(ArrayList<NameValuePair> args, String url){
		httppost = new HttpPost(server + url);
        try {
			httppost.setEntity(new UrlEncodedFormEntity(args));
			httpResponse = httpclient.execute(httppost);
			entity = httpResponse.getEntity();
			stringResponse = EntityUtils.toString(entity,"UTF-8");
			entity.consumeContent();
			Log.d(tag+url, stringResponse);
			return stringResponse;
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
        }
		return "error";
	}
	
	public static String doRequest(String url){
		httppost = new HttpPost(server + url);
        try {
			httpResponse = httpclient.execute(httppost);
			entity = httpResponse.getEntity();
			stringResponse = EntityUtils.toString(entity,"UTF-8");
			entity.consumeContent();
			Log.d(tag+url, stringResponse);
			return stringResponse;
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
		return "error";
	}


}
