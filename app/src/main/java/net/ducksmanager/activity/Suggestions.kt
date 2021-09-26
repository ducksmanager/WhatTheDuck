package net.ducksmanager.activity

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_suggested_issue.view.*
import net.ducksmanager.persistence.dao.SuggestedIssueDao
import net.ducksmanager.persistence.models.coa.InducksPerson
import net.ducksmanager.persistence.models.coa.InducksPublication
import net.ducksmanager.persistence.models.coa.InducksStory
import net.ducksmanager.persistence.models.composite.SuggestedIssueByReleaseDateSimple
import net.ducksmanager.persistence.models.composite.SuggestedIssueSimple
import net.ducksmanager.persistence.models.composite.SuggestionList
import net.ducksmanager.util.AppCompatActivityWithDrawer
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.config
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.isOfflineMode
import net.ducksmanager.whattheduck.databinding.SuggestionsBinding


class Suggestions : AppCompatActivityWithDrawer() {
    private lateinit var binding: SuggestionsBinding

    companion object {
        lateinit var publicationTitles: List<InducksPublication>
        lateinit var authorNames: List<InducksPerson>
        lateinit var storyDetails: List<InducksStory>

        fun loadSuggestions(suggestionList: SuggestionList, targetDaoClass: Class<*>) {
            val suggestions = suggestionList.issues.values.toMutableList()

            val storyDetails: Set<InducksStory> = suggestionList.storyDetails.map { (key, it) ->
                InducksStory(key, it.title, mutableSetOf(), it.storycomment)
            }.toMutableSet()

            suggestions.forEach { suggestion ->
                suggestion.stories.forEach { (personcode, storycodes) ->
                    storycodes.forEach { storycode ->
                        storyDetails.find { inducksStory -> storycode == inducksStory.storycode }?.personcodes?.add(personcode)
                    }
                }
            }
            appDB!!.inducksStoryDao().insertSet(storyDetails)

            when (targetDaoClass) {
                SuggestedIssueDao::class.java -> {
                    appDB!!.suggestedIssueDao().deleteAll()
                    appDB!!.suggestedIssueDao().insertList(suggestions.map {
                        val stories = mutableSetOf<String>()
                        it.stories.values.forEach { storycode -> stories.addAll(storycode) }
                        SuggestedIssueSimple(it.publicationcode, it.issuenumber, it.score, it.oldestdate, stories)
                    })
                }
                else -> {
                    appDB!!.suggestedIssueByReleaseDateDao().deleteAll()
                    appDB!!.suggestedIssueByReleaseDateDao().insertList(suggestions.map {
                        val stories = mutableSetOf<String>()
                        it.stories.values.forEach { storycode -> stories.addAll(storycode) }
                        SuggestedIssueByReleaseDateSimple(SuggestedIssueSimple(it.publicationcode, it.issuenumber, it.score, it.oldestdate, stories))
                    })
                }
            }

            appDB!!.inducksPersonDao().insertList(suggestionList.authors.map { (key, it) ->
                InducksPerson(key, it)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SuggestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toggleToolbar()

        if (isOfflineMode) {
            binding.warningMessage.visibility = View.VISIBLE
        }

        showSuggestions()

        binding.sort.setOnClickListener {
            showSuggestions((it as ToggleButton).isChecked)
        }
    }

    private fun showSuggestions(orderByReleaseDate: Boolean = false) {
        val suggestionListView = binding.suggestionList
        val noSuggestionView = binding.suggestionsNoResults

        println("get suggestions")
        val suggestions = if (orderByReleaseDate) {
            appDB!!.suggestedIssueByReleaseDateDao().findAll().map { suggestedIssueByReleaseDate -> suggestedIssueByReleaseDate.suggestedIssue }
        } else {
            appDB!!.suggestedIssueDao().findAll()
        }
        publicationTitles = appDB!!.inducksPublicationDao().findByPublicationCodes(suggestions.map { suggestion -> suggestion.publicationCode }.toSet())
        authorNames = appDB!!.inducksPersonDao().findAll()
        storyDetails = appDB!!.inducksStoryDao().findAll()

        val showSuggestions = suggestions.isNotEmpty() && publicationTitles.isNotEmpty() && authorNames.isNotEmpty() && storyDetails.isNotEmpty()

        noSuggestionView.visibility = if (!showSuggestions) View.VISIBLE else View.GONE

        suggestionListView.visibility = if (showSuggestions) View.VISIBLE else View.GONE
        suggestionListView.layoutManager = LinearLayoutManager(this@Suggestions)
        suggestionListView.adapter = SuggestedIssueAdapter(this@Suggestions, suggestions.toMutableList())
    }

    class SuggestedIssueAdapter internal constructor(
        private val context: Context,
        private var suggestions: MutableList<SuggestedIssueSimple>
    ) : RecyclerView.Adapter<SuggestedIssueAdapter.ViewHolder>() {

        private val inflater: LayoutInflater = LayoutInflater.from(context)

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textWrapperView: LinearLayout = itemView.findViewById(R.id.textwrapper)
            val prefixImageView: ImageView = itemView.findViewById(R.id.prefiximage)
            val suffixtextView1: TextView = itemView.findViewById(R.id.suffixtext1)
            val suffixtextView2: TextView = itemView.findViewById(R.id.scorevalue)
            val storyListView: RecyclerView = itemView.findViewById(R.id.storylist)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(inflater.inflate(R.layout.row_suggested_issue, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val currentIssue = suggestions[position]
            val countryCode = currentIssue.publicationCode.split("/")[0]
            val publicationName = publicationTitles.find {
                inducksPublication -> inducksPublication.publicationCode == currentIssue.publicationCode
            }?.title

            val title = holder.textWrapperView.itemtitle
            title.text = context.getString(
                R.string.title_template,
                publicationName ?: context.getString(R.string.unknown_publication),
                currentIssue.issueNumber
            )
            title.typeface = Typeface.DEFAULT_BOLD
            holder.prefixImageView.setImageResource(getImageResourceFromCountry(countryCode))

            holder.suffixtextView1.text = currentIssue.oldestdate ?: "Unknown"
            holder.suffixtextView2.text = currentIssue.suggestionScore.toString()

            val issueStories = currentIssue.stories
            val storiesWithAuthors: HashMap<String, Set<String>> = HashMap()
            for (storyCode in issueStories) {
                storiesWithAuthors[storyCode] = storyDetails.find { inducksStory -> inducksStory.storycode == storyCode }?.personcodes!!.toSet()
            }
            holder.storyListView.adapter = StoryAdapter(context, storiesWithAuthors)
            holder.storyListView.layoutManager = LinearLayoutManager(context)
        }

        private fun getImageResourceFromCountry(countryCode: String): Int {
            val uri = "@drawable/flags_$countryCode"
            var imageResource = context.resources.getIdentifier(uri, null, context.packageName)

            if (imageResource == 0) {
                imageResource = R.drawable.flags_unknown
            }
            return imageResource
        }

        override fun getItemCount() = suggestions.size

    }

    class StoryAdapter internal constructor(
        private val context: Context,
        private val stories: HashMap<String, Set<String>>
    ) : RecyclerView.Adapter<StoryAdapter.ViewHolder>() {

        private val inflater: LayoutInflater = LayoutInflater.from(context)

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val authorListView: RecyclerView = itemView.findViewById(R.id.authorlist)
            val storyTitleView: TextView = itemView.findViewById(R.id.storyTitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(inflater.inflate(R.layout.story_with_authors, parent, false))

        override fun getItemCount() = stories.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val currentStory = stories.values.toList()[position]
            val storyCode = stories.keys.toList()[position]
            val story = storyDetails.find { inducksStory -> inducksStory.storycode == storyCode }
            if (story?.storycode != null) {
                holder.storyTitleView.setOnClickListener {
                    this@StoryAdapter.context.startActivity(
                        Intent(Intent.ACTION_VIEW).setData(
                            Uri.parse(String.format("%s%s", config.getProperty("inducks_story_url"), story.storycode))
                        )
                    )
                }
            }
            holder.storyTitleView.text = if (story?.title?.isEmpty()!!) {
                context.getString(R.string.no_title)
            } else {
                story.title
            }

            holder.authorListView.adapter = AuthorAdapter(context, currentStory.toList())
            holder.authorListView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    class AuthorAdapter internal constructor(
        private val context: Context,
        private val authors: List<String>
    ) : RecyclerView.Adapter<AuthorAdapter.ViewHolder>() {

        private val inflater: LayoutInflater = LayoutInflater.from(context)

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val authorBadge: Button = itemView.findViewById(R.id.authorBadge)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(inflater.inflate(R.layout.row_author, parent, false))

        override fun getItemCount() = authors.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val currentItem = authors[position]
            holder.authorBadge.text = currentItem
            holder.authorBadge.setOnClickListener {
                Toast.makeText(context, authorNames.find { author -> author.personcode == (it as Button).text }!!.fullname, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
