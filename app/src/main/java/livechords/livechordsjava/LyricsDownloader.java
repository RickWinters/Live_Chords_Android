package livechords.livechordsjava;

import android.os.AsyncTask;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonReader;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import livechords.livechordsjava.Model.Tabsfile;
import livechords.livechordsjava.Model.Tabslines;

public class LyricsDownloader extends AsyncTask<Tabsfile, Object, Object> {

    private static final String TAG = "MYDEBUF_lyricsgetter";
    private WeakReference<MainActivity> activityWeakReference;
    private Tabsfile tabsfile;
    private Tabslines[] tabslines;
    private String[] lyrics;
    private int recursioncounter = 0;
    public static final String NOTABSFOUND = "no_tabs_found";
    private static final String USER_CLIENT = "Mozilla/5.0 (Linux; U; Android 6.0.1; ko-kr; Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";


    private int[] views = {R.id.Lyrics_line_1, R.id.Lyrics_line_2, R.id.Lyrics_line_3, R.id.Lyrics_line_4, R.id.Lyrics_line_5, R.id.Lyrics_line_6, R.id.Lyrics_line_7, R.id.Lyrics_line_8};

    public LyricsDownloader(MainActivity activity){
        activityWeakReference = new WeakReference<MainActivity>(activity);
    }

    private String ExtractTabsFromElement(List<Node> tabElements){
        recursioncounter++;
        StringBuilder tabs = new StringBuilder();
        for (Node element: tabElements){
            if (element.getClass()==TextNode.class){
                tabs.append( ((TextNode) element).getWholeText());
                tabs.append("\n");
            } else {
                List<Node> Newlist = element.childNodes();
                String temp = ExtractTabsFromElement(Newlist);
                tabs.append(temp);
            }
        }
        recursioncounter--;
        return tabs.toString();
    }//TODO: ANALYSIS OF THE ELEMENTS COULD BE BETTER, NOW IT RETURNS A NEW STRING FOR EVERY SEPERATE ELEMENT.



    private String SearchUltimateGuitarTabs(){
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()){
            return null;
        }
        String tab_url = NOTABSFOUND;
        String[] artisttitle = HelperMethods.cleanArtistTitleString(tabsfile.getArtist(), tabsfile.getTitle());
        String artist = artisttitle[0].replace("_"," ").trim();
        String title = artisttitle[1].replace("_", " ").trim();
        String searchurl = "https://www.ultimate-guitar.com/search.php?search_type=title&value="+artist+" "+title;
        URL url = null;
        try {
            url = new URL(searchurl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        //get and parse html
        String reply = HelperMethods.getResponse(url);
        Document doc = Jsoup.parse(reply);
        Elements elements = doc.getElementsByTag("script");
        String resultsstring = null;
        //loop over all elements with the tag script to find the correct one with search results
        for (Element element : elements){
            String child = ( (DataNode) element.childNode(0) ).getWholeData().trim();
            if (child.substring(0,23).equals("window.UGAPP.store.page")){
                resultsstring = child.substring(26,child.length()-1);
                break;
            }
        }
        //Map the search results in a LinkedTreeMap and get the data
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new StringReader(resultsstring));
        reader.setLenient(true);
        LinkedTreeMap map = gson.fromJson(reader, LinkedTreeMap.class);
        LinkedTreeMap datamap = (LinkedTreeMap) map.get("data");

        if ((boolean) datamap.get("not_found")){
            //Show no tabs found and stop this thread
            new TextViewComponentUpdater(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, views[0], TextViewComponentUpdater.COMMAND_TEXT, "no tabs found");
        } else {
            //otherwise filter list on just Chords and get highest rating
            ArrayList<LinkedTreeMap> resultslist = (ArrayList<LinkedTreeMap>) datamap.get("results");
            int i = 0;
            while (i < resultslist.size()) {
                LinkedTreeMap result = resultslist.get(i);
                if (result.containsKey("type") && (result.get("type").equals("Chords"))) {
                    i++;
                } else {
                    resultslist.remove(i);
                }
            }

            //Handle empty result list same as no result found
            if (resultslist.size() == 0) {
                new TextViewComponentUpdater(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, views[0], TextViewComponentUpdater.COMMAND_TEXT, "no tabs found");
            } else {
                //find the highest rating tab_url
                double bestscore = -1;
                for (i = 0; i < resultslist.size(); i++) {
                    LinkedTreeMap result = resultslist.get(i);
                    if (result.containsKey("rating") && ((double) result.get("rating")) > bestscore) {
                        bestscore = (double) result.get("rating");
                        tab_url = (String) result.get("tab_url");
                    }
                }
            }
        }
        return tab_url;
    }

