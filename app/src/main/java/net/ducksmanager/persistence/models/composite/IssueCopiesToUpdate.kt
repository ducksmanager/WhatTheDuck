package net.ducksmanager.persistence.models.composite

import com.google.gson.annotations.Expose

class IssueCopiesToUpdate(

        @Expose
        private val publicationCode: String,

        @Expose
        private val issueNumber: String,

        @Expose
        val conditions: MutableMap<Int, String?>,

        @Expose
        val purchaseIds: MutableMap<Int, Int?>,
)