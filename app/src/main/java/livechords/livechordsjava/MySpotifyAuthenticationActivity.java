package livechords.livechordsjava;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class MySpotifyAuthenticationActivity extends Activity {


    private static final String TAG = "MYDEBUG_SpAuthactivity";

    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent() called with: intent = [" + intent + "]");
        super.onNewIntent(intent);

        Uri uri = intent.getData();
        if (uri != null) {
            AuthenticationResponse response = AuthenticationResponse.fromUri(uri);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }

        }
    }

}