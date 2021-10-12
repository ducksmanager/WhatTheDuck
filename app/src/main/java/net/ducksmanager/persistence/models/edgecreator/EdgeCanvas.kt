package net.ducksmanager.persistence.models.edgecreator

import com.google.gson.annotations.Expose
import org.jetbrains.annotations.NotNull

class EdgeCanvas(
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
    var contributors: HashMap<String, HashMap<String, String>>,

    @Expose
    @NotNull
    var content: String
) {
    @Expose
    @NotNull
    var runExport: Boolean = false

    @Expose
    @NotNull
    var runSubmit: Boolean = false
}