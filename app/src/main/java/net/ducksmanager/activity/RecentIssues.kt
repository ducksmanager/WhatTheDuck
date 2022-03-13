package net.ducksmanager.activity

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.coa.InducksCountryName
import net.ducksmanager.persistence.models.coa.InducksIssueWithCoverUrl
import net.ducksmanager.persistence.models.coa.InducksPublication
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserData
import net.ducksmanager.persistence.models.dm.Issue
import net.ducksmanager.util.AppCompatActivityWithDrawer
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.databinding.RecentIssuesBinding
import retrofit2.Response


class RecentIssues : AppCompatActivityWithDrawer() {
    private lateinit var binding: RecentIssuesBinding

    companion object {
        lateinit var publicationTitles: List<InducksPublication>
        lateinit var countryNames: List<InducksCountryName>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = RecentIssuesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toggleToolbar()

        showRecentIssues()
    }

    private fun showRecentIssues() {
        val recentIssuesListView = binding.recentIssuesList

        DmServer.api.recentIssues.enqueue(object : DmServer.Callback<List<InducksIssueWithCoverUrl>>(
            DmServer.EVENT_GET_USER_NOTIFICATION_COUNTRIES, this, false) {
            override fun onSuccessfulResponse(response: Response<List<InducksIssueWithCoverUrl>>) {
                val recentIssues = response.body()!!
                countryNames = appDB!!.inducksCountryDao().findByCountryCodes(recentIssues.map { recentIssue -> recentIssue.inducksPublicationCode.split("/")[0] }.toSet())
                publicationTitles = appDB!!.inducksPublicationDao().findByPublicationCodes(recentIssues.map { recentIssue -> recentIssue.inducksPublicationCode }.toSet())

                val recentIssueCodes = recentIssues.map { it.inducksPublicationCode + "-" + it.inducksIssueNumber }.toSet()

                appDB!!.issueDao().findByIssueCodes(recentIssueCodes).observe(this@RecentIssues) { userIssues ->
                    val recentIssuesWithUserIssues = recentIssues.map { recentIssue ->
                        InducksIssueWithUserData(recentIssue, userIssues?.find { userIssue ->
                            "${recentIssue.inducksPublicationCode}-${recentIssue.inducksIssueNumber}" == "${userIssue.country}/${userIssue.magazine}-${userIssue.issueNumber}"
                        })
                    }
                    recentIssuesListView.adapter = RecentIssueAdapter(this@RecentIssues, recentIssuesWithUserIssues)
                    recentIssuesListView.layoutManager = LinearLayoutManager(this@RecentIssues)
                }
            }

            override fun onFailureFailover() {
                println("Failure")
            }
        })
    }

    class RecentIssueAdapter internal constructor(
        private val context: Context,
        private var recentIssues: List<InducksIssueWithUserData>
    ) : RecyclerView.Adapter<RecentIssueAdapter.ViewHolder>() {

        private val inflater: LayoutInflater = LayoutInflater.from(context)

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textWrapperView: LinearLayout = itemView.findViewById(R.id.textwrapper)
            val conditionImageView: ImageView = itemView.findViewById(R.id.conditionimage)
            val flagImageView: ImageView = itemView.findViewById(R.id.flagimage)
            val issueDateView: TextView = itemView.findViewById(R.id.issuedatetext)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(inflater.inflate(R.layout.row_recent_issue, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val currentIssue = recentIssues[position]
            val countryCode = currentIssue.issue.inducksPublicationCode.split("/")[0]
            val publication = publicationTitles.find {
                it.publicationCode == currentIssue.issue.inducksPublicationCode
            }
            val publicationName = publication?.title ?: context.getString(R.string.unknown_publication)

            val title = holder.textWrapperView.findViewById<TextView>(R.id.itemtitle)
            title.text = context.getString(
                R.string.title_template,
                publicationName,
                currentIssue.issue.inducksIssueNumber
            )
            title.typeface = Typeface.DEFAULT_BOLD

            holder.itemView.setOnClickListener {
                WhatTheDuck.selectedCountry = countryNames.find { it.countryCode == countryCode }
                WhatTheDuck.selectedPublication = publication
                WhatTheDuck.itemToScrollTo = currentIssue.issue.inducksIssueNumber
                ItemList.type = WhatTheDuck.CollectionType.COA.toString()
                context.startActivity(Intent(context, IssueList::class.java))
            }

            holder.flagImageView.setImageResource(getImageResourceFromCountry(countryCode))
            holder.conditionImageView.setImageResource(getImageResourceFromCondition(currentIssue.userIssue))

            holder.issueDateView.text = currentIssue.issue.oldestDate ?: "Unknown"
        }

        private fun getImageResourceFromCondition(userIssue: Issue?): Int {
            return if (userIssue != null) {
                InducksIssueWithUserData.issueConditionToResourceId(userIssue.condition) ?: android.R.color.transparent
            } else {
                android.R.color.transparent
            }
        }

        private fun getImageResourceFromCountry(countryCode: String): Int {
            val uri = "@drawable/flags_$countryCode"
            var imageResource = context.resources.getIdentifier(uri, null, context.packageName)

            if (imageResource == 0) {
                imageResource = R.drawable.flags_unknown
            }
            return imageResource
        }

        override fun getItemCount() = recentIssues.size
    }
}
