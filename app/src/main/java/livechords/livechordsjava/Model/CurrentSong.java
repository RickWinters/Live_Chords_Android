package livechords.livechordsjava.Model;

import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import livechords.livechordsjava.HelperMethods;

public class CurrentSong {
    private static final String TAG = "MYDEBUF_CurrentSong";
    private String artist;
    private String title;
    private long startTime; // start time of the current song in millis since system has started.

    //GENERATORS
    public CurrentSong() {
        this.artist = "no_song_playing";
        this.title = "no_song_playing";
    }

    public CurrentSong(String artist, String title) {
        this.artist = artist;
        this.title = title;
    }

    public void ParseJson(String reply) {
        if (!reply.equals("No song playing")) {
            Log.d(TAG, "ParseJson() called with: reply = [" + reply + "]");
            HashMap<String, Object> replyMap = HelperMethods.GetJSONHashMap(reply);
            HashMap itemMap = (HashMap) replyMap.get("item"); //TODO: handle a null object reference when an add is playing
            title = (String) itemMap.get("name");
            ArrayList artists = (ArrayList) itemMap.get("artists");
            HashMap artistMap = (HashMap) artists.get(0);
            artist = (String) artistMap.get("name");
            int progressMs = Integer.parseInt((String) replyMap.get("progress_ms"));
            long requestTime = SystemClock.elapsedRealtime();
            startTime = requestTime - progressMs;
            Log.d(TAG, "ParseJson() returned: " + artist + " - " + title);
        } else {
            title = "No song playing";
            artist = "No song playing";
        }
    }


    //GETTERS AND SETTERS
    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public long getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return "Artist = " + artist + " - title = " + title;
    }
}
