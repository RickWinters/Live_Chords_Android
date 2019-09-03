package livechords.livechordsjava;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class ServerConnection extends AsyncTask<String, Void, String> {
    private static final String TAG = "MYDEBUG_ServeConnection";
    private String mainURL = "http://82.75.204.165:8081/live_chords/";
    private StringBuffer response = new StringBuffer();

    private String getResponse(URL url){
        Log.d(TAG, "getResponse() called with: url = [" + url + "]");
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setReadTimeout(5000);
            connection.getInputStream();

            int status = connection.getResponseCode();
            //Log.i("ServerConnection", "Status = "+status);

            BufferedReader reader;
            String line;
            if (status > 299){
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while((line = reader.readLine()) != null){
                    response.append(line);
                }
                reader.close();
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while((line = reader.readLine()) != null){
                    response.append(line);
                }
                reader.close();
            }

            //Log.i("ServerConnection", response.toString());
            return response.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    private String getLyrics(String artist, String title){
        Log.d(TAG, "getLyrics() called with: artist = [" + artist + "], title = [" + title + "]");
        URL url = null;
        artist = artist.replace(" ", "_").replace("%20","_");
        title = title.replace(" ", "_").replace("%20","_");

        try {
            url = new URL(mainURL+"Get/"+artist+"/"+title);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return getResponse(url);
    }

    private String getLyricsList() {
        Log.d(TAG, "getLyricsList() called");
        // java.net.HttpUrlConnection Method
        URL url = null;
        try {
            url = new URL(mainURL+"List");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return getResponse(url);
    }

    @Override
    public String doInBackground(String... strings) {
        Log.d(TAG, "doInBackground() called with: strings = [" + Arrays.toString(strings) + "]");
        String action = strings[0];
        String answer = "incorrect command";
        if (action.equals("getList")) {
            answer = getLyricsList();
        } else if (action.equals("getLyrics"))
            answer = getLyrics(strings[1], strings[2]);
        Log.d(TAG, "doInBackground: Finished");

        return answer;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}