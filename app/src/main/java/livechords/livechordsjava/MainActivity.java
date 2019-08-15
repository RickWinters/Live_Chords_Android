package livechords.livechordsjava;

import android.content.Intent;
import android.os.AsyncTask;
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
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.concurrent.ExecutionException;

import livechords.livechordsjava.Fragment_classes.HomeFragment;
import livechords.livechordsjava.Fragment_classes.SpotifyFragment;
import livechords.livechordsjava.Model.CurrentSong;
import livechords.livechordsjava.Model.Tabsfile;

import static com.spotify.sdk.android.authentication.LoginActivity.REQUEST_CODE;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MYDEBUG_Main_Activity";
    //VIEWS
    private DrawerLayout drawer;
    private TextView textView;
    private FrameLayout frameLayout;
    private NavigationView navigationView;

    //DATA FOR SONGS AND STUFF
    private Tabsfile tabsfile = new Tabsfile();
    private String current_artist = "Flogging_Molly";
    private String current_title = "Drunken_Lullabies";
    private CurrentSong currentSong = new CurrentSong();

    //DATA FOR SPOTIFY CONNECTION
    private String accesToken = "";
    private Boolean loggedIn = false;

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
                //UpdateLyrics();
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

    //Function that given an artist and title returns the lyrics
    private String GetLyrics(String artist, String title){
        Log.d(TAG, "GetLyrics() called with: artist = [" + artist + "], title = [" + title + "]");
        String lyrics = null;
        try {
            String reply = new ServerConnection().execute("getLyrics", artist, title).get();
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
        Log.d(TAG, "UpdateLyrics() called song" + current_artist + " - " + current_title);
        String lyrics = GetLyrics(current_artist, current_title);
        new LyricsUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, lyrics);
    }

    public void UpdateLyricsButton(View view) {
        Log.d(TAG, "UpdateLyricsButton() called with: view = [" + view + "]");
        textView = findViewById(R.id.fragment_lyrics_text);
        try {
            if (!loggedIn) {
                Toast.makeText(this, "Not logged in to spotify yet", Toast.LENGTH_LONG).show();
            } else {
                currentSong = new SpotifyConnector(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "checksong", accesToken).get();
            }
            if (!currentSong.getArtist().equals(current_artist) || !currentSong.getTitle().equals(current_title)) {
                textView.setText("Lyrics are updating");
                current_title = currentSong.getTitle();
                current_artist = currentSong.getArtist();
                UpdateLyrics();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void SpotifyLogin(){
        Log.d(TAG, "SpotifyLogin() called");
        new SpotifyConnector(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "authenticate");

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
                    Log.d(TAG, "onActivityResult: User logged in");
                    accesToken = response.getAccessToken();
                    loggedIn = true;
                    new SpotifyConnector(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "checksong", accesToken);
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

    public void setCurrent_artist(String current_artist) {
        Log.d(TAG, "setCurrent_artist() called with: current_artist = [" + current_artist + "]");
        this.current_artist = current_artist;
    }

    public void setCurrent_title(String current_title) {
        Log.d(TAG, "setCurrent_title() called with: current_title = [" + current_title + "]");
        this.current_title = current_title;
    }
}
