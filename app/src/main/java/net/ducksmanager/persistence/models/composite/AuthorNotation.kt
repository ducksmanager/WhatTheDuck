package net.ducksmanager.persistence.models.composite

import com.google.gson.annotations.Expose

class AuthorNotation(
        @Expose
        val personCode: String,
        @Expose
        var notation: Int
)