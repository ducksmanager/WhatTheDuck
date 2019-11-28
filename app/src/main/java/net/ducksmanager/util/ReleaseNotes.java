package net.ducksmanager.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.ducksmanager.persistence.models.composite.UserMessage;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.WhatTheDuck;

import java.lang.ref.WeakReference;

import static net.ducksmanager.util.Settings.shouldShowMessage;

public class ReleaseNotes {

    public static final ReleaseNotes current;
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
        if (shouldShowMessage(getMessageId())) {
            Activity originActivity = originActivityRef.get();
            AlertDialog.Builder builder = new AlertDialog.Builder(originActivity);
            LayoutInflater factory = LayoutInflater.from(originActivity);
            final View view = factory.inflate(R.layout.release_notes, null);
            builder.setView(view);

            builder.setTitle(originActivity.getString(R.string.newFeature));

            builder.setNeutralButton(R.string.ok, (dialogInterface, i) -> {
                WhatTheDuck.appDB.userMessageDao().insert(new UserMessage(getMessageId(), false));
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

    private String getMessageId() {
        return "release_notes_" + majorVersion;
    }
}
