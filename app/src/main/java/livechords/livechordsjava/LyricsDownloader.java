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

import livechords.livechordsjava.Model.Chorded_lyrics;
import livechords.livechordsjava.Model.Tabsfile;
import livechords.livechordsjava.Model.Tabslines;

public class LyricsDownloader extends AsyncTask<Tabsfile, Object, Object> {

    private static final String TAG = "MYDEBUG_lyricsgetter";
    private WeakReference<MainActivity> activityWeakReference;
    private Tabsfile tabsfile;
    private ArrayList<Tabslines> tabslines = new ArrayList<>();
    private String tabs;
    private ArrayList<Chorded_lyrics> chorded_lyrics = new ArrayList<>();
    private String[] lyrics = new String[]{NOTABSFOUND};


    private boolean found_tabs = false;
    private boolean found_lyrics = false;

    private int recursioncounter = 0;

    public static final String NOTABSFOUND = "no_tabs_found";
    private static final String USER_CLIENT = "Mozilla/5.0 (Linux; U; Android 6.0.1; ko-kr; Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
    private int[] views = {R.id.Lyrics_line_1, R.id.Lyrics_line_2, R.id.Lyrics_line_3, R.id.Lyrics_line_4, R.id.Lyrics_line_5, R.id.Lyrics_line_6, R.id.Lyrics_line_7, R.id.Lyrics_line_8};

    public LyricsDownloader(MainActivity activity){
        activityWeakReference = new WeakReference<MainActivity>(activity);
    }

    private String ExtractTabsFromElement(List<Node> tabElements){
        Log.d(TAG, "ExtractTabsFromElement() called with: tabElements = [" + tabElements + "]");
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
    }//

    private String ExtractLyricsFromelements(List<Node> lyricsElements){
        Log.d(TAG, "ExtractLyricsFromelements() called with: lyricsElements = [" + lyricsElements + "]");
        StringBuilder lyrics = new StringBuilder();
        for (Node element: lyricsElements){
            if (element.getClass()==TextNode.class){
                lyrics.append(((TextNode) element).getWholeText());
                lyrics.append("\n");
            } else {
                List<Node> Newlist = element.childNodes();
                String temp = ExtractLyricsFromelements(Newlist);
                lyrics.append(temp);
            }
        }
        return lyrics.toString();
    }

