package net.ducksmanager.persistence.models.composite

import androidx.room.ColumnInfo

class IssuesPerCondition(
    @ColumnInfo(name = "condition") val condition: String,
    @ColumnInfo(name = "count") val count: Integer

)