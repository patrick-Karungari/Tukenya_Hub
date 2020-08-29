package com.patrickarungari.tukenyahub.ViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.JsonElement;
import com.patrickarungari.tukenyahub.Repository.Repository;
import com.patrickarungari.tukenyahub.utils.ApiResponse;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class LoginViewModel extends ViewModel {

    private Repository repository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final MutableLiveData<ApiResponse> responseLiveData = new MutableLiveData<>();


    public LoginViewModel(Repository repository) {
        this.repository = repository;
    }

    public MutableLiveData<ApiResponse> loginResponse() {
        return responseLiveData;
    }

    public MutableLiveData<ApiResponse> RegisterResponse() {
        return responseLiveData;
    }

    /*
     * method to call normal login api with $(mobileNumber + password)
     * */
    public void hitLoginApi(String uniNum, String password) {

        disposables.add(repository.executeLogin(uniNum, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe((d) -> responseLiveData.setValue(ApiResponse.loading()))
                .subscribe(
                        new Consumer<JsonElement>() {
                            @Override
                            public void accept(JsonElement result) throws Exception {
                                responseLiveData.setValue(ApiResponse.success(result));
                            }
                        },
                        throwable -> responseLiveData.setValue(ApiResponse.error(throwable))
                ));

    }

    /*
     * method to call normal register api
     * */
    public void hitRegisterApi(String email, String password, String uniNum, String username, String image) {

        disposables.add(repository.executeRegister(email, password,uniNum,username,image)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe((d) -> responseLiveData.setValue(ApiResponse.loading()))
                .subscribe(
                        new Consumer<JsonElement>() {
                            @Override
                            public void accept(JsonElement result) throws Exception {
                                responseLiveData.setValue(ApiResponse.success(result));
                            }
                        },
                        throwable -> responseLiveData.setValue(ApiResponse.error(throwable))
                ));

    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }
}