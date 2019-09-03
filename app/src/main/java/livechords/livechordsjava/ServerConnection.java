package livechords.livechordsjava;

import android.os.AsyncTask;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class ServerConnection extends AsyncTask<String, Void, String> {
    private static final String TAG = "MYDEBUG_ServeConnection";
    private String mainURL = "http://82.75.204.165:8081/live_chords/";

    private String getLyrics(String artist, String title){
        String[] artistitle = HelperMethods.cleanBrackets(artist, title);
        artist = artistitle[0];
        title = artistitle[1];
        Log.d(TAG, "getLyrics() called with: artist = [" + artist + "], title = [" + title + "]");
        URL url = null;
        artist = artist.replace(" ", "_").replace("%20","_");
        title = title.replace(" ", "_").replace("%20","_");

        try {
            url = new URL(mainURL+"Get/"+artist+"/"+title);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return HelperMethods.getResponse(url);
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
        return HelperMethods.getResponse(url);
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