    private String SearchUltimateGuitarTabs(){
        Log.d(TAG, "SearchUltimateGuitarTabs() called");
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
        Log.d(TAG, "GetUltitameGuitarTabs() called with: tab_url = [" + tab_url + "]");
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
            tabs = ExtractTabsFromElement(tabElements); // returns one string of all the chords and lyrics, seperated by a newline
            String[] tabsStringArray = tabs.split("\n");
            for (int i = 0; i < tabsStringArray.length; i++){
                tabslines.add(new Tabslines());
                tabslines.get(i).setText(tabsStringArray[i]);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void UltimateGuitarTabs(){
        Log.d(TAG, "UltimateGuitarTabs() called");
        publishProgress(views[0], TextViewComponentUpdater.COMMAND_TEXT, "Searching ULtimate Guitar tabs");

        // search for tabs on ultimate guitar tabs
        String tab_url = SearchUltimateGuitarTabs();
        if (!tab_url.equals(NOTABSFOUND)) {
            GetUltitameGuitarTabs(tab_url);
            publishProgress(views[0], TextViewComponentUpdater.COMMAND_TEXT, "Found tabs on ultimate guitar tabs");
            found_tabs = true;
        } else {
            tabslines.add(new Tabslines());
            tabslines.get(0).setText(NOTABSFOUND);
        }
    }

    private String SearchGenuisLyrics(){
        Log.d(TAG, "SearchGenuisLyrics() called");
        String lyrics_url = NOTABSFOUND;
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
        for (Object item : hitslist){
            LinkedTreeMap hit = (LinkedTreeMap) item;
            LinkedTreeMap result = (LinkedTreeMap) hit.get("result");
            String resultTitle = (String) result.get("title");
            resultTitle = HelperMethods.cleanTitleString(resultTitle);
            String tabsfiletitle = HelperMethods.cleanTitleString(tabsfile.getTitle()).replace("_"," ");
            if (resultTitle.equals(tabsfiletitle)){
                lyrics_url = (String) result.get("url");
                break;
            }
        }
        return lyrics_url;
    }

    private void GetGeniusLyrics(String lyrics_url){
        Log.d(TAG, "GetGeniusLyrics() called with: lyrics_url = [" + lyrics_url + "]");
        Connection connection = Jsoup.connect(lyrics_url)
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
        List<Node> LyricsElement = LyricsElements.get(0).childNodes();
        String lyrics_string = ExtractLyricsFromelements(LyricsElement);
        lyrics = lyrics_string.split("\n");


    }

    private void GeniusLyrics(){
        // this method handles searching lyrics on Genius and than tries to download the lyrics and puts them in class wide String[] lyrics
        Log.d(TAG, "GeniusLyrics() called");
        publishProgress(views[0], TextViewComponentUpdater.COMMAND_TEXT, "Searching lyrics on Genius.com");

        String lyrics_url = SearchGenuisLyrics();
        if(!lyrics_url.equals(NOTABSFOUND)){
            GetGeniusLyrics(lyrics_url);
            publishProgress(views[0], TextViewComponentUpdater.COMMAND_TEXT, "Found lyrics on Genius.com");
            found_lyrics = true;
        } else {
            publishProgress(views[0], TextViewComponentUpdater.COMMAND_TEXT, "No lyrics found, stopping search");
        }
    }

    private void FindLyricsInTabslines(){//This method will analyze the lyrics[] and tabslines[] and mark which Tabslines are lyrics
        String[] keywords = new String[]{"verse","chorus","interlude","instrumental","bridge","intro","outro","ad-libs","adlibs"};
        publishProgress(views[0], TextViewComponentUpdater.COMMAND_TEXT, "Comparing lyrics from both sides");
        Log.d(TAG, "FindLyricsInTabslines() called");
        int lyrics_index = 0;
        int tabslines_index = 0;
        boolean end = false;

        outerloop: for (lyrics_index = 0; lyrics_index < lyrics.length; lyrics_index++){ //loop over all the lyrics
            String lyrics_text = HelperMethods.cleanlyricsComparingString(lyrics[lyrics_index], true);
            String double_lyrics_text = "";
            String triple_lyrics_text = "";

            //check if lyrics_text is not Empty
            if(lyrics_text.equals("")){
                continue;
            }

            //check if the lyricsline contains any of the keywords, if so skip this lyrics line
            for (String keyword : keywords){
                if (lyrics_text.toLowerCase().contains(keyword)){
                    continue outerloop;
                }
            }

            // create a double_lyrics_text because sometimes the tabslines_lyrics migt be equal to two lines of lyrics. this needs to be counted as lyrics
            // or triple (Chasing Cars, Snow Patrol -_-, seriously this is a mess)
            if(lyrics_index < lyrics.length-1){
                double_lyrics_text = lyrics_text + HelperMethods.cleanlyricsComparingString(lyrics[lyrics_index+1], true);
            }
            if(lyrics_index < lyrics.length-2){
                triple_lyrics_text = double_lyrics_text + HelperMethods.cleanlyricsComparingString(lyrics[lyrics_index+2], true);
            }

            // check the tabslines to see if they are equal
            tabslines_whileloop: while (tabslines_index < tabslines.size()){
                String tabslines_text = HelperMethods.cleanlyricsComparingString(tabslines.get(tabslines_index).getText(), false);
                //check if the tabslines is empty, if so continue with next
                if(tabslines_text.length()==0){
                    tabslines_index++;
                    continue;
                }

                //check if the tabslines are any keywords, if so mark it as keyword and continue with next
                for (String keyword : keywords){
                    if (tabslines_text.toLowerCase().contains(keyword)){
                        tabslines.get(tabslines_index).setKeyword(true);
                        tabslines_index++;
                        continue tabslines_whileloop;
                    }
                }

                // set the tabslines_text to lowercase if its longer than 5 characters.
                // the lyrics are usually longer than 5 characters and chords should be in capital letter.
                if(tabslines_text.length()>5){
                    tabslines_text = tabslines_text.toLowerCase();
                }
                // TODO: PUT LOWER TODO IN SUCH A WAY THAT IF A LYRICS LINE EQUALS 2 SEQUENTIAL TABSLINES LYRICS (ARE OTHERWAY AROUND) AR NOT SKIPPED.

                // TODO: DEFINE METHOD THAT RETURNS A VALUE BETWEEN 0 AND 1 REPRESENTING THE SIMILARITY OF 2 STRINGS, WHERE A VALUE OF 1 IS EXACTLY SIMILAR.
                // AND THAN SET TABSLINES.LYRICS TO TRUE IF SIMILARITY > 0.8 (EXAMPLE). THIS TO ENSURE 2 STRINGS WHICH DIFFER ONLY SLIGHLY, ARE ALSO MARKED AS LYRICS
                double similarity = HelperMethods.similarity(lyrics_text, tabslines_text);
                double doublesimilarity = HelperMethods.similarity(double_lyrics_text, tabslines_text);
                double triplesimilarity = HelperMethods.similarity(triple_lyrics_text, tabslines_text);
                if (similarity > 0.5) {
                    tabslines.get(tabslines_index).setLyrics(true);
                    Log.d(TAG, "FindLyricsInTabslines: matched ["+lyrics_text+"] and ["+tabslines_text+"]");
                    continue outerloop;
                } else if (doublesimilarity > 0.5) {
                    tabslines.get(tabslines_index).setLyrics(true);
                    lyrics_index++;
                    Log.d(TAG, "FindLyricsInTabslines: matched ["+double_lyrics_text+"] and ["+tabslines_text+"]");
                    continue outerloop;
                } else if (triplesimilarity > 0.5) {
                    tabslines.get(tabslines_index).setLyrics(true);
                    lyrics_index++;
                    lyrics_index++;
                    Log.d(TAG, "FindLyricsInTabslines: matched ["+triple_lyrics_text+"] and ["+tabslines_text+"]");
                    continue outerloop;
                }
                tabslines_index++;
            }
        }
        System.out.println("test");
    }//Marks all the elements in tabslines[] as lyrics when comparing to lyrics[]

    private void GroupTabslines(){
        Log.d(TAG, "GroupTabslines() called");
        publishProgress(views[0], TextViewComponentUpdater.COMMAND_TEXT, "Grouping lyrics and chords together");
        String group = "start";
        int i = 0;
        Tabslines line = null;
        while (i < tabslines.size()){
            line = tabslines.get(i);
            line.setGroup(group);
            if (line.isKeyword()){
                group = line.getText();
                line.setGroup(group);
                if( (i < tabslines.size()-1 ) && tabslines.get(i+1).getText().equals("")){
                    tabslines.get(i+1).setGroup(group);
                    i+=1;
                }
            }
            if (line.getText().equals("")){
                tabslines.remove(line);
                group = "verse";
                i-=1;
            }
            i+=1;
        }
    }

    private void CreateChordedLyrics(){
        Log.d(TAG, "CreateChordedLyrics() called");
        publishProgress(views[0], TextViewComponentUpdater.COMMAND_TEXT, "Finalizing efforts");
        String introtext = "";
        Boolean inintro = false;
        String starttext = "";
        Boolean instart = false;
        String solotext = "";
        Boolean insolo = false;
        Boolean inbreak = false;

        int i = 0;
        int passed = 0;
        while( i < tabslines.size()){
            Tabslines line = tabslines.get(i);
            String lineGroup = line.getGroup().toLowerCase();
            if (lineGroup.contains("solo") || lineGroup.contains("instrumental") || lineGroup.contains("interlude") || lineGroup.contains("pre-verse")){
                insolo = true;
                passed = i;
                if(! line.getText().equals("")){
                    solotext = solotext + line.getText() + "\n";
                }
            } else if (insolo){
                chorded_lyrics.add(new Chorded_lyrics(solotext, 0, 0, "", "solo", 0));
                solotext = "";
                insolo = false;
            }

            if (lineGroup.contains("intro")){
                inintro = true;
                passed = i;
                if(line.getText().equals("")){
                    line.setGroup("verse");
                } else {
                    introtext = introtext + line.getText() + "\n";
                }
            } else if (inintro){
                chorded_lyrics.add(new Chorded_lyrics(introtext, 0,0,"","intro",0));
                introtext = "";
                inintro = false;
            }

            if (lineGroup.contains("start")){
                instart = true;
                passed = i;
                if(!line.getText().equals("")){
                    starttext = starttext + line.getText() + "\n";
                }
            } else if (instart){
                chorded_lyrics.add(new Chorded_lyrics(starttext, 0, 0, "", "start",0));
                starttext = "";
                instart = false;
            }

            if (lineGroup.contains("break") && !(insolo || inintro || instart || inbreak)){
                inbreak = true;

                int j = 0;
                Boolean no_lyrics = true;
                while( (i+j < tabslines.size()) && tabslines.get(i+j).getGroup().toLowerCase().contains("break")){
                    if ((tabslines.get(i+j).isLyrics()) && (j > 0)){
                        no_lyrics = false;
                        break;
                    } else {
                        solotext = solotext + tabslines.get(i+j).getText() + "\n";
                    }
                    j += 1;
                }
                if (no_lyrics){
                    chorded_lyrics.add(new Chorded_lyrics(solotext, 0, 0, "", "break", 0));
                    passed = i;
                    i += 1;
                }

            } else if (inbreak){
                solotext = "";
                inbreak = false;
            }
            if ( !(instart || inbreak || inintro || insolo)){
                if (line.isLyrics()){
                    String lyrics = line.getText();
                    String chords = "";
                    passed += 1;
                    while (passed < i){
                        chords = chords + tabslines.get(passed).getText();
                        passed += 1;
                    }
                    String group = line.getGroup();
                    chorded_lyrics.add(new Chorded_lyrics(lyrics, 0, 0, group, chords, 0));
                }
                if (line.isKeyword()){
                    passed = i;
                    chorded_lyrics.add(new Chorded_lyrics(line.getText(), 0, 0, line.getGroup(), "", 0));
                }
            }
            i += 1;
        }
    }

    private void CreateTabsfile(){
        Log.d(TAG, "CreateTabsfile() called");
        publishProgress(views[0], TextViewComponentUpdater.COMMAND_TEXT, "Filing it all away");
        tabsfile.setHas_azlyrics(true);
        tabsfile.setHas_tabs(true);
        tabsfile.setChorded_lyrics(chorded_lyrics);
        tabsfile.setTabslines(tabslines);
        tabsfile.setSynced(false);
        tabsfile.setVersion("android");
        tabsfile.setTabs(tabs);
        tabsfile.setTabslines(tabslines);

        ArrayList<String> azlyrics = new ArrayList<>();
        for (String line : lyrics){
            azlyrics.add(line);
        }
        tabsfile.setAzlyrics(azlyrics);
    }

    //TODO make a method to reorder the tabslines so they coincide with the tabslines as with the desktop version


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
        UltimateGuitarTabs(); //First calls searchUltimateGuitartabs which returns a string NOTABSFOUND or a link. Than opens the link to download the tabs to class variable tabslines[]
        if(! ( (String) tabslines.get(0).getText() ).equals(NOTABSFOUND) ){
            GeniusLyrics();
        } else {
            publishProgress(views[0], TextViewComponentUpdater.COMMAND_TEXT, "No tabs found, cancelling search. ");
        }

        if(found_lyrics && found_tabs){
            FindLyricsInTabslines();
            GroupTabslines();
            CreateChordedLyrics();
            CreateTabsfile();
            activity.setTabsfile(tabsfile);
            activity.UploadNewTabsfile();
        }
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
