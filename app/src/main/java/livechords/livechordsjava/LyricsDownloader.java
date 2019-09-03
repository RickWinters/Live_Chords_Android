package livechords.livechordsjava;

import android.os.AsyncTask;
import android.util.Log;

import androidx.core.content.ContextCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import livechords.livechordsjava.Model.Tabsfile;

public class LyricsDownloader extends AsyncTask<Tabsfile, Object, Object> {

    private static final String TAG = "MYDEBUF_lyricsgetter";
    private WeakReference<MainActivity> activityWeakReference;
    private Tabsfile tabsfile;

    private int[] views = {R.id.Lyrics_line_1, R.id.Lyrics_line_2, R.id.Lyrics_line_3, R.id.Lyrics_line_4, R.id.Lyrics_line_5, R.id.Lyrics_line_6, R.id.Lyrics_line_7, R.id.Lyrics_line_8};
    private StringBuilder response = new StringBuilder();

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

    private String getResponse(URL url){
        Log.d(TAG, "getResponse() called with: url = [" + url + "]");
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setReadTimeout(5000);
            connection.getInputStream();

            int status = connection.getResponseCode();
            //Log.i("ServerConnection", "Status = "+status);

            BufferedReader reader;
            String line;
            if (status > 299){
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while((line = reader.readLine()) != null){
                    response.append(line);
                }
                reader.close();
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while((line = reader.readLine()) != null){
                    response.append(line);
                }
                reader.close();
            }

            //Log.i("ServerConnection", response.toString());
            return response.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    private void SearchUltimateGuitarTabs(){
        String artist = tabsfile.getArtist().replace("%20"," ").replace("_"," ").trim();
        String title = tabsfile.getTitle().replace("%20"," ").replace("_"," ").trim();
        String searchurl = "https:/www.ultimate-guitar.com/search.php?search_type=title&value="+artist+" "+title;
        try {
            URL url = new URL(searchurl);
            String reply = getResponse(url);
            Document doc = Jsoup.parse(reply);
            Elements elements = doc.getElementsByTag("script");
            for (Element element : elements){
                Elements child = element.getAllElements();
                System.out.println(child);
            }
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
