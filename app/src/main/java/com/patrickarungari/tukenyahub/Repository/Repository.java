package com.patrickarungari.tukenyahub.Repository;

import com.google.gson.JsonElement;
import com.patrickarungari.tukenyahub.utils.ApiInterface;

import io.reactivex.Observable;

public class Repository {

    private ApiInterface apiInterface;

    public Repository(ApiInterface apiCallInterface) {
        this.apiInterface = apiCallInterface;
    }

    /*
     * method to call login api
     * */
    public Observable<JsonElement> executeLogin(String uniNum, String password) {
        return apiInterface.login(uniNum, password);
    }
    public Observable<JsonElement> executeRegister(String email, String password, String uniNum, String username, String image ) {
        return apiInterface.register(email,password,uniNum,username,image);
    }

}