package livechords.livechordsjava;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import livechords.livechordsjava.Model.Chorded_lyrics;
import livechords.livechordsjava.Model.CurrentSong;
import livechords.livechordsjava.Model.Tabsfile;

public class LyricsUpdater extends AsyncTask<Object, Object, Void> {
    private static final String TAG = "MYDEBUG_LyricsUpdater";
    private WeakReference<MainActivity> activityWeakReference;
    private int[] views = {R.id.Lyrics_line_1, R.id.Lyrics_line_2, R.id.Lyrics_line_3, R.id.Lyrics_line_4, R.id.Lyrics_line_5, R.id.Lyrics_line_6, R.id.Lyrics_line_7, R.id.Lyrics_line_8};

    public LyricsUpdater(MainActivity activity) {
        activityWeakReference = new WeakReference<MainActivity>(activity);
    }


    private void Synced_Lyrics(Tabsfile tabsfile, CurrentSong currentSong) {
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()){
            return;
        }
        Log.d(TAG, "Synced_Lyrics() called with: tabsfile = [" + tabsfile + "], currentSong = [" + currentSong + "]");
        ArrayList<Chorded_lyrics> chordedLyrics = tabsfile.getChorded_lyrics();

        int nlines = chordedLyrics.size();
        int active_line = 0;
        int display_line = 1;
        int start_offset = 0;
        int old_active_line = -1;
        long progress_ms = SystemClock.elapsedRealtime() - currentSong.getStartTime();
        while (true) { //while true loop to get initial active line.
            Chorded_lyrics line = chordedLyrics.get(active_line);
            double startTime = line.getStart() * 1000;
            if (startTime * 1000 > progress_ms) {
                active_line += 1;
            } else {
                break;
            }
        }

        while (active_line < nlines) {
            double nextStart = chordedLyrics.get(active_line).getStart();
            double currentProgress = (SystemClock.elapsedRealtime() - currentSong.getStartTime()) / 1000.0;
            if (nextStart < currentProgress) {
                active_line += 1;
            }
            if (active_line > 2) {
                start_offset = 2;
            } else {
                start_offset = active_line;
            }
            if (active_line > 1) {
                display_line = 1;
            } else {
                display_line = active_line;
            }

            if (old_active_line != active_line) {
                String[] savelines = new String[8];
                Log.d(TAG, "Synced_Lyrics(), active line = " + active_line + "/" + nlines);
                for (int i = 0; i < 8; i++) {
                    int index = active_line - start_offset + i;
                    String lyrics = " ";
                    if (index < chordedLyrics.size()) {
                        lyrics = chordedLyrics.get(index).getChords() + "\n" + chordedLyrics.get(index).getLyrics();
                    }
                    savelines[i] = lyrics;
                    publishProgress(views[i], TextViewComponentUpdater.COMMAND_TEXT, lyrics);
                    int color = (int) ContextCompat.getColor(activity, R.color.inactive_font_colour);
                    if (i == display_line) { color = (int) ContextCompat.getColor(activity, R.color.active_font_colour);}
                    publishProgress(views[i], TextViewComponentUpdater.COMMAND_COLOR, color);
                }
                activity.setLines(savelines);
                old_active_line = active_line;
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void Unsynced_lyrics(Tabsfile tabsfile) {
        Log.d(TAG, "Unsynced_lyrics() called with: tabsfile = [" + tabsfile + "]");
        StringBuilder lyrics = new StringBuilder();
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()){
            return;
        }
        for (Chorded_lyrics line : tabsfile.getChorded_lyrics()) {
            lyrics.append(line.getChords()).append("\n");
            lyrics.append(line.getLyrics()).append("\n\n");
        }
        publishProgress(views[0], TextViewComponentUpdater.COMMAND_TEXT, lyrics.toString());
        publishProgress(views[0], TextViewComponentUpdater.COMMAND_COLOR, ContextCompat.getColor(activity, R.color.active_font_colour));
        publishProgress(views[0], TextViewComponentUpdater.COMMAND_SCROLLABLE, true);
        activity.setLines(new String[]{lyrics.toString()," "," "," "," "," "," "," "});
    }

    @Override
    protected Void doInBackground(Object... objects) {
        Log.d(TAG, "doInBackground() called with: objects = [" + Arrays.toString(objects) + "]");
        Tabsfile tabsfile = (Tabsfile) objects[0];
        CurrentSong currentSong = (CurrentSong) objects[1];
        MainActivity activity = activityWeakReference.get();
        publishProgress(views[0], TextViewComponentUpdater.COMMAND_SCROLLABLE, false);
        if (activity == null || activity.isFinishing()){
            return null;
        }
        if (tabsfile.isHas_tabs() && tabsfile.isHas_azlyrics()) {
            if (tabsfile.isSynced()) {
                Synced_Lyrics(tabsfile, currentSong);
            } else {
                Unsynced_lyrics(tabsfile);
            }
        } else {
            publishProgress(views[0], TextViewComponentUpdater.COMMAND_TEXT, "No file found on server");
            publishProgress(views[0], TextViewComponentUpdater.COMMAND_COLOR, ContextCompat.getColor(activity, R.color.active_font_colour));
            activity.setLines(new String[]{"No file found on server"," "," "," "," "," "," "," "});
        }
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
