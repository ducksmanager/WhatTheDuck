package net.ducksmanager.persistence.models.composite

import com.google.gson.annotations.Expose

class IssueListToUpdate(
    @Expose
    private val publicationCode: String,

    @Expose
    private val issueNumbers: Set<String>,

    @Expose
    private val condition: String,

    @Expose
    private val purchaseId: Int?
)