package net.ducksmanager.util

import android.view.View
import android.view.ViewGroup

import net.ducksmanager.whattheduck.R
import net.igenius.customcheckbox.CustomCheckBox

import java.lang.ref.WeakReference
import java.util.HashSet

class MultipleCustomCheckboxes(private val container: WeakReference<View>, private val customOnChecked: View.OnClickListener, private val customOnUnchecked: View.OnClickListener) {

    private val onCheckedListener = CustomCheckBox.OnCheckedChangeListener { checkBox, isChecked ->
        if (checkBox.getTag(R.id.check_by_user) != null) {
            checkBox.setTag(R.id.check_by_user, null)
        } else {
            if (isChecked) {
                this@MultipleCustomCheckboxes.initClickEvents()
                for (otherCheckbox in checkboxList) {
                    if (checkBox != otherCheckbox) {
                        otherCheckbox.setTag(R.id.direct_uncheck, java.lang.Boolean.FALSE)
                        otherCheckbox.isChecked = false
                    }
                }
                customOnChecked.onClick(checkBox)
            } else {
                if (checkBox.getTag(R.id.direct_uncheck) != null && checkBox.getTag(R.id.direct_uncheck) == java.lang.Boolean.FALSE) {
                    checkBox.setTag(R.id.direct_uncheck, null)
                } else {
                    customOnUnchecked.onClick(checkBox)
                    checkBox.isChecked = true
                }
            }
        }
    }

    private val checkboxList = HashSet<CustomCheckBox>()

    interface CheckboxFilter {
        fun isMatched(checkbox: CustomCheckBox): Boolean
    }

    private fun storeDescendantsOfType(v: ViewGroup, type: Class<*>) {
        for (i in 0 until v.childCount) {
            this.storeOrStoreDescendantsOfType(v.getChildAt(i), type)
        }
    }

    private fun storeOrStoreDescendantsOfType(item: Any, type: Class<*>) {
        if (item is CustomCheckBox) {
            this.checkboxList.add(item)
        } else if (item is ViewGroup) {
            storeDescendantsOfType(item, type)
        }
    }

    fun checkInitialCheckbox(filter: CheckboxFilter) {
        var checkboxToCheck: CustomCheckBox? = null
        for (checkbox in checkboxList) {
            if (filter.isMatched(checkbox)) {
                checkboxToCheck = checkbox // Don't call setChecked() here to avoir concurrent access in initClickEvents() called in the OnCheckedChangeListener
            }
        }
        if (checkboxToCheck != null) {
            checkboxToCheck.isChecked = true
        }
    }

    fun initClickEvents() {
        this.storeDescendantsOfType(container.get() as ViewGroup, CustomCheckBox::class.java)

        for (checkBox in checkboxList) {
            checkBox.setTag(R.id.direct_uncheck, null)
            checkBox.setOnCheckedChangeListener(onCheckedListener)
        }

    }
}
