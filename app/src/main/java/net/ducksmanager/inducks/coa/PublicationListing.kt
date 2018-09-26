package net.ducksmanager.inducks.coa

import android.app.Activity

import com.koushikdutta.async.future.FutureCallback

import net.ducksmanager.whattheduck.WhatTheDuck

import org.json.JSONException
import org.json.JSONObject

import java.util.HashMap
import java.util.HashSet

class PublicationListing(list: Activity, private val countryShortName: String, callback: FutureCallback<*>) : CoaListing(list, CoaListing.ListType.PUBLICATION_LIST, callback) {

    protected override val urlSuffix: String
        get() = "/coa/list/publications/$countryShortName"

    @Throws(JSONException::class)
    override fun processData(response: String?) {
        if (response != null) {
            addFullPublications(countryShortName, JSONObject(response))
        }
    }

    companion object {

        private val publicationNames = HashMap<String, HashMap<String, String>>()
        private val fullListCountries = HashSet<String>()

        fun getPublicationFullName(shortCountryName: String, shortPublicationName: String): String? {
            if (publicationNames[shortCountryName] == null) {
                println("Can't get publications of country $shortCountryName")
                return null
            }
            return publicationNames[shortCountryName].get(shortPublicationName)
        }

        private fun addPublication(countryShortName: String, shortName: String, fullName: String) {
            if (publicationNames[countryShortName] == null)
                publicationNames[countryShortName] = HashMap()
            publicationNames[countryShortName][shortName] = fullName
        }

        fun hasFullList(country: String): Boolean {
            return fullListCountries.contains(country)
        }

        @Throws(JSONException::class)
        private fun addFullPublications(countryShortName: String, `object`: JSONObject) {
            addPublications(`object`)
            fullListCountries.add(countryShortName)
        }

        @Throws(JSONException::class)
        fun addPublications(publicationNames: JSONObject) {
            var publicationNames = publicationNames
            if (publicationNames.has("static")) { // Legacy JSON structure
                publicationNames = publicationNames.getJSONObject("static").getJSONObject("magazines")
            }
            val publicationIterator = publicationNames.keys()
            while (publicationIterator.hasNext()) {
                val publicationShortName = publicationIterator.next()
                val publicationFullName = publicationNames.getString(publicationShortName)
                val countryShortName = publicationShortName.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0]

                addPublication(countryShortName, publicationShortName, publicationFullName)
                WhatTheDuck.coaCollection.addPublication(countryShortName, publicationShortName)
            }
        }
    }
}
