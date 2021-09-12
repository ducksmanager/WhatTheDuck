package net.ducksmanager.persistence.models.composite

import androidx.room.ColumnInfo

abstract class OwnershipAndReferenceCount {

    @ColumnInfo
    var possessedIssues: Int = 0

    @ColumnInfo
    var referencedIssues: Int = 0
}