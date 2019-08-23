package livechords.livechordsjava.Model;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import livechords.livechordsjava.MainActivity;
import livechords.livechordsjava.R;
import livechords.livechordsjava.TextViewUpdater;

public class LyricsUpdater extends AsyncTask<Object, Object, Void> {
    private static final String TAG = "MYDEBUG_LyricsUpdater";
    private WeakReference<MainActivity> activityWeakReference;
    private int[] views = {R.id.Lyrics_line_1, R.id.Lyrics_line_2, R.id.Lyrics_line_3, R.id.Lyrics_line_4, R.id.Lyrics_line_5, R.id.Lyrics_line_6, R.id.Lyrics_line_7, R.id.Lyrics_line_8};

    public LyricsUpdater(MainActivity activity) {
        activityWeakReference = new WeakReference<MainActivity>(activity);
    }


    private void Synced_Lyrics(Tabsfile tabsfile, CurrentSong currentSong) {
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
            double nextStart = chordedLyrics.get(active_line).getStart() - 0.1;
            double currentProgress = (SystemClock.elapsedRealtime() - currentSong.getStartTime()) / 1000.0;
            if (nextStart < currentProgress) {
                active_line += 1;
            }
            if (active_line > 2) {
                start_offset = 2;
            } else {
                start_offset = active_line;
            }
            if (active_line > 2) {
                display_line = 2;
            } else {
                display_line = active_line;
            }

            int endloop = 8;
            if (nlines - active_line - start_offset < 8) {
                endloop = nlines - active_line - start_offset;
            }
            if (old_active_line != active_line) {
                for (int i = 0; i < endloop; i++) {
                    String lyrics = chordedLyrics.get(active_line - start_offset + i).getChords() + "\n";
                    lyrics += chordedLyrics.get(active_line - start_offset + i).getLyrics();
                    publishProgress(i, lyrics);
                }
                old_active_line = active_line;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void Unsynced_lyrics(Tabsfile tabsfile) {
        Log.d(TAG, "Unsynced_lyrics() called with: tabsfile = [" + tabsfile + "]");
        StringBuilder Lyrics = new StringBuilder();
        for (Chorded_lyrics line : tabsfile.getChorded_lyrics()) {
            Lyrics.append(line.getChords()).append("\n");
            Lyrics.append(line.getLyrics()).append("\n\n");
        }
        publishProgress(0, Lyrics.toString());
    }

    @Override
    protected Void doInBackground(Object... objects) {
        Log.d(TAG, "doInBackground() called with: objects = [" + Arrays.toString(objects) + "]");
        Tabsfile tabsfile = (Tabsfile) objects[0];
        CurrentSong currentSong = (CurrentSong) objects[1];
        if (tabsfile.isHas_tabs() && tabsfile.isHas_azlyrics()) {
            if (tabsfile.isSynced()) {
                Synced_Lyrics(tabsfile, currentSong);
            } else {
                Unsynced_lyrics(tabsfile);
            }
        } else {
            publishProgress(0, "no file found on server");
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        Log.d(TAG, "onProgressUpdate() called with: values = [" + values + "]");
        super.onProgressUpdate(values);
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        int index = (int) values[0];
        String lyrics = (String) values[1];
        new TextViewUpdater(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, views[index], lyrics);
    }
}
