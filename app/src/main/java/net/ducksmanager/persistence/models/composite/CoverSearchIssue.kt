package net.ducksmanager.persistence.models.composite

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.jetbrains.annotations.NotNull

@Entity(tableName = "coversearch_issue")
class CoverSearchIssue(
    @SerializedName(value = "coverid")
    @PrimaryKey
    val coverId: @NotNull String,

    @SerializedName(value = "countrycode")
    @ColumnInfo
    val coverCountryCode: String,

    @SerializedName(value = "publicationcode")
    @ColumnInfo
    val coverPublicationCode: String,

    @SerializedName(value = "publicationtitle")
    @ColumnInfo
    val coverPublicationTitle: String,

    @SerializedName(value = "issuenumber")
    @ColumnInfo
    val coverIssueNumber: String,

    @SerializedName(value = "coverurl")
    @ColumnInfo
    var coverUrl: String
)