    private void GetUltitameGuitarTabs(String tab_url){
        String tabsString = NOTABSFOUND;
        try {
            URL url = new URL(tab_url);
            //get and parse html
            String tabReply = HelperMethods.getResponse(url);
            Document tabDoc = Jsoup.parse(tabReply);
            Elements preElements = tabDoc.getElementsByTag("pre");
            List<Node> tabElements = null;
            for (Element element : preElements) {
                if (element.childNodeSize() > 0) {
                    tabElements = element.childNodes();
                    break;
                }
            }
            //TODO: tabElements is a list of elements including either text or a list of childnodes with the chords
            tabsString = ExtractTabsFromElement(tabElements);
            String[] tabsStringArray = tabsString.split("\n");
            tabslines = new Tabslines[tabsStringArray.length];
            for (int i = 0; i < tabslines.length; i++){
                tabslines[i] = new Tabslines();
                tabslines[i].setText(tabsStringArray[i]);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void UltimateGuitarTabs(){
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()){
            return;
        }
        publishProgress(views[0], TextViewComponentUpdater.COMMAND_TEXT, "Searching ULtimate Guitar tabs");

        // search for tabs on ultimate guitar tabs
        String tab_url = SearchUltimateGuitarTabs();
        if (!tab_url.equals("no_tabs_found")) {
            GetUltitameGuitarTabs(tab_url);
            publishProgress(views[0], TextViewComponentUpdater.COMMAND_TEXT, "Found tabs on ultimate guitar tabs");
        }
    }

    private void GeniusLyrics(){
        MainActivity activity = activityWeakReference.get();
        if(activity == null || activity.isFinishing()){
            return;
        }
        publishProgress(views[0], TextViewComponentUpdater.COMMAND_TEXT, "Searching lyrics on genius.com");

        String[] artisttitle = HelperMethods.cleanArtistTitleString(tabsfile.getArtist(), tabsfile.getTitle());
        String artist = artisttitle[0].replace("_","%20").trim();
        String title = artisttitle[1].replace("_", "%20").trim();

        String searchurl = "https://api.genius.com/search?q="+artist+"%20"+title;
        Connection connection = Jsoup.connect(searchurl)
                .header("Authorization", "Bearer utx2qbckGnPCuScF4t4WAzP-Po6FIfWI1bOxY8M4-DvVmIkL31iMHLSL02ic01B1")
                .timeout(0)
                .ignoreContentType(true);
        Document doc = null;
        try {
            doc = connection.userAgent(USER_CLIENT).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements bodyelements = doc.getElementsByTag("body");
        Element body = bodyelements.first();
        String JsonString = body.text();

        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new StringReader(JsonString));
        reader.setLenient(true);
        LinkedTreeMap map = gson.fromJson(reader, LinkedTreeMap.class);
        LinkedTreeMap responsemap = (LinkedTreeMap) map.get("response");
        ArrayList hitslist = (ArrayList) responsemap.get("hits");
        String api_path = null;
        String lyrics_url = null;
        Boolean found = false;
        for (Object item : hitslist){
            LinkedTreeMap hit = (LinkedTreeMap) item;
            LinkedTreeMap result = (LinkedTreeMap) hit.get("result");
            String resultTitle = (String) result.get("title");
            resultTitle = HelperMethods.cleanTitleString(resultTitle);
            String tabsfiletitle = HelperMethods.cleanTitleString(tabsfile.getTitle()).replace("_"," ");
            if (resultTitle.equals(tabsfiletitle)){
                api_path = (String) result.get("api_path");
                lyrics_url = (String) result.get("url");
                found = true;
                break;
            }
        }

        connection = Jsoup.connect(lyrics_url)
                .header("Authorization", "Bearer utx2qbckGnPCuScF4t4WAzP-Po6FIfWI1bOxY8M4-DvVmIkL31iMHLSL02ic01B1")
                .timeout(0)
                .ignoreContentType(true);
        Document lyricsdoc = null;
        try {
            lyricsdoc = connection.userAgent(USER_CLIENT).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements LyricsElements = lyricsdoc.getElementsByTag("p");
        Element LyricsElement = LyricsElements.get(0);
        List<TextNode> lyrics_list = LyricsElement.textNodes();
        lyrics = new String[lyrics_list.size()];
        for (int i = 0; i < lyrics_list.size(); i++){
            lyrics[i] = lyrics_list.get(i).text();
        }
        StringBuilder lyricsstring = new StringBuilder();
        for (String line : lyrics){
            lyricsstring.append(line+"\n");
        }
        publishProgress(views[0], TextViewComponentUpdater.COMMAND_TEXT, lyricsstring.toString());

        System.out.println(lyricsdoc);
    }



    @Override
    protected Object doInBackground(Tabsfile... tabsfiles) {
        Log.d(TAG, "doInBackground() called with: tabsfiles = [" + tabsfiles[0].toString() + "]");
        tabsfile = tabsfiles[0];
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()){
            return null;
        }
        publishProgress(views[0], TextViewComponentUpdater.COMMAND_TEXT, "No file found on server");
        publishProgress(views[0], TextViewComponentUpdater.COMMAND_COLOR, ContextCompat.getColor(activity, R.color.active_font_colour));
        activity.setLines(new String[]{"No file found on server"," "," "," "," "," "," "," "});

        // Search  Ultimate guitar tabs
        UltimateGuitarTabs(); //TODO: should return an array of Tabslines to be analysed later.

        GeniusLyrics();

        // Handle no tabs found
        // Search Genius.com
        // Search Azlyrics.com
        return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        Log.d(TAG, "onProgressUpdate() called with: values = [" + values + "]");
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        new TextViewComponentUpdater(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, values);
    }
}
