package net.ducksmanager.persistence.models.coa

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "inducks_issuequotation", primaryKeys = ["publicationCode", "issueNumber"])
class InducksIssueQuotation constructor(
    @ColumnInfo
    @Expose
    @SerializedName("publicationcode")
    val publicationCode: String,

    @ColumnInfo
    @Expose
    @SerializedName("issuenumber")
    val issueNumber: String,

    @ColumnInfo
    @Expose
    @SerializedName("estimationmin")
    val estimationMin: Float?,

    @ColumnInfo
    @Expose
    @SerializedName("estimationmax")
    val estimationMax: Float?,
)