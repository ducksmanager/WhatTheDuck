package net.ducksmanager.inducks.coa

import android.app.Activity

import com.koushikdutta.async.future.FutureCallback

import net.ducksmanager.whattheduck.WhatTheDuck

import org.json.JSONException
import org.json.JSONObject

import java.util.HashMap

class CountryListing(activity: Activity, callback: FutureCallback<*>) : CoaListing(activity, CoaListing.ListType.COUNTRY_LIST, callback) {

    protected override val urlSuffix: String
        get() = "/coa/list/countries/{locale}"

    @Throws(JSONException::class)
    override fun processData(response: String?) {
        if (response != null) {
            resetCountries()
            addCountriesFullList(JSONObject(response))
        }
    }

    companion object {

        private var countryNames = HashMap<String, String>()
        var hasFullList = false

        fun getCountryFullName(shortCountryName: String): String {
            return countryNames[shortCountryName]
        }

        private fun resetCountries() {
            countryNames = HashMap()
        }

        private fun addCountry(shortName: String, fullName: String) {
            countryNames[shortName] = fullName
        }

        @Throws(JSONException::class)
        fun addCountries(countryNames: JSONObject) {
            var countryNames = countryNames
            if (!hasFullList) {
                if (countryNames.has("static")) { // Legacy JSON structure
                    countryNames = countryNames.getJSONObject("static").getJSONObject("pays")
                }

                val countryIterator = countryNames.keys()
                while (countryIterator.hasNext()) {
                    val shortName = countryIterator.next()
                    val fullName = countryNames.getString(shortName)

                    addCountry(shortName, fullName)
                    if (shortName != "zz" && !WhatTheDuck.coaCollection.hasCountry(shortName)) {
                        WhatTheDuck.coaCollection.addCountry(shortName)
                    }
                }
            }
        }

        @Throws(JSONException::class)
        private fun addCountriesFullList(`object`: JSONObject) {
            addCountries(`object`)
            hasFullList = true
        }
    }
}
