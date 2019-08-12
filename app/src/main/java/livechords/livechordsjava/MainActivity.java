package livechords.livechordsjava;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyNativeAuthUtil;

import java.util.concurrent.ExecutionException;

import livechords.livechordsjava.Fragment_classes.ContactFragment;
import livechords.livechordsjava.Fragment_classes.HomeFragment;
import livechords.livechordsjava.Fragment_classes.SpotifyFragment;
import livechords.livechordsjava.Model.Tabsfile;

import static com.spotify.sdk.android.authentication.LoginActivity.REQUEST_CODE;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MYDEBUG_Main_Activity";
    private static final int AUTH_TOKEN_REQUEST_CODE = 0x10;
    private DrawerLayout drawer;
    private TextView textView;
    private FrameLayout frameLayout;
    private NavigationView navigationView;
    private Tabsfile tabsfile = new Tabsfile();
    private String current_artist = "Flogging_Molly";
    private String current_title = "Drunken_Lullabies";

    private static final String CLIENT_ID = "733d8f71031d4a9890f1940a1dddbab9";
    private static final String REDIRECT_URI = "AndroidLiveChords://login";
    private AuthenticationRequest authenticationRequest;
    private SpotifyNativeAuthUtil spotifyNativeAuthUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        frameLayout = findViewById(R.id.fragment_container);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

    }

    @Override
    public void onBackPressed(){
        Log.d(TAG, "onBackPressed() called");
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        //switch views
        Log.d(TAG, "onNavigationItemSelected() called with: menuItem = [" + menuItem + "]");
        switch(menuItem.getItemId()){
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                UpdateLyrics();
                break;
            case R.id.nav_message:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ContactFragment()).commit();
                break;
            case R.id.nav_spotify_icon:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SpotifyFragment()).commit();
                break;
            case R.id.nav_share:
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_send:
                Toast.makeText(this, "Send", Toast.LENGTH_SHORT).show();
                break;
    }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Function that given an artist and title returns
    private String GetLyrics(String artist, String title){
        Log.d(TAG, "GetLyrics() called with: artist = [" + artist + "], title = [" + title + "]");
        String lyrics = null;
        try {
            String reply = new Connection().execute("getLyrics",artist,title).get();
            tabsfile.ParseJsonstring(reply);
            lyrics = tabsfile.getLyrics();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return lyrics;
    }

    //Function that fills lyrics screen with lyrics
    public void UpdateLyrics() {
        Log.d(TAG, "UpdateLyrics() called");
        String lyrics = GetLyrics(current_artist, current_title);
        new LyricsUpdater(this).execute(lyrics);
    }

    public void UpdateLyricsButton(View view) {
        Log.d(TAG, "UpdateLyricsButton() called with: view = [" + view + "]");
        textView = findViewById(R.id.fragment_lyrics_text);
        textView.setText("Lyrics are updating");
        UpdateLyrics();
    }

    public void SpotifyLogin(){
        Log.d(TAG, "SpotifyLogin() called");
        new SpotifyConnector(this).execute("authenticate");

    }

    public void SpotifyLoginButton(View view){
        Log.d(TAG, "SpotifyLoginButton() called");
        SpotifyLogin();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], intent = [" + intent + "]");
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    String token = response.getAccessToken();
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
