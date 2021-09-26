package net.ducksmanager.persistence.models.composite

import com.google.gson.annotations.Expose

class SimpleStoryIssue(
    @Expose
    var publicationcode: String,

    @Expose
    var issuenumber: String,

    var condition: String? = null
)