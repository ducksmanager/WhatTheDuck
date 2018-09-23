package net.ducksmanager.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.ducksmanager.whattheduck.R;

import java.lang.ref.WeakReference;

public class ReleaseNotes {

    public static ReleaseNotes current;
    static {
        current = new ReleaseNotes("1.8", R.string.newFeatures18Text, R.drawable.bookcase_view_switch);
    }

    private String majorVersion;
    private Integer messageId;
    private Integer imageId;

    private ReleaseNotes(String majorVersion, Integer messageId, Integer imageId) {
        this.majorVersion = majorVersion;
        this.messageId = messageId;
        this.imageId = imageId;
    }

    public void showOnVersionUpdate(WeakReference<Activity> originActivityRef) {
        Activity originActivity = originActivityRef.get();
        if (shouldShowMessage()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(originActivity);
            LayoutInflater factory = LayoutInflater.from(originActivity);
            final View view = factory.inflate(R.layout.release_notes, null);
            builder.setView(view);

            builder.setTitle(originActivity.getString(R.string.newFeature));

            builder.setNeutralButton(R.string.ok, (dialogInterface, i) -> {
                addToMessagesAlreadyShown();
                dialogInterface.dismiss();
            });

            ((TextView)view.findViewById(R.id.text)).setText(messageId);

            if (imageId != null) {
                ((ImageView) view.findViewById(R.id.image)).setImageResource(imageId);
            }
            else {
                view.findViewById(R.id.image).setVisibility(View.GONE);
            }

            builder.show();
        }
    }

    private boolean shouldShowMessage() {
        return Settings.shouldShowMessage(getMessageId());
    }

    private void addToMessagesAlreadyShown() {
        Settings.addToMessagesAlreadyShown(getMessageId());
    }

    public String getMessageId() {
        return "release_notes_" + majorVersion;
    }
}
