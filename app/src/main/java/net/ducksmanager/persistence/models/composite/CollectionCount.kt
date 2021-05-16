package net.ducksmanager.persistence.models.composite

import androidx.room.ColumnInfo

class CollectionCount(
    @ColumnInfo(name = "countries") val countries: Integer,
    @ColumnInfo(name = "publications") val publications: Integer,
    @ColumnInfo(name = "issues") val issues: Integer

)