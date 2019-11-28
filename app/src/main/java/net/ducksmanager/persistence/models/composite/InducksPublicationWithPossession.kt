package net.ducksmanager.persistence.models.composite

import androidx.room.ColumnInfo
import androidx.room.Embedded
import net.ducksmanager.persistence.models.coa.InducksPublication

class InducksPublicationWithPossession {
    @Embedded
    lateinit var publication: InducksPublication

    @ColumnInfo
    var isPossessed: Boolean = false
}