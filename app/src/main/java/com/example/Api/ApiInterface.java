package com.example.Api;

import com.example.Models.NewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/*
 ** Uses the URL Endpoint and other queries to complete the call.
 **/
public interface ApiInterface {

    //Endpoint for Top Country Headlines
    @GET("top-headlines")
    Call<NewsResponse> getTopCountryHeadlines(@Query("country") String country,
                                              @Query("apiKey") String apiKey);

    //Endpoint for category Headlines
    @GET("top-headlines")
    Call<NewsResponse> getCategoryHeadlines(@Query("category") String category,
                                            @Query("country") String country,
                                            @Query("apiKey") String apiKey);

    //Endpoint to fetch search results.
    @GET("everything")
    Call<NewsResponse> getSearchResults(@Query("q") String query,
                                        @Query("language") String language,
                                        @Query("apiKey") String apiKey);

}
