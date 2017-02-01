package de.polyshift.polyshift.Game.Sync;

import android.util.Base64;
import android.util.Log;

import de.polyshift.polyshift.Game.Objects.Player;
import de.polyshift.polyshift.Game.Objects.Polynomino;
import de.polyshift.polyshift.Game.Renderer.Vector;
import de.polyshift.polyshift.Game.Logic.Simulation;
import de.polyshift.polyshift.Tools.PHPConnector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Diese Klasse ist für die Synchronisierung des Spielfeldes zuständig. Sie serialisiert das
 * Spielfeld und sendet es an den Server bzw. empfängt das Spielfeld vom Server und
 * deserialisiert es.
 *
 * @author helmsa
 *
 */

public class GameSync {

    static Simulation simulation;
    static ArrayList<Simulation> simulations;

    public static String uploadSimulation(final Simulation simulation){
        class UploadSimulationThread extends Thread{
            private String response = "";
            public void run(){
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                String serializedObjects = "";
                try {
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    ObjectOutputStream so = new ObjectOutputStream(bo);
                    so.writeObject(simulation);
                    so.flush();

                    serializedObjects = Base64.encodeToString(bo.toByteArray(), Base64.DEFAULT);
                } catch (Exception e) {
                }
                nameValuePairs.add(new BasicNameValuePair("objects", serializedObjects));
                response = PHPConnector.doRequest(nameValuePairs, "update_playground.php");
            }
            public String getResponse(){
                return response;
            }
        }
        UploadSimulationThread upload_simulation_thread = new UploadSimulationThread();
        upload_simulation_thread.start();
        try {
            long waitMillis = 10000;
            while (upload_simulation_thread.isAlive()) {
                upload_simulation_thread.join(waitMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return upload_simulation_thread.getResponse();
    }

    public static String uploadSimulations(final ArrayList<Simulation> simulations){
        String simulationsString = "";
        for(int i = 0; i < simulations.size(); i++){
            String serializedSimulation = "";
            try {
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                ObjectOutputStream so = new ObjectOutputStream(bo);
                so.writeObject(simulations.get(i));
                so.flush();

                serializedSimulation = Base64.encodeToString(bo.toByteArray(), Base64.DEFAULT);
            } catch (Exception e) {
            }
            if(!simulationsString.equals("")){
                simulationsString = simulationsString + ";" + serializedSimulation;
            }else{
                simulationsString = serializedSimulation;
            }

        }
        final String serializedSimulations = simulationsString;
        class UploadSimulationThread extends Thread{
            private String response = "";
            public void run(){
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("objects", serializedSimulations));
                response = PHPConnector.doRequest(nameValuePairs, "update_playground.php");
            }
            public String getResponse(){
                return response;
            }
        }
        UploadSimulationThread upload_simulation_thread = new UploadSimulationThread();
        upload_simulation_thread.start();
        try {
            long waitMillis = 10000;
            while (upload_simulation_thread.isAlive()) {
                upload_simulation_thread.join(waitMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return upload_simulation_thread.getResponse();
    }

    public static Simulation downloadSimulation() {
        Thread download_playground_thread = new DownloadSimulationThread();
        download_playground_thread.start();
        try {
            long waitMillis = 10000;
            while (download_playground_thread.isAlive()) {
                download_playground_thread.join(waitMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return simulation;
    }

    public static ArrayList<Simulation> downloadSimulations() {
        Thread download_playgrounds_thread = new DownloadSimulationsThread();
        download_playgrounds_thread.start();
        try {
            long waitMillis = 10000;
            while (download_playgrounds_thread.isAlive()) {
                download_playgrounds_thread.join(waitMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return simulations;
    }

    public static class DownloadSimulationThread extends Thread{

        public void run(){
            String serializedObjects = PHPConnector.doRequest("update_playground.php");
            try {
                byte b[] = Base64.decode(serializedObjects,Base64.DEFAULT);
                ByteArrayInputStream bi = new ByteArrayInputStream(b);
                ObjectInputStream si = new ObjectInputStream(bi);
                simulation = (Simulation) si.readObject();
                for(int i = 0; i < simulation.objects.length; i++) {
                    for (int j = 0; j < simulation.objects[0].length; j++) {
                        if(simulation.objects[i][j] instanceof Player) {
                            if(simulation.player == simulation.objects[i][j]){
                                simulation.objects[i][j].isPlayerOne = true;
                            }
                        }
                        if(simulation.objects[i][j] instanceof Polynomino) {
                            simulation.objects[i][j].colors = recreateColor();
                            Polynomino polynomino = (Polynomino) simulation.objects[i][j];
                            polynomino.border_pixel_position = new Vector(0,0,0);
                            polynomino.isRendered = true;
                            simulation.objects[i][j] = polynomino;
                        }
                    }
                }
                for(int k = 0; k < simulation.lastMovedPolynomino.blocks.size(); k++) {
                    simulation.objects[simulation.lastMovedPolynomino.blocks.get(k).x][simulation.lastMovedPolynomino.blocks.get(k).y].isLocked = true;
                    Polynomino polynomino = (Polynomino) simulation.objects[simulation.lastMovedPolynomino.blocks.get(k).x][simulation.lastMovedPolynomino.blocks.get(k).y];
                    polynomino.isRendered = true;
                    simulation.objects[simulation.lastMovedPolynomino.blocks.get(k).x][simulation.lastMovedPolynomino.blocks.get(k).y] = polynomino;
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public static class DownloadSimulationsThread extends Thread {
        ArrayList<Simulation> simulationsList = new ArrayList<>();

        public void run() {
            String serializedObjects = PHPConnector.doRequest("update_playground.php");

            String[] simulationsArray = serializedObjects.split(";");
            for (int l = 0; l < simulationsArray.length; l++) {
                Log.d("test",simulationsArray[l]);
                try {
                    byte b[] = Base64.decode(simulationsArray[l], Base64.DEFAULT);
                    ByteArrayInputStream bi = new ByteArrayInputStream(b);
                    ObjectInputStream si = new ObjectInputStream(bi);
                    Simulation simulation =(Simulation) si.readObject();
                    simulationsList.add(simulation);
                    for (int i = 0; i < simulation.objects.length; i++) {
                        for (int j = 0; j < simulation.objects[0].length; j++) {
                            if (simulation.objects[i][j] instanceof Player) {
                                if (simulation.player == simulation.objects[i][j]) {
                                    simulation.objects[i][j].isPlayerOne = true;
                                }
                            }
                            if (simulation.objects[i][j] instanceof Polynomino) {
                                simulation.objects[i][j].colors = recreateColor();
                                Polynomino polynomino = (Polynomino) simulation.objects[i][j];
                                polynomino.border_pixel_position = new Vector(0, 0, 0);
                                polynomino.isRendered = true;
                                simulation.objects[i][j] = polynomino;
                            }
                        }
                    }
                    for (int k = 0; k < simulation.lastMovedPolynomino.blocks.size(); k++) {
                        simulation.objects[simulation.lastMovedPolynomino.blocks.get(k).x][simulation.lastMovedPolynomino.blocks.get(k).y].isLocked = true;
                        Polynomino polynomino = (Polynomino) simulation.objects[simulation.lastMovedPolynomino.blocks.get(k).x][simulation.lastMovedPolynomino.blocks.get(k).y];
                        polynomino.isRendered = true;
                        simulation.objects[simulation.lastMovedPolynomino.blocks.get(k).x][simulation.lastMovedPolynomino.blocks.get(k).y] = polynomino;
                    }
                } catch (Exception e) {

                }
            }
            simulations = simulationsList;
        }
    }

    public static float[] recreateColor(){
        ArrayList<float[]> colors = new ArrayList<float[]>();
        float[] color1 = {(51f/255f),(77f/255),(92f/255f),1f};
        float[] color2 = {(71f/255f),(176f/255f),(156f/255f),1f};
        float[] color3 = {(239f/255f),(201f/255f),(76f/255f),1f};
        float[] color4 = {(226f/255f),(122f/255f),(65f/255f),1f};
        float[] color5 = {(223f/255f),(73f/255f),(73f/255f),1f};
        colors.add(color1);
        colors.add(color2);
        colors.add(color3);
        colors.add(color4);
        colors.add(color5);
        int randomColor =  (int)(Math.random()*colors.size());
        return colors.get(randomColor);
    }

    public static void SendChangeNotification(final String receiverUserId,final String msg,final String notificationGameID, final String className) {
        String encodedMsg = "";
        class ChangeNotificationThread extends Thread {
            public void run() {
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("userid",receiverUserId));
                String encodedMsg = null;
                try {
                    encodedMsg = URLEncoder.encode(msg, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                nameValuePairs.add(new BasicNameValuePair("msg",encodedMsg));
                nameValuePairs.add(new BasicNameValuePair("game_id",notificationGameID));
                nameValuePairs.add(new BasicNameValuePair("class_name",className));
                PHPConnector.doRequest(nameValuePairs,"gcmpush.php");
            }
        }
        Log.i("GCM", "...gesendet an " + receiverUserId);
        Log.i("GCM", "Nachricht: " + msg);
        Log.i("GCM", "Game_ID: " + notificationGameID);
        Log.i("GCM", "Class_Name: " + className);
        Thread send_change_notification = new ChangeNotificationThread();
        send_change_notification.start();
        try {
            long waitMillis = 10000;
            while (send_change_notification.isAlive()) {
                send_change_notification.join(waitMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
