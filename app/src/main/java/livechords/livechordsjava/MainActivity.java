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
import livechords.livechordsjava.Model.LyricsUpdater;
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
    public static final String KEY_titleText = "titleText_key";
    public static final String KEY_line1 = "line1_key";
    public static final String KEY_line2 = "line2_key";
    public static final String KEY_line3 = "line3_key";
    public static final String KEY_line4 = "line4_key";
    public static final String KEY_line5 = "line5_key";
    public static final String KEY_line6 = "line6_key";
    public static final String KEY_line7 = "line7_key";
    public static final String KEY_line8 = "line8_key";
    public static final String KEY_spotifyLoginButtonText = "spotifyLoginButtonText_key";
    private String currentArtist;
    private String currentTitle;
    private String accountName;

    //Strings for textviews
    private String titleText;
    private String line1;
    private String line2;
    private String line3;
    private String line4;
    private String line5;
    private String line6;
    private String line7;
    private String line8;
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
        new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_Title, titleText);
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
                new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_Title, titleText);
                new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_line_1, line1);
                new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_line_2, line2);
                new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_line_3, line3);
                new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_line_4, line4);
                new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_line_5, line5);
                new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_line_6, line6);
                new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_line_7, line7);
                new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_line_8, line8);
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
        editor.putString(KEY_titleText, titleText);
        editor.putString(KEY_line1, line1);
        editor.putString(KEY_line2, line2);
        editor.putString(KEY_line3, line3);
        editor.putString(KEY_line4, line4);
        editor.putString(KEY_line5, line5);
        editor.putString(KEY_line6, line6);
        editor.putString(KEY_line7, line7);
        editor.putString(KEY_line8, line8);
        editor.putBoolean(KEY_loggedIn, loggedIn);
        editor.apply();

        Toast.makeText(this, "Data saved", Toast.LENGTH_LONG).show();
    }

    public void loadData() {
        Log.d(TAG, "loadData() called");
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        accesToken = sharedPreferences.getString(KEY_accesToken, "");
        accountName = sharedPreferences.getString(KEY_accounName, "");
        titleText = sharedPreferences.getString(KEY_titleText, "Lyrics come here");
        line1 = sharedPreferences.getString(KEY_line1, "-\n-");
        line2 = sharedPreferences.getString(KEY_line2, "-\n-");
        line3 = sharedPreferences.getString(KEY_line3, "-\n-");
        line4 = sharedPreferences.getString(KEY_line4, "-\n-");
        line5 = sharedPreferences.getString(KEY_line5, "-\n-");
        line6 = sharedPreferences.getString(KEY_line6, "-\n-");
        line7 = sharedPreferences.getString(KEY_line7, "-\n-");
        line8 = sharedPreferences.getString(KEY_line8, "-\n-");
        spotifyLoginButtonText = sharedPreferences.getString(KEY_spotifyLoginButtonText, "Login to new account");
        loggedIn = sharedPreferences.getBoolean(KEY_loggedIn, false);
    }

    //Function that given an artist and title returns the lyrics
    private void GetLyrics(String artist, String title) {
        Log.d(TAG, "GetLyrics() called with: artist = [" + artist + "], title = [" + title + "]");
        try {
            String reply = new ServerConnection().execute("getLyrics", artist, title).get();
            tabsfile.ParseJsonstring(reply);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Function that fills lyrics screen with lyrics
    public void UpdateLyrics() {
        Log.d(TAG, "UpdateLyrics() called song = " + currentArtist + "_" + currentTitle);
        GetLyrics(currentArtist, currentTitle);
        new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_Title, currentArtist.replace("_", " ") + "\n" + currentTitle.replace("_", " "));
        new LyricsUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tabsfile, currentSong);
    }

    public void UpdateLyricsButton(View view) {
        Log.d(TAG, "UpdateLyricsButton() called with: view = [" + view + "]");
        textView = findViewById(R.id.Lyrics_Title);
        if (!loggedIn) {
            Toast.makeText(this, "Not logged in to spotify yet", Toast.LENGTH_LONG).show();
        } else {
            new SpotifyConnector(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "checksong", accesToken);
            new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_Title, "Lyrics are updating\n");
            new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_line_1, "-\n-");
            new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_line_2, "-\n-");
            new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_line_3, "-\n-");
            new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_line_4, "-\n-");
            new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_line_5, "-\n-");
            new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_line_6, "-\n-");
            new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_line_7, "-\n-");
            new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_line_8, "-\n-");
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

    public void LogOutUser() {
        this.accesToken = "";
        this.loggedIn = false;
        this.spotifyLoginButtonText = "login to new user";
        this.currentSong = null;
        new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.log_in_spotify_button, "log in with new account");
        new TextViewUpdater(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, R.id.Lyrics_Title, "lyrics come here");
        Toast.makeText(this, "Acces token expired, " + accountName + " logged out from spotify", Toast.LENGTH_LONG).show();
    }

    public void UserStillLoggedIn() {
        Toast.makeText(this, "User " + accountName + " still logged in", Toast.LENGTH_LONG).show();
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
}
