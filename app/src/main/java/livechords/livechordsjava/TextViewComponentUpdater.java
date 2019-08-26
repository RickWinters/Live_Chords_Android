package livechords.livechordsjava;

import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import livechords.livechordsjava.Model.TextViewComponentUpdaterCommand;

public class TextViewComponentUpdater extends AsyncTask<TextViewComponentUpdaterCommand, TextViewComponentUpdaterCommand, Void> {
    private static final String TAG = "MYDEBUG_TextVC_updater";
    public static final int COMMAND_TEXT = 1;
    public static final int COMMAND_TEXTSIZE = 2;
    public static final int COMMAND_COLOR = 3;

    private WeakReference<MainActivity> activityWeakReference;
    private TextView textView;
    private ServerConnection serverConnection = new ServerConnection();

    public TextViewComponentUpdater(MainActivity activity) {
        activityWeakReference = new WeakReference<MainActivity>(activity);
    }

    @Override
    protected Void doInBackground(TextViewComponentUpdaterCommand... textViewComponentUpdaterCommands) {
        Log.d(TAG, "doInBackground() called with: textViewComponentUpdaterCommands = [" + textViewComponentUpdaterCommands + "]");
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()){
            return null;
        }
        for (TextViewComponentUpdaterCommand command : textViewComponentUpdaterCommands) {
            while(true){
                textView = activity.findViewById(command.getId());
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
            publishProgress(command);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(TextViewComponentUpdaterCommand... values) {
        Log.d(TAG, "onProgressUpdate() called with: values = [" + values + "]");
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()){
            return;
        }
        TextViewComponentUpdaterCommand command = values[0];
        if (command.getCommand() == COMMAND_COLOR){
            textView.setTextColor((ColorStateList) command.getParameter());
        } else if (command.getCommand() == COMMAND_TEXT){
            textView.setText((String) command.getParameter());
        } else if (command.getCommand() == COMMAND_TEXTSIZE){
            textView.setTextSize((Float) command.getParameter());
        }
    }
}
