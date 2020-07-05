package net.ducksmanager.persistence.models.coa

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inducks_person")
class InducksPerson(
    @PrimaryKey
    val personcode: String,

    @ColumnInfo
    val fullname: String
)