package com.patrickarungari.tukenyahub.utils;

import com.google.gson.JsonElement;
import com.patrickarungari.tukenyahub.Modules.Constants;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiInterface {

    @FormUrlEncoded
    @POST(Constants.URL_LOGIN)
    Observable<JsonElement> login(@Field("uniNum") String uniNum, @Field("password") String password);

    @FormUrlEncoded
    @POST(Constants.URL_REGISTER)
    Observable<JsonElement> register( @Field("email") String email,
                                      @Field("password") String password,
                                      @Field("uniNum") String uniNum,
                                      @Field("username") String username,
                                      @Field("image") String image);
}