package net.ducksmanager.apigateway;

import net.ducksmanager.persistence.models.dm.Issue;
import net.ducksmanager.persistence.models.dm.Purchase;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface DmServerApi {
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
    Call<String> createUserPurchase(@Body Purchase $purchase);
}
