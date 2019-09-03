package livechords.livechordsjava;

import android.os.AsyncTask;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import livechords.livechordsjava.Model.Tabsfile;

public class LyricsDownloader extends AsyncTask<Tabsfile, Object, Object> {

    private static final String TAG = "MYDEBUF_lyricsgetter";
    private WeakReference<MainActivity> activityWeakReference;
    private Tabsfile tabsfile;

    private int[] views = {R.id.Lyrics_line_1, R.id.Lyrics_line_2, R.id.Lyrics_line_3, R.id.Lyrics_line_4, R.id.Lyrics_line_5, R.id.Lyrics_line_6, R.id.Lyrics_line_7, R.id.Lyrics_line_8};

    public LyricsDownloader(MainActivity activity){
        activityWeakReference = new WeakReference<MainActivity>(activity);
    }



    private void SearchUltimateGuitarTabs(){
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()){
            return;
        }
        new TextViewComponentUpdater(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, views[0], TextViewComponentUpdater.COMMAND_TEXT, "Searching Ultimate Guitar tabs");
        // search for tabs on ultimate guitar tabs
        String[] artisttitle = HelperMethods.cleanArtistTitleString(tabsfile.getArtist(), tabsfile.getTitle());
        String artist = artisttitle[0].replace("_"," ");
        String title = artisttitle[1].replace("_", " ");
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
            while (i < resultslist.size()){
                LinkedTreeMap result = resultslist.get(i);
                if (result.containsKey("type") && (result.get("type").equals("Chords"))){
                    i++;
                } else {
                    resultslist.remove(i);
                }
            }

            //Handle empty result list same as no result found
            if(resultslist.size()==0){
                new TextViewComponentUpdater(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, views[0], TextViewComponentUpdater.COMMAND_TEXT, "no tabs found");
            } else {
                //find the highest rating tab_url
                double bestscore = -1;
                String tab_url = null;
                for (i = 0; i < resultslist.size(); i++) {
                    LinkedTreeMap result = resultslist.get(i);
                    if (result.containsKey("rating") && ((double) result.get("rating")) > bestscore) {
                        bestscore = (double) result.get("rating");
                        tab_url = (String) result.get("tab_url");
                    }
                }

                // get and parse HTML of Tabs url
                try {
                    url = new URL(tab_url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                //get and parse html
                String tabReply = HelperMethods.getResponse(url);
                Document tabDoc = Jsoup.parse(tabReply);
                Elements tabElements = tabDoc.getAllElements();
                String tabResultsstring = null;

                System.out.println(resultslist);
            }
        }
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
        SearchUltimateGuitarTabs();
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
