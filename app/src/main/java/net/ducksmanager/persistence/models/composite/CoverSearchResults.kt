package net.ducksmanager.persistence.models.composite

import com.google.gson.annotations.Expose
import java.util.*

class CoverSearchResults {
    @Expose
    val issues: HashMap<String, CoverSearchIssue>? = null

    @Expose
    private val imageIds: List<Int>? = null

    @Expose
    var type: String? = null
}