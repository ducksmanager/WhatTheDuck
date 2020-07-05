package net.ducksmanager.persistence.models.coa

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inducks_story")
class InducksStory(
    @PrimaryKey
    val storycode: String,

    @ColumnInfo
    val title: String,

    @ColumnInfo
    val personcodes: MutableSet<String> = mutableSetOf(),

    @ColumnInfo
    val storycomment: String
)