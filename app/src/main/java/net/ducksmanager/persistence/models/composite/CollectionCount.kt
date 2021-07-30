package net.ducksmanager.persistence.models.composite

import androidx.room.ColumnInfo

class CollectionCount(
    @ColumnInfo(name = "countries") val countries: Int,
    @ColumnInfo(name = "publications") val publications: Int,
    @ColumnInfo(name = "issues") val issues: Int

)