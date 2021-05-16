package net.ducksmanager.persistence.models.composite

import androidx.room.ColumnInfo

class IssueCountPerMonthAndPublication(
    @ColumnInfo(name = "publicationcode") val publicationcode: String,
    @ColumnInfo(name = "month") val month: String,
    @ColumnInfo(name = "count") val count: Int

)