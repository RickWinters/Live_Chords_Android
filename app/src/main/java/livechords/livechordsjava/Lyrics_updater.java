package livechords.livechordsjava;

import android.os.AsyncTask;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class Lyrics_updater extends AsyncTask<String, String, Void> {
    private WeakReference<MainActivity> activityWeakReference;
    private TextView textView;
    private Connection connection = new Connection();
    private static final String TAG = "MYDEBUG_Lyrics_updater";

    Lyrics_updater(MainActivity activity){
        activityWeakReference = new WeakReference<MainActivity>(activity);
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "onPreExecute() called");
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(String... strings) {
        Log.d(TAG, "doInBackground() called with: strings = [" + strings + "]");
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()){
            return null;
        }
        //check if the textview is visible or if it is not null, than progressupdate the text.
        Boolean not_found = true;
        while (not_found) {
            Log.d(TAG, "doInBackground() while loop running");
            textView = activity.findViewById(R.id.fragment_lyrics_text);
            Log.d(TAG, "doInBackground: textview searched for");
            if (textView == null){
                Log.d(TAG, "doInBackground: textView is null");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                not_found = false;
            }
        }
        publishProgress(strings[0]);
        Log.d(TAG, "doInBackground: Finished");
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        Log.d(TAG, "onProgressUpdate() called with: values = [" + values + "]");
        super.onProgressUpdate(values);
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()){
            return;
        }
        textView.setText(values[0]);
        textView.setMovementMethod(new ScrollingMovementMethod());
        //executed on Uithread
        //put text in textfield.
    }

}
