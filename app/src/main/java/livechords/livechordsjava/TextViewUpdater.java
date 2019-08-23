package livechords.livechordsjava;

import android.os.AsyncTask;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class TextViewUpdater extends AsyncTask<Object, String, Void> {
    private WeakReference<MainActivity> activityWeakReference;
    private TextView textView;
    private ServerConnection serverConnection = new ServerConnection();
    private static final String TAG = "MYDEBUG_TextV_updater";

    public TextViewUpdater(MainActivity activity) {
        activityWeakReference = new WeakReference<MainActivity>(activity);
    }


    @Override
    protected Void doInBackground(Object... Objects) {
        Log.d(TAG, "doInBackground() called with: Objects = [" + Objects + "......]");
        int id = (int) Objects[0];
        String text = (String) Objects[1];
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()){
            return null;
        }
        //check if the textview is visible or if it is not null, than progressupdate the text.
        boolean not_found = true;
        while (not_found) {
            //Log.d(TAG, "doInBackground: while loop running");
            textView = activity.findViewById(id);
            //Log.d(TAG, "doInBackground: textview searched for");
            if (textView == null){
                //Log.d(TAG, "doInBackground: textView is null");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                not_found = false;
            }
        }
        publishProgress(text);
        Log.d(TAG, "doInBackground: Finished, Updated textview " + id + " to " + text);
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        Log.d(TAG, "onProgressUpdate() called with: values = [" + values + "....]");
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

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
