package com.patrickarungari.tukenyahub.utils;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import com.patrickarungari.tukenyahub.Repository.Repository;
import com.patrickarungari.tukenyahub.ViewModel.LoginViewModel;

import javax.inject.Inject;

/**
 * Created by ${Saquib} on 03-05-2018.
 */

public class viewModelFactory implements ViewModelProvider.Factory {

    private Repository repository;

    @Inject
    public viewModelFactory(Repository repository) {
        this.repository = repository;
    }


    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown class name");
    }
}