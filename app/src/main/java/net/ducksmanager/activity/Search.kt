package net.ducksmanager.activity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.author_notations.*
import kotlinx.android.synthetic.main.release_notes.view.*
import kotlinx.android.synthetic.main.row_suggested_issue.view.*
import kotlinx.android.synthetic.main.score.view.*
import net.ducksmanager.api.DmServer
import net.ducksmanager.api.DmServer.Companion.api
import net.ducksmanager.persistence.models.composite.*
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserData.Companion.issueConditionToStringId
import net.ducksmanager.util.AppCompatActivityWithDrawer
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.applicationContext
import net.ducksmanager.whattheduck.databinding.SearchBinding
import retrofit2.Response
import java.util.*

class Search : AppCompatActivityWithDrawer() {
    private lateinit var binding: SearchBinding

    companion object {
        fun populateConditionBadge(conditionBadge: ImageView, condition: String?) {
            if (condition != null) {
                val conditionResourceId = InducksIssueWithUserData.issueConditionToResourceId(condition)
                if (conditionResourceId != null) {
                    conditionBadge.setImageResource(conditionResourceId)
                    conditionBadge.visibility = View.VISIBLE
                    conditionBadge.setOnClickListener {
                        Toast.makeText(
                            applicationContext,
                            issueConditionToStringId(condition),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                conditionBadge.visibility = View.INVISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toggleToolbar()

        binding.searchField.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                arrayOf<String>()
            )
        )
        binding.issuelist.adapter = IssueAdapter(this@Search, emptyList(), emptyMap())
        binding.issuelist.layoutManager = LinearLayoutManager(this@Search)

        binding.searchField.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            binding.searchResultsStoryDetailsIntro.visibility = View.VISIBLE
            val story = binding.searchField.adapter.getItem(position) as SimpleStoryWithIssues
            if (story.issues != null) {
                binding.searchResultsStoryTitle.text = story.title
                val publicationNames = appDB!!.inducksPublicationDao().findByPublicationCodes(story.issues!!.map { it.publicationcode }.toSet())
                val issueAdapter = IssueAdapter(
                    this@Search,
                    story.issues!!,
                    publicationNames.map { it.publicationCode to it.title }.toMap()
                )
                binding.issuelist.adapter = issueAdapter
                issueAdapter.notifyItemRangeChanged(0, story.issues!!.size)
                binding.searchField.text.clear()
                hideKeyboard(binding.searchField)
                binding.searchField.clearFocus()
            }
        }
        binding.searchField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length <= 3) {
                    return
                }
                api.searchStory(StorySearchInput(s.toString())).enqueue(object :
                    DmServer.Callback<StorySearchResults>("searchStory", this@Search, true) {
                    override fun onSuccessfulResponse(response: Response<StorySearchResults>) {
                        val results = response.body()!!.results.toMutableSet()
                        if (response.body()!!.hasmore) {
                            results.add(SimpleStoryWithIssues())
                        }
                        appDB!!.issueDao().findByIssueCodes(results.flatMap { story -> story.issues?.map { "${it.publicationcode}-${it.issuenumber}"} ?: listOf() }.toSet()).observe(this@Search,
                            { issues ->
                                val ownedIssuesConditions = issues.map { "${it.country}/${it.magazine}-${it.issueNumber}" to it.condition }.toMap()
                                results.forEach { story ->
                                    story.issues?.forEach { issue ->
                                        issue.condition = ownedIssuesConditions["${issue.publicationcode}-${issue.issuenumber}"]
                                        if (issue.condition != null) {
                                            story.condition = issue.condition
                                        }
                                    }
                                }
                                val storyAdapter = StoryAdapter(
                                    this@Search,
                                    R.layout.story_with_condition,
                                    results.toMutableList()
                                )
                                binding.searchField.setAdapter(storyAdapter)
                                storyAdapter.notifyDataSetChanged()
                            })
                    }
                })
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun hideKeyboard(view: View) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(view.windowToken, 0)
    }

    class StoryAdapter(
        context: Context,
        private val resource: Int,
        private val items: MutableList<SimpleStoryWithIssues>
    ) :
        ArrayAdapter<SimpleStoryWithIssues>(context, resource, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var v = convertView
            if (v == null) {
                val vi = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                v = vi.inflate(resource, null)
            }
            val storyWithIssues: SimpleStoryWithIssues = items[position]
            val storyTitle = v!!.findViewById<View>(R.id.storyTitle) as TextView
            if (storyWithIssues.title == null) {
                storyTitle.text = context.getString(R.string.search_more_results)
            } else {
                storyTitle.text = storyWithIssues.title
                populateConditionBadge(v.findViewById<View>(R.id.conditionbadge) as ImageView, storyWithIssues.condition)
            }
            return v
        }
    }

    class IssueAdapter internal constructor(
        private val context: Context,
        private val issues: List<SimpleStoryIssue>,
        private val publicationNames: Map<String, String>
    ) : RecyclerView.Adapter<IssueAdapter.ViewHolder>() {

        private val inflater: LayoutInflater = LayoutInflater.from(context)

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val conditionbadge: ImageView = itemView.findViewById(R.id.conditionbadge)
            val countrybadge: ImageView = itemView.findViewById(R.id.countrybadge)
            val issueTitle: TextView = itemView.findViewById(R.id.issuetitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(inflater.inflate(R.layout.issue, parent, false))

        override fun getItemCount() = issues.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val currentItem = issues[position]
            val country = currentItem.publicationcode.split('/')[0]
            val countryUri = "@drawable/flags_${country}"
            var countryImageResource = context.resources.getIdentifier(countryUri, null, context.packageName)
            if (countryImageResource == 0) {
                countryImageResource = R.drawable.flags_unknown
            }
            holder.countrybadge.setImageResource(countryImageResource)

            populateConditionBadge(holder.conditionbadge, currentItem.condition)

            holder.issueTitle.text = context.resources.getString(
                R.string.title_template,
                publicationNames[currentItem.publicationcode],
                currentItem.issuenumber
            )
            holder.issueTitle.textSize = 14f
            holder.issueTitle.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        }
    }
}
