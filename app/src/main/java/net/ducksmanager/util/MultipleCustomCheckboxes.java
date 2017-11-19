package net.ducksmanager.util;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.ducksmanager.whattheduck.R;
import net.igenius.customcheckbox.CustomCheckBox;

import java.util.ArrayList;
import java.util.List;

public class MultipleCustomCheckboxes {

    public CustomCheckBox.OnCheckedChangeListener onCheckedListener = new CustomCheckBox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CustomCheckBox checkBox, boolean isChecked) {
            MultipleCustomCheckboxes.this.initClickEvents();
            if (isChecked) {
                for (CustomCheckBox otherCheckbox : checkboxList) {
                    if (!checkBox.equals(otherCheckbox)) {
                        otherCheckbox.setTag(R.id.direct_uncheck, Boolean.FALSE);
                        otherCheckbox.setChecked(false);
                    }
                }

                if (checkedElementInfoTextView != null) {
                    checkedElementInfoTextView.setText(checkBox.getContentDescription());
                }
            } else {
                if (checkBox.getTag(R.id.direct_uncheck) != null
                    && checkBox.getTag(R.id.direct_uncheck).equals(Boolean.FALSE)) {
                    checkBox.setTag(R.id.direct_uncheck, null);
                } else {
                    checkBox.setChecked(true);
                }
            }
        }
    };

    public interface CheckboxFilter {
        boolean isMatched(CustomCheckBox checkbox);
    }

    private final TextView checkedElementInfoTextView;
    private View container;
    private int checkboxContainerId;
    private List<CustomCheckBox> checkboxList = new ArrayList<>();

    public MultipleCustomCheckboxes(final TextView checkedElementInfoTextView, View container, int checkboxContainerId) {
        this.checkedElementInfoTextView = checkedElementInfoTextView;
        this.container = container;
        this.checkboxContainerId = checkboxContainerId;
    }

    private void storeDescendantsOfType(ViewGroup v, Class type) {
        for (int i = 0; i < v.getChildCount(); i++) {
            this.storeOrStoreDescendantsOfType(v.getChildAt(i), type);
        }
    }

    private void storeOrStoreDescendantsOfType(Object item, Class type) {
        if (item instanceof CustomCheckBox) {
            this.checkboxList.add((CustomCheckBox) item);
        } else if (item instanceof ViewGroup) {
            storeDescendantsOfType((ViewGroup) item, type);
        }
    }

    public void checkInitialCheckbox(CheckboxFilter filter) {
        for (CustomCheckBox checkbox : checkboxList) {
            if (filter.isMatched(checkbox)) {
                checkbox.setChecked(true);
            }
        }
    }

    public void initClickEvents() {
        if (this.checkboxList.size() == 0) {
            this.storeDescendantsOfType((ViewGroup) container.findViewById(checkboxContainerId), CustomCheckBox.class);

            for (CustomCheckBox checkBox : checkboxList) {
                checkBox.setTag(R.id.direct_uncheck, null);
                checkBox.setOnCheckedChangeListener(onCheckedListener);
            }
        }
    }
}
