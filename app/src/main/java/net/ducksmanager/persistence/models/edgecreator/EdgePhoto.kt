package net.ducksmanager.persistence.models.edgecreator

import com.google.gson.annotations.Expose
import org.jetbrains.annotations.NotNull

class EdgePhoto(
    @Expose
    @NotNull
    var country: String,

    @Expose
    @NotNull
    var magazine: String,

    @Expose
    @NotNull
    var issuenumber: String,

    @Expose
    @NotNull
    var data: String
)