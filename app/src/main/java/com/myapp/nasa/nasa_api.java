package com.myapp.nasa;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface nasa_api {

    @GET
    Call<nasa> getNasa(@Url String url);
}
