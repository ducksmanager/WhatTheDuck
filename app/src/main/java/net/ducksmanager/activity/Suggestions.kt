package net.ducksmanager.activity

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_suggested_issue.view.*
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.coa.InducksPerson
import net.ducksmanager.persistence.models.coa.InducksPublication
import net.ducksmanager.persistence.models.coa.InducksStory
import net.ducksmanager.persistence.models.composite.SuggestedIssueSimple
import net.ducksmanager.persistence.models.composite.SuggestionList
import net.ducksmanager.util.AppCompatActivityWithDrawer
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.databinding.SuggestionsBinding
import retrofit2.Response


class Suggestions : AppCompatActivityWithDrawer() {
    private lateinit var binding: SuggestionsBinding

    companion object {
        lateinit var publicationTitles: List<InducksPublication>
        lateinit var authorNames: List<InducksPerson>
        lateinit var storyDetails: List<InducksStory>

        public fun loadSuggestions(suggestionList: SuggestionList) {
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
            WhatTheDuck.appDB!!.inducksStoryDao().insertSet(storyDetails)

            WhatTheDuck.appDB!!.suggestedIssueDao().deleteAll()
            WhatTheDuck.appDB!!.suggestedIssueDao().insertList(suggestions.map {
                val stories = mutableSetOf<String>()
                it.stories.values.forEach { storycode -> stories.addAll(storycode) }
                SuggestedIssueSimple(it.publicationcode, it.issuenumber, it.score, it.oldestdate, stories)
            })

            WhatTheDuck.appDB!!.inducksPublicationDao().insertList(suggestionList.publicationTitles.map { (key, it) ->
                InducksPublication(key, it)
            })

            WhatTheDuck.appDB!!.inducksPersonDao().insertList(suggestionList.authors.map { (key, it) ->
                InducksPerson(key, it)
            })
        }
    }

    override fun shouldShowToolbar() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SuggestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        showToolbarIfExists()

        val suggestionListView = binding.suggestionList
        val noSuggestionView = binding.suggestionsNoResults

        DmServer.api.suggestedIssues.enqueue(object : DmServer.Callback<SuggestionList>("getSuggestedIssues", this) {
            override fun onSuccessfulResponse(response: Response<SuggestionList>) {
                loadSuggestions(response.body()!!)

                val suggestions = WhatTheDuck.appDB!!.suggestedIssueDao().findAll()
                publicationTitles = WhatTheDuck.appDB!!.inducksPublicationDao().findAll()
                authorNames = WhatTheDuck.appDB!!.inducksPersonDao().findAll()
                storyDetails = WhatTheDuck.appDB!!.inducksStoryDao().findAll()

                val showSuggestions = suggestions.isNotEmpty() && publicationTitles.isNotEmpty() && authorNames.isNotEmpty() && storyDetails.isNotEmpty()

                suggestionListView.visibility = if (showSuggestions) View.VISIBLE else View.GONE
                noSuggestionView.visibility = if (!showSuggestions) View.VISIBLE else View.GONE

                suggestionListView.adapter = SuggestedIssueAdapter(this@Suggestions, suggestions.toMutableList())
                (suggestionListView.adapter as SuggestedIssueAdapter).orderByPublicationDate()
                suggestionListView.layoutManager = LinearLayoutManager(this@Suggestions)
            }
        })

        binding.sort.setOnClickListener {
            val adapter = suggestionListView.adapter as SuggestedIssueAdapter
            if ((it as ToggleButton).isChecked) {
                adapter.orderByScore()
            }
            else {
                adapter.orderByPublicationDate()
            }
        }
    }

    class SuggestedIssueAdapter internal constructor(
        private val context: Context,
        var suggestions: MutableList<SuggestedIssueSimple>
    ) : RecyclerView.Adapter<SuggestedIssueAdapter.ViewHolder>() {

        private val inflater: LayoutInflater = LayoutInflater.from(context)

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textWrapperView: LinearLayout = itemView.findViewById(R.id.textwrapper)
            val prefixImageView: ImageView = itemView.findViewById(R.id.prefiximage)
            val suffixtextView1: TextView = itemView.findViewById(R.id.suffixtext1)
            val suffixtextView2: TextView = itemView.findViewById(R.id.scorevalue)
            val storyListView: RecyclerView = itemView.findViewById(R.id.storylist)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(inflater.inflate(R.layout.row_suggested_issue, parent, false))
        }

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
            for (storycode in issueStories) {
                storiesWithAuthors[storycode] = storyDetails.find { inducksStory -> inducksStory.storycode == storycode }?.personcodes!!.toSet()
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

        fun orderByPublicationDate() {
            suggestions.sortByDescending { it.oldestdate }
            notifyDataSetChanged()
        }

        fun orderByScore() {
            suggestions.sortByDescending { it.suggestionScore }
            notifyDataSetChanged()
        }
    }

    class StoryAdapter internal constructor(
        private val context: Context,
        private val stories: HashMap<String, Set<String>>
    ) : RecyclerView.Adapter<StoryAdapter.ViewHolder>() {

        private val inflater: LayoutInflater = LayoutInflater.from(context)

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val authorListView: RecyclerView = itemView.findViewById(R.id.authorlist)
            val storyTitleView: TextView = itemView.findViewById(R.id.storyTitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(inflater.inflate(R.layout.row_story, parent, false))
        }

        override fun getItemCount() = stories.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val currentStory = stories.values.toList()[position]
            val storyCode = stories.keys.toList()[position]
            val story = storyDetails.find { inducksStory -> inducksStory.storycode == storyCode }
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

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val authorBadge: Button = itemView.findViewById(R.id.authorBadge)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(inflater.inflate(R.layout.row_author, parent, false))
        }

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
