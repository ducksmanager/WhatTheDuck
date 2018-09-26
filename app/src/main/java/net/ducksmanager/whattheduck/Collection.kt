package net.ducksmanager.whattheduck


import net.ducksmanager.inducks.coa.CountryListing
import net.ducksmanager.inducks.coa.PublicationListing
import net.ducksmanager.whattheduck.Issue.IssueCondition

import java.io.Serializable
import java.util.ArrayList
import java.util.HashMap

class Collection : Serializable {
    private val issues = HashMap<String, HashMap<String, HashMap<String, Issue>>>()
    private var purchases = HashMap<Int, PurchaseAdapter.Purchase>()
        set

    internal val purchasesWithEmptyItem: HashMap<String, PurchaseAdapter.Purchase>
        get() {
            val purchasesWithEmptyItem = HashMap<String, PurchaseAdapter.Purchase>()

            val values = ArrayList(purchases.values)
            values.add(PurchaseAdapter.SpecialPurchase(true))
            for (p in values) {
                purchasesWithEmptyItem[p.toString()] = p
            }

            return purchasesWithEmptyItem
        }

    val countryList: ArrayList<CountryAdapter.Country>
        get() {
            val countryList = ArrayList<CountryAdapter.Country>()
            val countrySet = issues.keys
            for (shortCountryName in countrySet) {
                countryList.add(CountryAdapter.Country(shortCountryName, CountryListing.getCountryFullName(shortCountryName)))
            }
            return countryList
        }

    enum class CollectionType {
        COA, USER
    }

    fun addCountry(country: String) {
        issues[country] = HashMap()
    }

    fun addPublication(country: String, publication: String) {
        if (issues[country] == null)
            this.addCountry(country)
        if (issues[country].get(publication) == null) {
            issues[country][publication] = HashMap()
        }
    }

    fun addIssue(countryAndPublication: String, issue: Issue) {
        val country = countryAndPublication.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0]
        addIssue(country, countryAndPublication, issue)
    }

    fun addIssue(country: String, publication: String, issue: Issue) {
        if (issues[country] == null)
            this.addCountry(country)
        if (issues[country].get(publication) == null)
            this.addPublication(country, publication)
        issues[country].get(publication)[issue.issueNumber] = issue
    }

    fun getPublicationList(shortCountryName: String): ArrayList<PublicationAdapter.Publication> {
        val publicationList = ArrayList<PublicationAdapter.Publication>()
        val publicationMap = issues[shortCountryName]
        if (publicationMap != null) {
            for (shortPublicationName in publicationMap.keys) {
                publicationList.add(PublicationAdapter.Publication(shortPublicationName, PublicationListing.getPublicationFullName(shortCountryName, shortPublicationName)))
            }
        }
        return publicationList
    }

    fun getIssueList(shortCountryName: String, shortPublicationName: String): ArrayList<Issue> {
        val finalList = ArrayList<Issue>()
        val publicationMap = issues[shortCountryName]
        if (publicationMap != null) {
            val list = publicationMap[shortPublicationName]
            if (list != null) {
                for (issue in list.values) {
                    var condition: IssueCondition? = null
                    var purchase: PurchaseAdapter.PurchaseWithDate? = null
                    val existingIssue = WhatTheDuck.userCollection.getIssue(shortCountryName, shortPublicationName, issue.issueNumber)
                    if (existingIssue != null) {
                        condition = existingIssue.issueCondition
                        purchase = existingIssue.purchase
                    }
                    finalList.add(Issue(issue.issueNumber, condition, purchase))
                }
            }
        }
        return finalList
    }

    fun hasCountry(countryShortName: String): Boolean {
        return issues[countryShortName] != null && issues[countryShortName].size > 0
    }

    fun hasPublication(shortCountryName: String, shortPublicationName: String): Boolean {
        return (hasCountry(shortCountryName)
                && issues[shortCountryName].get(shortPublicationName) != null
                && issues[shortCountryName].get(shortPublicationName).size > 0)
    }

    fun getIssue(shortCountryName: String, shortPublicationName: String, issueNumber: String): Issue? {
        return if (!hasPublication(shortCountryName, shortPublicationName)) null else issues[shortCountryName].get(shortPublicationName).get(issueNumber)
    }
}
