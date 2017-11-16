package net.ducksmanager.util;

import android.view.ViewGroup;
import android.widget.TextView;

import net.igenius.customcheckbox.CustomCheckBox;

import java.util.ArrayList;
import java.util.List;

public class MultipleCustomCheckboxes {
    private final ViewGroup container;
    private final TextView checkedElementInfoTextView;
    private final Integer initiallyCheckedId;
    private List<CustomCheckBox> checkboxList;

    public MultipleCustomCheckboxes(
        final ViewGroup container,
        Integer initiallyCheckedId,
        final TextView checkedElementInfoTextView) {

        this.container = container;
        this.checkedElementInfoTextView = checkedElementInfoTextView;
        this.initiallyCheckedId = initiallyCheckedId;
        this.checkboxList = new ArrayList<>();

        this.findDescendantsOfType(container, CustomCheckBox.class);
    }

    private void findDescendantsOfType(ViewGroup v, Class type) {
        for (int i = 0; i < v.getChildCount(); i++) {
            if (v.getChildAt(i) instanceof CustomCheckBox) {
                this.checkboxList.add((CustomCheckBox) v.getChildAt(i));
            }
            else if (v.getChildAt(i) instanceof ViewGroup) {
                findDescendantsOfType((ViewGroup) v.getChildAt(i), type);
            }
        }
    }

    public void init() {
        ((CustomCheckBox) container.findViewById(initiallyCheckedId)).setChecked(true);

        for (CustomCheckBox checkBox : checkboxList) {
            checkBox.setTag("");
            checkBox.setOnCheckedChangeListener(new CustomCheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CustomCheckBox checkBox, boolean isChecked) {
                    if (isChecked) {
                        for (CustomCheckBox otherCheckbox : checkboxList) {
                            if (otherCheckbox.getId() != checkBox.getId()) {
                                otherCheckbox.setTag("direct_uncheck=false");
                                otherCheckbox.setChecked(false);
                            }
                        }

                        if (checkedElementInfoTextView != null) {
                            checkedElementInfoTextView.setText(checkBox.getContentDescription());
                        }
                    } else {
                        if (checkBox.getTag().equals("direct_uncheck=false")) {
                            checkBox.setTag("");
                        } else {
                            checkBox.setChecked(true);
                        }
                    }
                }
            });
        }
    }
}
