package net.ducksmanager.persistence.models.composite

import com.google.gson.annotations.Expose

class StorySearchResults(
    @Expose
    var results: List<SimpleStoryWithIssues>,

    @Expose
    var hasmore: Boolean
)