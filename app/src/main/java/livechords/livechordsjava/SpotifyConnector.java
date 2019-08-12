package livechords.livechordsjava;

import android.os.AsyncTask;
import android.util.Log;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.lang.ref.WeakReference;

import static com.spotify.sdk.android.authentication.LoginActivity.REQUEST_CODE;

public class SpotifyConnector extends AsyncTask<String, Void, Void> {

    private static final String CLIENT_ID = "733d8f71031d4a9890f1940a1dddbab9";
    private static final String REDIRECT_URI = "androidlivechords://login";
    private static final String TAG = "MYDEBUG_Spotify_Connect";
    private AuthenticationRequest authenticationRequest;

    private WeakReference<MainActivity> activityWeakReference;

    public SpotifyConnector(MainActivity activity){
        activityWeakReference = new WeakReference<>(activity);
    }

    public void LoginAuthenticate() {
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



    @Override
    protected Void doInBackground(String... strings) {
        Log.d(TAG, "doInBackground() called with: strings = [" + strings[0] + "]");
        String action = strings[0];
        if (action.equals("authenticate")){
            LoginAuthenticate();
            Log.d(TAG, "doInBackground: Authenticate finished");
        }
        return null;
    }
}
