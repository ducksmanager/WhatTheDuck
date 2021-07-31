package net.ducksmanager.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.author_notations.*
import kotlinx.android.synthetic.main.release_notes.view.*
import kotlinx.android.synthetic.main.row_suggested_issue.view.*
import kotlinx.android.synthetic.main.score.view.*
import net.ducksmanager.api.DmServer
import net.ducksmanager.api.DmServer.Companion.api
import net.ducksmanager.persistence.models.composite.AuthorNotation
import net.ducksmanager.util.AppCompatActivityWithDrawer
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.databinding.AuthorNotationsBinding
import retrofit2.Response
import java.lang.ref.WeakReference


class Authors : AppCompatActivityWithDrawer() {
    private lateinit var binding: AuthorNotationsBinding

    companion object {
        private var authorNames: HashMap<String, String> = hashMapOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AuthorNotationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toggleToolbar()

        binding.newAuthorName.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayOf<String>()))
        binding.newAuthorName.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val newAuthorFullName = binding.newAuthorName.adapter.getItem(position) as String
            val authorNotationAdapter = binding.authorNotationsList.adapter as AuthorNotationAdapter
            for ((key, value) in authorNames) {
                if (value == newAuthorFullName) {
                    if (authorNotationAdapter.hasPersonCode(key)) {
                        WhatTheDuck.info(WeakReference(this), R.string.input_error__author_already_rated, Toast.LENGTH_SHORT)
                        break
                    }
                    val authorNotation = AuthorNotation(key, 5)
                    api.createAuthorNotation(authorNotation).enqueue(object : DmServer.Callback<Void>("createAuthorNotation", this@Authors, true) {
                        override fun onSuccessfulResponse(response: Response<Void>) {
                            authorNotationAdapter.addAuthor(authorNotation)
                            binding.authorNotationsList.visibility = VISIBLE
                            binding.authorNotationsNoResults.visibility = GONE
                        }
                    })
                    break
                }
            }
            binding.newAuthorName.text.clear()
        }
        binding.newAuthorName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length <= 3) {
                    return
                }
                api.searchAuthor(s.toString()).enqueue(object : DmServer.Callback<HashMap<String, String>>("searchAuthor", this@Authors, true) {
                    override fun onSuccessfulResponse(response: Response<HashMap<String, String>>) {
                        authorNames.putAll(response.body()!!)
                        binding.newAuthorName.setAdapter(ArrayAdapter(this@Authors, android.R.layout.simple_dropdown_item_1line, response.body()!!.values.toTypedArray()))
                    }
                })
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        api.authorNotations.enqueue(object : DmServer.Callback<List<AuthorNotation>>("getAuthorNotations", this, true) {
            override fun onSuccessfulResponse(response: Response<List<AuthorNotation>>) {
                val authorNotations = response.body()!!.toMutableList()
                binding.authorNotationsList.layoutManager = LinearLayoutManager(this@Authors)

                if (authorNotations.isEmpty()) {
                    binding.authorNotationsList.adapter = AuthorNotationAdapter(this@Authors, authorNotations)
                    toggleEmptyAuthorListVisibility()
                }
                else {
                    val personCodes = authorNotations.joinToString(",") { authorNotation -> authorNotation.personCode }
                    api.getAuthorNames(personCodes).enqueue(object : DmServer.Callback<HashMap<String, String>>("getAuthorNotations", this@Authors, true) {
                        override fun onSuccessfulResponse(response: Response<HashMap<String, String>>) {
                            binding.authorNotationsList.adapter = AuthorNotationAdapter(this@Authors, authorNotations)
                            toggleEmptyAuthorListVisibility()
                            toggleMaxAuthorsWatchedVisibility()
                            authorNames.putAll(response.body()!!)
                        }
                    })
                }
            }

        })
    }

    fun toggleMaxAuthorsWatchedVisibility() {
        val hasReachedMax = (binding.authorNotationsList.adapter as AuthorNotationAdapter).authorNotations.size >= 5
        binding.maxAuthorCountReached.visibility = if (hasReachedMax) { VISIBLE } else { GONE }
        binding.newAuthorName.visibility = if (hasReachedMax) { GONE } else { VISIBLE }
    }

    fun toggleEmptyAuthorListVisibility() {
        val isEmptyList = (binding.authorNotationsList.adapter as AuthorNotationAdapter).authorNotations.isEmpty()
        binding.authorNotationsNoResults.visibility = if (isEmptyList) { VISIBLE } else { GONE }
        binding.authorNotationsList.visibility = if (isEmptyList) { GONE } else { VISIBLE }
    }

    class AuthorNotationAdapter internal constructor(
        val activity: Authors,
        var authorNotations: MutableList<AuthorNotation>
    ) : RecyclerView.Adapter<AuthorNotationAdapter.ViewHolder>() {

        private val inflater: LayoutInflater = LayoutInflater.from(activity)

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var authorName: TextView = itemView.findViewById(R.id.authorname)
            var notation: RatingBar = itemView.findViewById(R.id.rating)
            var deleteRating: Button = itemView.findViewById(R.id.authordelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(inflater.inflate(R.layout.row_author_notation, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val currentNotation = authorNotations[position]
            holder.authorName.text = authorNames[currentNotation.personCode]
            holder.notation.rating = currentNotation.notation.toFloat()
            holder.notation.setOnRatingBarChangeListener { _, value, _ ->
                val updatedAuthorNotation = AuthorNotation(authorNotations[position].personCode, value.toInt())
                api.updateAuthorNotation(updatedAuthorNotation).enqueue(object : DmServer.Callback<Void>("updateAuthorNotation", activity, true) {
                    override fun onSuccessfulResponse(response: Response<Void>) {
                        authorNotations[position] = updatedAuthorNotation
                        notifyDataSetChanged()
                    }
                })
            }

            holder.deleteRating.setOnClickListener {
                api.deleteAuthorNotation(currentNotation).enqueue(object : DmServer.Callback<Void>("deleteAuthorNotation", activity, true) {
                    override fun onSuccessfulResponse(response: Response<Void>) {
                        authorNotations.removeAt(position)
                        notifyDataSetChanged()
                        activity.toggleEmptyAuthorListVisibility()
                        activity.toggleMaxAuthorsWatchedVisibility()
                    }
                })
            }
        }

        override fun getItemCount() = authorNotations.size

        fun addAuthor(author: AuthorNotation) {
            authorNotations.add(author)
            notifyDataSetChanged()
            activity.toggleMaxAuthorsWatchedVisibility()
        }

        fun hasPersonCode(personCode: String) : Boolean {
            return null != authorNotations.find { author -> author.personCode == personCode }
        }
    }

}
