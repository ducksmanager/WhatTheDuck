package net.ducksmanager.whattheduck

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.composite.SuggestedIssueSimple
import net.ducksmanager.persistence.models.composite.SuggestionList
import net.ducksmanager.util.AppCompatActivityWithDrawer
import retrofit2.Response


class Suggestions : AppCompatActivityWithDrawer() {

    companion object {
        var publicationTitles: HashMap<String, String> = HashMap()
        var authorNames: HashMap<String, String> = HashMap()
        var storyDetails: java.util.HashMap<String, SuggestionList.SuggestedStory> = HashMap()
    }

    override fun shouldShowToolbar() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.suggestions)
        showToolbarIfExists()

        val suggestionListView = findViewById<RecyclerView>(R.id.suggestionList)

        publicationTitles = HashMap()

        DmServer.api.suggestedIssues.enqueue(object : DmServer.Callback<SuggestionList>("getSuggestedIssues", this) {
            override fun onSuccessfulResponse(response: Response<SuggestionList>) {
                val suggestions = response.body()!!.issues.values.toList() as MutableList<SuggestionList.SuggestedIssue>

                WhatTheDuck.appDB.suggestedIssueDao().deleteAll()
                WhatTheDuck.appDB.suggestedIssueDao().insertList(suggestions.map {
                    SuggestedIssueSimple(it.publicationcode, it.issuenumber, it.score)
                })

                publicationTitles = response.body()!!.publicationTitles
                authorNames = response.body()!!.authors
                storyDetails = response.body()!!.storyDetails

                suggestionListView.adapter = SuggestedIssueAdapter(this@Suggestions, suggestions)
                (suggestionListView.adapter as SuggestedIssueAdapter).orderByPublicationDate()
                suggestionListView.layoutManager = LinearLayoutManager(this@Suggestions)
            }
        })

        findViewById<ToggleButton>(R.id.sort).setOnClickListener {
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
        var suggestions: MutableList<SuggestionList.SuggestedIssue>
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
            val currentItem = suggestions[position]
            val countryCode = currentItem.publicationcode.split("/")[0]
            val publicationName = publicationTitles.get(currentItem.publicationcode)

            val title = holder.textWrapperView.findViewById<TextView>(R.id.itemtitle)
            title.text = (publicationName ?: "Unknown publication") + " " + currentItem.issuenumber
            title.typeface = Typeface.DEFAULT_BOLD
            holder.prefixImageView.setImageResource(getImageResourceFromCountry(countryCode))
            holder.suffixtextView1.text = currentItem.oldestdate ?: "Unknown"

            holder.suffixtextView2.text = currentItem.score.toString()

            val allStories = currentItem.stories.values.flatten().toSet()
            val storiesWithAuthors: HashMap<String, List<String>> = HashMap()
            for (story in allStories) {
                storiesWithAuthors[story] = currentItem.stories.keys.filter { author ->
                    currentItem.stories[author]!!.contains(story)
                }
            }
            holder.storyListView.adapter = StoryAdapter(context, storiesWithAuthors, storyDetails)
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
            suggestions.sortByDescending { it.score }
            notifyDataSetChanged()
        }
    }

    class StoryAdapter internal constructor(
        private val context: Context,
        private val stories: HashMap<String, List<String>>,
        private val storyDetails: HashMap<String, SuggestionList.SuggestedStory>
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
            val currentItem = stories.values.toList()[position]
            val storyCode = stories.keys.toList()[position]
            holder.storyTitleView.text = storyDetails[storyCode]?.title ?: "Unknown title"

            holder.authorListView.adapter = AuthorAdapter(context, currentItem)
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
                Toast.makeText(context, authorNames.get((it as Button).text), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
