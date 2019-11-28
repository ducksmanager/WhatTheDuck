package net.ducksmanager.persistence.models.coa

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inducks_countryname")
class InducksCountryName(
    @PrimaryKey
    val countryCode: String,

    @ColumnInfo
    val countryName: String
)