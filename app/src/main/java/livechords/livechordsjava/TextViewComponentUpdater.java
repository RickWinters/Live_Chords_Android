package livechords.livechordsjava;

import android.os.AsyncTask;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Arrays;

public class TextViewComponentUpdater extends AsyncTask<Object, Object, Void> {
    private static final String TAG = "MYDEBUG_TextVC_updater";
    public static final int COMMAND_TEXT = 1;
    public static final int COMMAND_TEXTSIZE = 2;
    public static final int COMMAND_COLOR = 3;
    public static final int COMMAND_SCROLLABLE = 4;
    private int textViewID;
    private int command;
    private Object parameter;

    private WeakReference<MainActivity> activityWeakReference;
    private TextView textView;
    private ServerConnection serverConnection = new ServerConnection();

    public TextViewComponentUpdater(MainActivity activity) {
        activityWeakReference = new WeakReference<MainActivity>(activity);
    }

    @Override
    protected Void doInBackground(Object... objects) {
        textViewID = (int) objects[0];
        command = (int) objects[1];
        parameter = objects[2]; //will be cast later to the correct object type
        Log.d(TAG, "doInBackground() called with: textViewComponentUpdaterCommands = [ id = " + textViewID + " command = " + command  + " parameter = " + parameter + "]");
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()){
            return null;
        }
        while(true){
            textView = activity.findViewById(textViewID);
            if (textView != null) {
                break;
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        publishProgress(objects);
        return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        Log.d(TAG, "onProgressUpdate() called with: values = [" + Arrays.toString(values) + "]");
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()){
            return;
        }
        if (command == COMMAND_COLOR){
            textView.setTextColor((int) parameter);
        } else if (command == COMMAND_TEXT){
            textView.setText((String) parameter);
        } else if (command == COMMAND_TEXTSIZE){
            textView.setTextSize((Float) parameter);
        } else if (command == COMMAND_SCROLLABLE){
            if ((boolean) parameter) {
                textView.setMovementMethod(new ScrollingMovementMethod());
            } else {
                textView.setMovementMethod(null);
            }
        }
    }

}
