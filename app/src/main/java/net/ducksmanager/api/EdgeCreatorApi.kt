package net.ducksmanager.api

import net.ducksmanager.persistence.models.edgecreator.EdgeCanvas
import net.ducksmanager.persistence.models.edgecreator.EdgePhoto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.*

interface EdgeCreatorApi {
    @POST("/fs/upload-base64")
    fun upload(@Body photo: EdgePhoto): Call<HashMap<String, String>>

    @POST("/fs/save")
    fun saveEdgeCanvas(@Body photo: EdgeCanvas): Call<Void>
}