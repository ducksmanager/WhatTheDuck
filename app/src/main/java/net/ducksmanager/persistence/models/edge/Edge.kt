package net.ducksmanager.persistence.models.edge

import com.google.gson.annotations.Expose
import org.jetbrains.annotations.NotNull

class Edge(
    @Expose
    @NotNull
    var publicationcode: String,

    @Expose
    @NotNull
    var issuenumber: String
)