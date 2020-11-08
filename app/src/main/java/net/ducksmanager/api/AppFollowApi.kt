package net.ducksmanager.api

import net.ducksmanager.persistence.models.appfollow.Apps
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AppFollowApi {
    @GET("/apps/app")
    fun getAppVersion(@Query(value="apps_id") appId: Int = 117727): Call<Apps>
}