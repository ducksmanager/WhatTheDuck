package net.ducksmanager.whattheduck

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.apigateway.DmServer
import net.ducksmanager.persistence.models.coa.InducksCountryName
import net.ducksmanager.persistence.models.composite.InducksCountryNameWithPossession
import net.ducksmanager.whattheduck.WhatTheDuckApplication.appDB
import net.ducksmanager.whattheduck.WhatTheDuckApplication.applicationVersion
import retrofit2.Response

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.settings_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<View>(R.id.notifySwitch).setOnClickListener { view ->
            findViewById<View>(R.id.notifiedCountriesListWrapper).visibility = if ((view as Switch).isChecked)
                View.VISIBLE
            else
                View.GONE
        }

        CountryList.downloadList(this) { _, _ ->
            run {
                appDB.inducksCountryDao().findAllWithPossession().observe(this, Observer { countryNames ->
                    val recyclerView = findViewById<RecyclerView>(R.id.notifiedCountriesList)
                    recyclerView.adapter = CountryToNotifyListAdapter(this, countryNames)
                    recyclerView.layoutManager = LinearLayoutManager(this)
                })
            }
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        }
    }

    class CountryToNotifyListAdapter internal constructor(
        private val context: Context,
        private var countriesToNotify: MutableList<InducksCountryNameWithPossession>
    ) : RecyclerView.Adapter<CountryToNotifyListAdapter.ViewHolder>() {

        private val inflater: LayoutInflater = LayoutInflater.from(context)

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val countryItemView: TextView = itemView.findViewById(R.id.itemtitle)
            val prefixImageView: ImageView = itemView.findViewById(R.id.prefiximage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = inflater.inflate(R.layout.row_notified_country, parent, false)
            itemView.findViewById<ImageView>(R.id.suffiximage).visibility = View.GONE
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val currentItem = countriesToNotify[position]
            holder.countryItemView.text = currentItem.country.countryName
            holder.prefixImageView.setImageResource(getImageResourceFromCountry(currentItem.country))
        }

        private fun getImageResourceFromCountry(country: InducksCountryName): Int {
            val uri = "@drawable/flags_" + country.countryCode
            var imageResource = context.resources.getIdentifier(uri, null, context.packageName)

            if (imageResource == 0) {
                imageResource = R.drawable.flags_unknown
            }
            return imageResource
        }

        override fun getItemCount() = countriesToNotify.size
    }
}
