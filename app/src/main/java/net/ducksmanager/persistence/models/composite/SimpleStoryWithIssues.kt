package net.ducksmanager.persistence.models.composite

import com.google.gson.annotations.Expose

class SimpleStoryWithIssues(
    @Expose
    var code: String? = null,

    @Expose
    var title: String? = null,

    @Expose
    var issues: List<SimpleStoryIssue>? = null,

    var condition: String? = null
)