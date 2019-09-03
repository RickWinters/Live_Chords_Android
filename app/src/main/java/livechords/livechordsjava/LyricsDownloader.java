package livechords.livechordsjava;

import android.os.AsyncTask;
import android.util.Log;

import androidx.core.content.ContextCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import livechords.livechordsjava.Model.Tabsfile;

public class LyricsDownloader extends AsyncTask<Tabsfile, Object, Object> {

    private static final String TAG = "MYDEBUF_lyricsgetter";
    private WeakReference<MainActivity> activityWeakReference;
    private Tabsfile tabsfile;

    private int[] views = {R.id.Lyrics_line_1, R.id.Lyrics_line_2, R.id.Lyrics_line_3, R.id.Lyrics_line_4, R.id.Lyrics_line_5, R.id.Lyrics_line_6, R.id.Lyrics_line_7, R.id.Lyrics_line_8};

    public LyricsDownloader(MainActivity activity){
        activityWeakReference = new WeakReference<MainActivity>(activity);
    }

    private String[] seperaateLines(String input){
        ArrayList<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (char letter : input.toCharArray()) {
            if (letter == '\n') {
                lines.add(line.toString());
                line.delete(0, line.length());
            } else {
                line.append(letter);
            }
        }
        String[] stringlines = (String[]) lines.toArray();
        return stringlines;
    }

    private void SearchUltimateGuitarTabs(){
        String[] artisttitle = HelperMethods.cleanArtistTitleString(tabsfile.getArtist(), tabsfile.getTitle());
        String artist = artisttitle[0].replace("_"," ");
        String title = artisttitle[1].replace("_", " ");
        String searchurl = "https://www.ultimate-guitar.com/search.php?search_type=title&value="+artist+" "+title;
        try {
            URL url = new URL(searchurl);
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
            //Map the search results in a Hashmap and get
            HashMap<String, Object> resultsmap = HelperMethods.GetJSONHashMap(resultsstring);
            System.out.println(resultsmap);



        } catch (MalformedURLException e) {
            e.printStackTrace();
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
