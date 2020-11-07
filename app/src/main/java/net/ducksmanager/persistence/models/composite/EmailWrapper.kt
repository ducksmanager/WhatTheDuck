package net.ducksmanager.persistence.models.composite

import com.google.gson.annotations.Expose

class EmailWrapper(
    @Expose
    private val email: String
)