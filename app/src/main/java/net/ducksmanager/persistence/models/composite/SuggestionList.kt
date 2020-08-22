package net.ducksmanager.persistence.models.composite

import com.google.gson.annotations.Expose
import java.util.*

class SuggestionList(
    @Expose
    private val minScore: Int,

    @Expose
    private val maxScore: Int,

    @Expose
    val issues: HashMap<String, SuggestedIssue>,

    @Expose
    val authors: HashMap<String, String>,

    @Expose
    val publicationTitles: HashMap<String, String>,

    @Expose
    val storyDetails: HashMap<String, SuggestedStory>
) {

    class SuggestedStory private constructor(
        @Expose
        val title: String,

        @Expose
        private val personcode: String,

        @Expose
        val storycomment: String
    )

    class SuggestedIssue private constructor(
        @Expose
        val stories: HashMap<String, HashSet<String>>,

        @Expose
        val score: Int,

        @Expose
        val publicationcode: String,

        @Expose
        val oldestdate: String?,

        @Expose
        val issuenumber: String
    )
}