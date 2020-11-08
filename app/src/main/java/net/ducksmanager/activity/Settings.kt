package net.ducksmanager.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row.view.*
import kotlinx.android.synthetic.main.settings.*
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.coa.InducksCountryName
import net.ducksmanager.persistence.models.composite.CountryListToUpdate
import net.ducksmanager.persistence.models.composite.UserSetting
import net.ducksmanager.util.AppCompatActivityWithDrawer
import net.ducksmanager.util.Settings
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.applicationVersion
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.isOfflineMode
import net.ducksmanager.whattheduck.databinding.SettingsBinding
import retrofit2.Response
import java.lang.ref.WeakReference


class Settings : AppCompatActivityWithDrawer() {
    private lateinit var binding: SettingsBinding
    
    override fun shouldShowToolbar() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toggleToolbar()

        binding.notifySwitch.isChecked = appDB!!.userSettingDao().findByKey(UserSetting.SETTING_KEY_NOTIFICATIONS_ENABLED)?.value.equals("1")

        Settings.loadNotificationCountries(this) {
            appDB!!.inducksCountryDao().findAllWithNotification().observe(this, { countryNamesWithNotification ->
                val countryNames = countryNamesWithNotification.map { it.country }
                val countriesToNotifyTo = countryNamesWithNotification.filter { it.isNotified }.map { it.country.countryCode }.toMutableSet()

                val recyclerView = binding.notifiedCountriesList
                recyclerView.adapter = CountryToNotifyListAdapter(this@Settings, countryNames, countriesToNotifyTo)
                recyclerView.layoutManager = LinearLayoutManager(this@Settings)

                if (isOfflineMode) {
                    binding.warningMessage.visibility = View.VISIBLE
                    binding.notifySwitch.isEnabled = false
                    binding.save.isEnabled = false
                }
                binding.progressBar.visibility = View.GONE
            })
        }

        binding.version.text = getString(R.string.version, applicationVersion)

        binding.save.setOnClickListener {
            if (notifySwitch.isChecked) {
                WhatTheDuck.registerForNotifications(WeakReference(this@Settings), true)
            }
            else {
                WhatTheDuck.unregisterFromNotifications()
            }

            val countriesToNotifyTo = (binding.notifiedCountriesList.adapter as CountryToNotifyListAdapter).countriesToNotifyTo
            DmServer.api.updateUserNotificationCountries(CountryListToUpdate(countriesToNotifyTo))
                .enqueue(object: DmServer.Callback<Void>("updateUserNotificationCountries", this, true) {
                    override fun onSuccessfulResponse(response: Response<Void>) {
                        finish()
                    }
                })
        }
    }

    class CountryToNotifyListAdapter internal constructor(
        private val context: Context,
        private var countries: List<InducksCountryName>,
        val countriesToNotifyTo: MutableSet<String>
    ) : RecyclerView.Adapter<CountryToNotifyListAdapter.ViewHolder>() {

        private val inflater: LayoutInflater = LayoutInflater.from(context)

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val background: ImageView? = itemView.findViewById(R.id.background)
            val countryItemView: TextView = itemView.findViewById(R.id.itemtitle)
            val prefixImageView: ImageView = itemView.findViewById(R.id.prefiximage)
            val isNotifiedCountry: CheckBox = itemView.findViewById(R.id.isNotifiedCountry)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = inflater.inflate(R.layout.row_notified_country, parent, false)
            itemView.suffiximage.visibility = View.GONE
            itemView.suffixtext.visibility = View.GONE
            itemView.itemdescription.visibility = View.GONE
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val currentItem = countries[position]
            holder.background?.layoutParams?.width = 0
            holder.countryItemView.text = currentItem.countryName
            holder.prefixImageView.setImageResource(getImageResourceFromCountry(currentItem))
            holder.isNotifiedCountry.isEnabled = !isOfflineMode
            holder.isNotifiedCountry.isChecked = countriesToNotifyTo.contains(currentItem.countryCode)
            holder.isNotifiedCountry.setOnClickListener {
                if ((it as CheckBox).isChecked) {
                    countriesToNotifyTo.add(currentItem.countryCode)
                }
                else  {
                    countriesToNotifyTo.remove(currentItem.countryCode)
                }
            }
        }

        private fun getImageResourceFromCountry(country: InducksCountryName): Int {
            val uri = "@drawable/flags_" + country.countryCode
            var imageResource = context.resources.getIdentifier(uri, null, context.packageName)

            if (imageResource == 0) {
                imageResource = R.drawable.flags_unknown
            }
            return imageResource
        }

        override fun getItemCount() = countries.size
    }
}
