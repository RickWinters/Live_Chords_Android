package livechords.livechordsjava.Model;

import android.util.Log;

import java.util.HashMap;

import livechords.livechordsjava.HelperMethods;

public class SpotifyAccount {
    private static final String TAG = "MYDEBUG_Spotify_Account";
    private String name;
    private String accesToken;

    //CONSTRUCTORS
    public SpotifyAccount() {
    }

    public SpotifyAccount(String name, String accesToken) {
        this.name = name;
        this.accesToken = accesToken;
    }

    //FUNCTIONS

    public void ParseJson(String reply) {
        Log.d(TAG, "ParseJson() called with: reply = [" + reply + "]");
        HashMap<String, Object> map = HelperMethods.GetJSONHashMap(reply);
        name = (String) map.get("display_name");
        Log.d(TAG, "ParseJson() returned: " + map);

    }

    @Override
    public String toString() {
        return "Class: SpotifyAccount, name = " + name;
    }

    //GETTERS AND SETTERS
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccesToken() {
        return accesToken;
    }

    public void setAccesToken(String accesToken) {
        this.accesToken = accesToken;
    }

}
