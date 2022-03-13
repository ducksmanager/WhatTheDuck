package net.ducksmanager.persistence.models.coa

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "inducks_issue", primaryKeys = ["inducksPublicationCode", "inducksIssueNumber"])
class InducksIssueWithCoverUrl @JvmOverloads constructor(
    @ColumnInfo
    @Expose
    @SerializedName("publicationcode")
    val inducksPublicationCode: String,

    @ColumnInfo
    @Expose
    @SerializedName("issuenumber")
    val inducksIssueNumber: String,

    @ColumnInfo
    @Expose
    val title: String,

    @ColumnInfo
    @Expose
    @SerializedName("cover_url")
    val coverUrl: String?,

    @Expose
    @androidx.room.Ignore
    @SerializedName("oldestdate")
    val oldestDate: String? = null
)