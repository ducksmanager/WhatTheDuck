package net.ducksmanager.persistence.models.composite

import com.google.gson.annotations.Expose

class UserFeedback(
    @Expose
    private val message: String
)