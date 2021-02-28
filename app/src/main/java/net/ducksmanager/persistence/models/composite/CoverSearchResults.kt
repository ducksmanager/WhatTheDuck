package net.ducksmanager.persistence.models.composite

import com.google.gson.annotations.Expose
import java.util.*

class CoverSearchResults {
    @Expose
    lateinit var issues: HashMap<String, CoverSearchIssue>
}