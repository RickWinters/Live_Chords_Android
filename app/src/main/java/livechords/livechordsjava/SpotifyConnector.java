package livechords.livechordsjava;

import android.os.AsyncTask;
import android.util.Log;

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
import livechords.livechordsjava.Model.SpotifyAccount;

import static com.spotify.sdk.android.authentication.LoginActivity.REQUEST_CODE;

public class SpotifyConnector extends AsyncTask<String, Object, Void> {

    private static final String CLIENT_ID = "733d8f71031d4a9890f1940a1dddbab9";
    private static final String REDIRECT_URI = "androidlivechords://login";
    private static final String TAG = "MYDEBUG_Spotify_Connect";
    private static final String CURRENTLYPLAYINGURL = "https://api.spotify.com/v1/me/player/currently-playing";
    private static final String ACCOUNTINFOURL = "https://api.spotify.com/v1/me";
    private static final String ACCESTOKENEXPIRED = "acces token expired";
    private static final String USERSTILLLOGGEDIN = "user still logged in";

    private StringBuffer response = new StringBuffer();

    private WeakReference<MainActivity> activityWeakReference;

    private CurrentSong currentSong = new CurrentSong();
    private SpotifyAccount spotifyAccount = new SpotifyAccount();

    SpotifyConnector(MainActivity activity) {
        activityWeakReference = new WeakReference<>(activity);
    }

    private String GetReply(URL url, String accestoken) {
        Log.d(TAG, "GetReply() called with: url = [" + url + "], accestoken = [" + accestoken + "]");
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Authorization", "Bearer " + accestoken);
            connection.setRequestProperty("Content-Type", "application/json");
            //connection.getInputStream();
            int status = connection.getResponseCode();
            Log.i(TAG, "Status = "+status);

            //HANDLE ERRORS
            BufferedReader reader;
            String line;
//            if (status > 299) {
//                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
//                while ((line = reader.readLine()) != null) {
//                    response.append(line);
//                }
//                reader.close();
//            }

            //Handle 204 no content when no song is playing
            if (status == 204){
                response.append("No song playing");
            }

            else if (status == 401){
                response.append(ACCESTOKENEXPIRED);
            }

            //Handle correct content
            else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            }
            //Log.d(TAG, "UpdateCurrentSong() returned: " + response.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "GetReply() returned: " + response.toString());
        return response.toString();
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
        AuthenticationRequest authenticationRequest = builder.build();
        AuthenticationClient.openLoginActivity(activity, REQUEST_CODE, authenticationRequest);
    }

    private void UpdateCurrentSong(String accestoken) {
        Log.d(TAG, "UpdateCurrentSong() called with: accestoken = [" + accestoken + "]");
        URL url = null;
        try {
            url = new URL(CURRENTLYPLAYINGURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String reply = GetReply(url, accestoken);
        if (reply.equals(ACCESTOKENEXPIRED)){
            publishProgress(ACCESTOKENEXPIRED);
        } else {
            currentSong.ParseJson(reply);
            Log.d(TAG, "UpdateCurrentSong() returned: " + currentSong.toString());
        }
    }

    private void GetAccountInfo(String accestoken) {
        Log.d(TAG, "GetAccountInfo() called with: accestoken = [" + accestoken + "]");
        URL url = null;
        try {
            url = new URL(ACCOUNTINFOURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String reply = GetReply(url, accestoken);
        if (reply.equals(ACCESTOKENEXPIRED)) {
            publishProgress(ACCESTOKENEXPIRED);
        } else {
            spotifyAccount.ParseJson(reply);
        }

    }

    private String CheckAccesTokenValidity(String accestoken){
        Log.d(TAG, "CheckAccesTokenValidity() called with: accestoken = [" + accestoken + "]");
        URL url = null;
        try {
            url = new URL(ACCOUNTINFOURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String reply = GetReply(url, accestoken);
        if (reply.equals(ACCESTOKENEXPIRED)) {
            return ACCESTOKENEXPIRED;
        } else {
            return USERSTILLLOGGEDIN;
        }
    }


    @Override
    protected Void doInBackground(String... strings) {
        Log.d(TAG, "doInBackground() called with: strings = [" + Arrays.toString(strings) + "]");
        String action = strings[0];
        if (action.equals("authenticate")){
            LoginAuthenticate();
            Log.d(TAG, "doInBackground: Authenticate finished");
        } else if (action.equals("checksong")) {
            UpdateCurrentSong(strings[1]);
            Log.d(TAG, "doInBackground: Checksong finished Song = " + currentSong.toString());
            publishProgress(currentSong);
            //return currentSong;
        } else if (action.equals("personal")) {
            GetAccountInfo(strings[1]);
            Log.d(TAG, "doInBackground: Personal finished account = " + spotifyAccount.toString());
            publishProgress(spotifyAccount);
            //return spotifyAccount;
        } else if (action.equals("valid_accestoken")){
            String valid = CheckAccesTokenValidity(strings[1]);
            publishProgress(valid);
        }
        return null;
    }


    @Override
    protected void onProgressUpdate(Object... values) {
        Log.d(TAG, "onProgressUpdate() called with: values = [" + values[0].toString() + "]");
        Object value = values[0];
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        //handle return of UpdateCurrentSong
        if (value.getClass() == CurrentSong.class) {
            String artist = currentSong.getArtist().replace(" ", "_");
            String title = currentSong.getTitle().replace(" ", "_");
            activity.setCurrentArtist(artist);
            activity.setCurrentTitle(title);
            activity.setCurrentSong(currentSong);
            activity.UpdateLyrics();

        //handle return of GetAccountInfo()
        } else if (value.getClass() == SpotifyAccount.class) {
            activity.setAccountName(spotifyAccount.getName());
            //spotifyAccount = (SpotifyAccount) values[0];
            //String name = spotifyAccount.getName();
            //activity.setAccountName(name);
            activity.UpdateLoginButtonText();

        //handle return of access token expired
        } else if (value.equals(ACCESTOKENEXPIRED)){
            activity.LogOutUser();

        //handle return of user still logged in
        } else if (value.equals(USERSTILLLOGGEDIN)){
            activity.UserStillLoggedIn();
        }


    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
