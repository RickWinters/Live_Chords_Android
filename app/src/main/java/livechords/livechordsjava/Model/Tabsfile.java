package livechords.livechordsjava.Model;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Tabsfile {
    private String artist = "no_file_found";
    private String title = "no_file_found";
    private String tabs = "no_file_found";
    private boolean synced;
    private ArrayList<Tabslines> tabslines = new ArrayList<Tabslines>();
    private boolean has_tabs;
    private boolean has_azlyrics;
    private ArrayList<Chorded_lyrics> chorded_lyrics = new ArrayList<>();
    private String version = "no_file_found";
    private ArrayList<String> azlyrics = new ArrayList<>();


    //CONTSTRUCTORS
    public Tabsfile() {
    }

    public Tabsfile(String artist, String title, String tabs, boolean synced, ArrayList<Tabslines> tabslines, boolean has_tabs, boolean has_azlyrics, ArrayList<Chorded_lyrics> chorded_lyrics, String version, ArrayList<String> azlyrics){
        this.artist = artist;
        this.title = title;
        this.tabs = tabs;
        this.synced = synced;
        this.tabslines = tabslines;
        this.has_tabs = has_tabs;
        this.has_azlyrics = has_azlyrics;
        this.chorded_lyrics = chorded_lyrics;
        this.version = version;
        this.azlyrics = azlyrics;
    }

    //FUNNCTIONS
    public void ParseJsonstring(String jsonstring){
        try {
            JSONObject root = new JSONObject(jsonstring);
            artist = root.getString("artist");
            title = root.getString("title");
            tabs = root.getString("tabs");
            synced = root.getBoolean("synced");
            has_tabs = root.getBoolean("has_tabs");
            has_azlyrics = root.getBoolean("has_azlyrics");
            version = root.getString("version");

            while (azlyrics.size() > 0) {
                azlyrics.remove(0);
            }
            JSONArray azlyric_array = root.getJSONArray("azlyrics");
            for (int i = 0; i < azlyric_array.length(); i++) {
                String item = (String) azlyric_array.get(i);
                azlyrics.add(item);
            }

            while (tabslines.size() > 0) {
                tabslines.remove(0);
            }
            JSONArray tabsline_array = root.getJSONArray("tabslines");
            for (int i = 0; i < tabsline_array.length(); i++) {
                JSONObject item = (JSONObject) tabsline_array.get(i);
                Tabslines tabsline = new Tabslines();
                tabsline.setChords(item.getBoolean("chords"));
                tabsline.setGroup(item.getString("group"));
                tabsline.setLyrics(item.getBoolean("lyrics"));
                tabsline.setText(item.getString("text"));
                tabsline.setKeyword(item.getBoolean("keyword"));
                tabslines.add(tabsline);
            }

            while (chorded_lyrics.size() > 0) {
                chorded_lyrics.remove(0);
            }
            JSONArray chorded_lyric_array = root.getJSONArray("chorded_lyrics");
            for (int i = 0; i < chorded_lyric_array.length(); i++) {
                JSONObject item = (JSONObject) chorded_lyric_array.get(i);
                Chorded_lyrics chorded_lyric = new Chorded_lyrics();
                chorded_lyric.setChords(item.getString("chords"));
                chorded_lyric.setEnd(item.getDouble("end"));
                chorded_lyric.setGroup(item.getString("group"));
                chorded_lyric.setLyrics(item.getString("lyrics"));
                chorded_lyric.setStart(item.getDouble("start"));
                chorded_lyric.setEnd(item.getDouble("end"));
                chorded_lyrics.add(chorded_lyric);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //GETTERS - SETTERS
    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTabs() {
        return tabs;
    }

    public void setTabs(String tabs) {
        this.tabs = tabs;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public ArrayList<Tabslines> getTabslines() {
        return tabslines;
    }

    public void setTabslines(ArrayList<Tabslines> tabslines) {
        this.tabslines = tabslines;
    }

    public boolean isHas_tabs() {
        return has_tabs;
    }

    public void setHas_tabs(boolean has_tabs) {
        this.has_tabs = has_tabs;
    }

    public boolean isHas_azlyrics() {
        return has_azlyrics;
    }

    public void setHas_azlyrics(boolean has_azlyrics) {
        this.has_azlyrics = has_azlyrics;
    }

    public ArrayList<Chorded_lyrics> getChorded_lyrics() {
        return chorded_lyrics;
    }

    public void setChorded_lyrics(ArrayList<Chorded_lyrics> chorded_lyrics) {
        this.chorded_lyrics = chorded_lyrics;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ArrayList<String> getAzlyrics() {
        return azlyrics;
    }

    public void setAzlyrics(ArrayList<String> azlyrics) {
        this.azlyrics = azlyrics;
    }

    public String toString(){
        return "File is: \n" +
                "   Artist     = " + this.artist.replace("%20"," ") + "\n" +
                "   Title      = " + this.title.replace("%20"," ")  + "\n" +
                "   Has tabs   = " + this.has_tabs                                    + "\n" +
                "   Has lyrics = " + this.has_azlyrics                                + "\n" +
                "   Synced     = " + this.synced;
    }

    public String getLyrics() {
        StringBuilder lyrics = new StringBuilder(artist.replace("%20", " ") + "\n" + title.replace("%20", " ") + "\n\n");
        for (int i = 0; i < chorded_lyrics.size(); i++){
            lyrics.append(chorded_lyrics.get(i).getChords()).append("\n").append(chorded_lyrics.get(i).getLyrics()).append("\n\n");
        }
        return lyrics.toString();
    }
}
