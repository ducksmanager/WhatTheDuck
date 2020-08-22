package net.ducksmanager.api

import net.ducksmanager.persistence.models.composite.*
import net.ducksmanager.persistence.models.dm.Issue
import net.ducksmanager.persistence.models.dm.Purchase
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import java.util.*

interface DmServerApi {
    @PUT("/ducksmanager/user")
    fun createUser(@Body user: UserToCreate): Call<Void>

    @GET("/coa/list/countries/{locale}")
    fun getCountries(@Path("locale") locale: String): Call<HashMap<String, String>>

    @get:GET("/coa/list/publications")
    val publications: Call<HashMap<String, String>>

    @GET("/coa/list/issues/withTitle/{publicationCode}")
    fun getIssues(@Path(value = "publicationCode", encoded = true) publicationCode: String): Call<HashMap<String, String>>

    @get:GET("/collection/issues")
    val userIssues: Call<List<Issue>>

    @get:GET("/collection/purchases")
    val userPurchases: Call<List<Purchase>>

    @get:GET("/collection/notifications/countries")
    val userNotificationCountries: Call<List<String>>

    @get:GET("/collection/stats/suggestedissues/countries_to_notify/_")
    val suggestedIssues: Call<SuggestionList>

    @POST("/collection/notifications/countries")
    fun updateUserNotificationCountries(@Body countryListToUpdate: CountryListToUpdate): Call<Void>

    @POST("/collection/purchases")
    fun createUserPurchase(@Body purchase: Purchase): Call<Void>

    @POST("/collection/issues")
    fun createUserIssues(@Body issueListToUpdate: IssueListToUpdate): Call<Any>

    @Multipart
    @POST("/cover-id/search")
    fun searchFromCover(@Part file: MultipartBody.Part, @Part("wtd_jpg") fileName: RequestBody): Call<CoverSearchResults>
}