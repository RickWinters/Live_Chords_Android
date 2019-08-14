package livechords.livechordsjava;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import livechords.livechordsjava.Model.CurrentSong;

import static com.spotify.sdk.android.authentication.LoginActivity.REQUEST_CODE;

public class SpotifyConnector extends AsyncTask<String, CurrentSong, CurrentSong> {

    private static final String CLIENT_ID = "733d8f71031d4a9890f1940a1dddbab9";
    private static final String REDIRECT_URI = "androidlivechords://login";
    private static final String TAG = "MYDEBUG_Spotify_Connect";
    private static final String CURRENTLYPLAYINGURL = "https://api.spotify.com/v1/me/player/currently-playing";
    private AuthenticationRequest authenticationRequest;
    private BufferedReader reader;
    private String line;
    private StringBuffer response = new StringBuffer();

    private WeakReference<MainActivity> activityWeakReference;
    private TextView textView;

    private CurrentSong currentSong = new CurrentSong();

    SpotifyConnector(MainActivity activity) {
        activityWeakReference = new WeakReference<>(activity);
    }

    private void LoginAuthenticate() {
        Log.d(TAG, "LoginAuthenticate() called");
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setShowDialog(true).setScopes(
                new String[]{"user-read-private", "user-read-currently-playing"});
        authenticationRequest = builder.build();
        AuthenticationClient.openLoginActivity(activity, REQUEST_CODE, authenticationRequest);
    }

    private CurrentSong UpdateCurrentSong(String accestoken) {
        try {
            Thread.sleep(5000);
            URL url = new URL(CURRENTLYPLAYINGURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Authorization", "Bearer " + accestoken);
            connection.setRequestProperty("Content-Type", "application/json");
            //connection.getInputStream();
            int status = connection.getResponseCode();
            //Log.i("ServerConnection", "Status = "+status);

            if (status > 299) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            }
            //Log.d(TAG, "UpdateCurrentSong() returned: " + response.toString());
            currentSong.ParseJson(response.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return currentSong;
    }


    @Override
    protected CurrentSong doInBackground(String... strings) {
        Log.d(TAG, "doInBackground() called with: strings = [" + Arrays.toString(strings) + "]");
        String action = strings[0];
        if (action.equals("authenticate")){
            LoginAuthenticate();
            Log.d(TAG, "doInBackground: Authenticate finished");
        } else if (action.equals("checksong")) {
            currentSong = UpdateCurrentSong(strings[1]);
            Log.d(TAG, "doInBackground: Checksong finished Song = " + currentSong.toString());
        }
        return currentSong;
    }

    @Override
    protected void onProgressUpdate(CurrentSong... currentSongs) {
        currentSong = currentSongs[0];
        Log.d(TAG, "onProgressUpdate() called with: currentSong = [" + currentSong.toString() + "]");
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        String artist = currentSong.getArtist().replace(" ", "_").toLowerCase();
        String title = currentSong.getTitle().replace(" ", "_").toLowerCase();
        activity.setCurrent_artist(artist);
        activity.setCurrent_title(title);
        activity.UpdateLyrics();
    }

    @Override
    protected void onPostExecute(CurrentSong currentSong) {
        super.onPostExecute(currentSong);
    }
}
