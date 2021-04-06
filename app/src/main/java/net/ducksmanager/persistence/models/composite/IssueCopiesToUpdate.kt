package net.ducksmanager.persistence.models.composite

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class IssueCopiesToUpdate(

    @Expose
    private val publicationCode: String,

    @Expose
    private val issueNumbers: Set<String>,

    @Expose
    @SerializedName("condition")
    val conditions: MutableList<String?>,

    @Expose
    @SerializedName("purchaseId")
    val purchaseIds: MutableList<Int?>,
) {
}