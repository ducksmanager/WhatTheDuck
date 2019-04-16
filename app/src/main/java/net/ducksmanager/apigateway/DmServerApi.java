package net.ducksmanager.apigateway;

import net.ducksmanager.persistence.models.composite.CoverSearchResults;
import net.ducksmanager.persistence.models.composite.IssueListToUpdate;
import net.ducksmanager.persistence.models.composite.UserToCreate;
import net.ducksmanager.persistence.models.dm.Issue;
import net.ducksmanager.persistence.models.dm.Purchase;
import net.ducksmanager.persistence.models.dm.User;

import java.util.HashMap;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface DmServerApi {
    @PUT("/ducksmanager/user")
    Call<Void> createUser(@Body UserToCreate user);

    @GET("/coa/list/countries/{locale}")
    Call<HashMap<String, String>> getCountries(@Path("locale") String locale);

    @GET("/coa/list/publications/{countryName}")
    Call<HashMap<String, String>> getPublications(@Path("countryName") String countryName);

    @GET("/coa/list/issues/{publicationCode}")
    Call<List<String>> getIssues(@Path("publicationCode") String publicationCode);

    @GET("/collection/issues")
    Call<List<Issue>> getUserIssues();

    @GET("/collection/purchases")
    Call<List<Purchase>> getUserPurchases();

    @POST("/collection/purchases")
    Call<Void> createUserPurchase(@Body Purchase purchase);

    @POST("/collection/issues")
    Call<Object> createUserIssues(@Body IssueListToUpdate issueListToUpdate);

    @Multipart
    @POST("/cover-id/search")
    Call<CoverSearchResults> searchFromCover(@Part MultipartBody.Part file, @Part("wtd_jpg") RequestBody fileName);
}
