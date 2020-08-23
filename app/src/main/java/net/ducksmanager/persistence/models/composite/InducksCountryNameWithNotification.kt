package net.ducksmanager.persistence.models.composite

import androidx.room.ColumnInfo
import androidx.room.Embedded
import net.ducksmanager.persistence.models.coa.InducksCountryName

class InducksCountryNameWithNotification {
    @Embedded
    lateinit var country: InducksCountryName

    @ColumnInfo
    var isNotified: Boolean = false
}