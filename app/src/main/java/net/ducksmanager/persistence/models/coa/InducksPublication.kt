package net.ducksmanager.persistence.models.coa

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inducks_publication")
class InducksPublication(
    @PrimaryKey
    val publicationCode: String,

    @ColumnInfo
    val title: String
)