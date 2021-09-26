package net.ducksmanager.api

import net.ducksmanager.persistence.models.coa.InducksIssueWithCoverUrl
import net.ducksmanager.persistence.models.composite.*
import net.ducksmanager.persistence.models.dm.ContributionTotalPoints
import net.ducksmanager.persistence.models.dm.Issue
import net.ducksmanager.persistence.models.dm.Purchase
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface DmServerApi {
    @PUT("/ducksmanager/user")
    fun createUser(@Body user: UserToCreate): Call<Void>

    @POST("/ducksmanager/resetpassword/init")
    fun initForgotPassword(@Body emailWrapper: EmailWrapper): Call<Void>

    @GET("/coa/list/countries/{locale}")
    fun getCountries(@Path("locale") locale: String): Call<HashMap<String, String>>

    @get:GET("/coa/list/publications")
    val publications: Call<HashMap<String, String>>

    @get:GET("/coa/list/issues/count")
    val issueCount: Call<HashMap<String, Int>>

    @GET("/coa/list/issues/withDetails/{publicationCode}")
    fun getIssues(@Path(value = "publicationCode", encoded = true) publicationCode: String): Call<List<InducksIssueWithCoverUrl>>

    @get:GET("/collection/issues")
    val userIssues: Call<List<Issue>>

    @get:GET("/collection/purchases")
    val userPurchases: Call<List<Purchase>>

    @get:GET("/collection/notifications/countries")
    val userNotificationCountries: Call<List<String>>

    @get:GET("/collection/points")
    val userPoints: Call<HashMap<Int, List<ContributionTotalPoints>>>

    @GET("/coa/authorsfullnames/search/{nameStart}")
    fun searchAuthor(@Path(value = "nameStart") nameStart: String): Call<HashMap<String, String>>

    @POST("/coa/stories/search/withIssues")
    fun searchStory(@Body searchInput: StorySearchInput): Call<StorySearchResults>

    @GET("/coa/authorsfullnames/{personCodes}")
    fun getAuthorNames(@Path(value = "personCodes") personCodes: String): Call<HashMap<String, String>>

    @get:GET("/collection/authors/watched")
    val authorNotations: Call<List<AuthorNotation>>

    @PUT("/collection/authors/watched")
    fun createAuthorNotation(@Body authorNotation: AuthorNotation): Call<Void>

    @POST("/collection/authors/watched")
    fun updateAuthorNotation(@Body authorNotation: AuthorNotation): Call<Void>

    @HTTP(method="DELETE", path = "/collection/authors/watched", hasBody = true)
    fun deleteAuthorNotation(@Body authorNotation: AuthorNotation): Call<Void>

    @get:GET("/collection/stats/suggestedissues/countries_to_notify/_")
    val suggestedIssues: Call<SuggestionList>

    @get:GET("/collection/stats/suggestedissues/countries_to_notify/_/oldestdate")
    val suggestedIssuesByReleaseDate: Call<SuggestionList>

    @POST("/collection/notifications/countries")
    fun updateUserNotificationCountries(@Body countryListToUpdate: CountryListToUpdate): Call<Void>

    @POST("/collection/feedback")
    fun sendFeedback(@Body feedback: UserFeedback): Call<Void>

    @POST("/collection/purchases")
    fun createUserPurchase(@Body purchase: Purchase): Call<Void>

    @POST("/collection/issues")
    fun updateUserIssues(@Body issueListToUpdate: IssueListToUpdate): Call<Any>

    @POST("/collection/issues")
    fun updateUserIssueCopies(@Body issueCopiesToUpdate: IssueCopiesToUpdate): Call<Any>

    @Multipart
    @POST("/cover-id/search")
    fun searchFromCover(@Part file: MultipartBody.Part, @Part("wtd_jpg") fileName: RequestBody): Call<CoverSearchResults>
}