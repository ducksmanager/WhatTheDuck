package net.ducksmanager.whattheduck

import java.io.Serializable

class IssueWithFullUrl(val countryCode: String, val publicationCode: String, val publicationTitle: String, val issueNumber: String, val fullUrl: String) : Serializable
