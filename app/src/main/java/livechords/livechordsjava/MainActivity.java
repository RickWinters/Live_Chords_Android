package livechords.livechordsjava;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private CurrentSong currentSong = new CurrentSong();

    //KEYS
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String KEY_accounName = "accountName_key";
    public static final String KEY_accesToken = "accesToken_key";

    //DATA FOR SPOTIFY CONNECTION
    private String accesToken = "";
    private Boolean loggedIn = false;
    public static final String KEY_loggedIn = "loggedIn_key";
    public static final String KEY_lyricsText = "lyricsText_key";
    public static final String KEY_spotifyLoginButtonText = "spotifyLoginButtonText_key";
    private String currentArtist;
    private String currentTitle;
    private String accountName;

    //Strings for textviews
    private String lyricsText;
    private String spotifyLoginButtonText;

    //OVERRIDDEN METHODS
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

        loadData();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.fragment_lyrics_text, lyricsText);
        new SpotifyConnector(this ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "valid_accestoken", accesToken);
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
                new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.fragment_lyrics_text, lyricsText);
                break;
            case R.id.nav_spotify_icon:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SpotifyFragment()).commit();
                new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.log_in_spotify_button, spotifyLoginButtonText);
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

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause() called");
        saveData();
        super.onPause();
    }

    //SELF MADE FUNCTIONS
    ///LOADING AND SAVING DATA
    public void saveData() {
        Log.d(TAG, "saveData() called");
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_accesToken, accesToken);
        editor.putString(KEY_accounName, accountName);
        editor.putString(KEY_spotifyLoginButtonText, spotifyLoginButtonText);
        editor.putString(KEY_lyricsText, lyricsText);
        editor.putBoolean(KEY_loggedIn, loggedIn);
        editor.apply();

        Toast.makeText(this, "Data saved", Toast.LENGTH_LONG).show();
    }

    public void loadData() {
        Log.d(TAG, "loadData() called");
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        accesToken = sharedPreferences.getString(KEY_accesToken, "");
        accountName = sharedPreferences.getString(KEY_accounName, "");
        lyricsText = sharedPreferences.getString(KEY_lyricsText, "Lyrics come here");
        spotifyLoginButtonText = sharedPreferences.getString(KEY_spotifyLoginButtonText, "Login to new account");
        loggedIn = sharedPreferences.getBoolean(KEY_loggedIn, false);
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
        Log.d(TAG, "UpdateLyrics() called song = " + currentArtist + "_" + currentTitle);
        String lyrics = GetLyrics(currentArtist, currentTitle);
        lyricsText = lyrics;
        new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.fragment_lyrics_text, lyrics);
    }

    public void UpdateLyricsButton(View view) {
        Log.d(TAG, "UpdateLyricsButton() called with: view = [" + view + "]");
        textView = findViewById(R.id.fragment_lyrics_text);
        if (!loggedIn) {
            Toast.makeText(this, "Not logged in to spotify yet", Toast.LENGTH_LONG).show();
        } else {
            new SpotifyConnector(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "checksong", accesToken);
            new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.fragment_lyrics_text, "Lyrics are updating");
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

    //method that handles a call from spotifyconnector to update the text of the button
    public void UpdateLoginButtonText() {
        Log.d(TAG, "UpdateLoginButtonText() called: Account name = " + accountName);
        String string = "Logged into spotify with account name: \n" + accountName;
        new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.log_in_spotify_button, string);
        spotifyLoginButtonText = string;
    }

    //method handling the result of the WebView when logging into spotify
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
                    new SpotifyConnector(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "personal", accesToken);
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


    //SETTERS
    public void setCurrentArtist(String currentArtist) {
        Log.d(TAG, "setCurrentArtist() called with: currentArtist = [" + currentArtist + "]");
        this.currentArtist = currentArtist;
    }

    public void setCurrentTitle(String currentTitle) {
        Log.d(TAG, "setCurrentTitle() called with: currentTitle = [" + currentTitle + "]");
        this.currentTitle = currentTitle;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setCurrentSong(CurrentSong currentSong) {
        this.currentSong = currentSong;
    }

    public void LogOutUser() {
        this.accesToken = "";
        this.loggedIn = false;
        this.spotifyLoginButtonText = "login to new user";
        new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.log_in_spotify_button, "Login to new account");
        new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.fragment_lyrics_text, "Lyrics come here");
        Toast.makeText(this, "Acces token expired, " + accountName + " logged out from spotify", Toast.LENGTH_LONG).show();
    }

    public void UserStillLoggedIn() {
        Toast.makeText(this, "User " + accountName + " still logged in", Toast.LENGTH_LONG).show();
    }
}
