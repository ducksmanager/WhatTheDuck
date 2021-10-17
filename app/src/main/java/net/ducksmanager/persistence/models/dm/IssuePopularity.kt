package net.ducksmanager.persistence.models.dm

import com.google.gson.annotations.Expose
import org.jetbrains.annotations.NotNull

class IssuePopularity {
    @Expose
    @NotNull
    lateinit var country: String

    @Expose
    @NotNull
    lateinit var magazine: String

    @Expose
    @NotNull
    lateinit var issueNumber: String

    @Expose
    @NotNull
    var popularity: Int? = null
}