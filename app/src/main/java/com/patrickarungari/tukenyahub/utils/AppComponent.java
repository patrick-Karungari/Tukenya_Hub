package com.patrickarungari.tukenyahub.utils;


import com.patrickarungari.tukenyahub.Activities.LoginActivity;
import com.patrickarungari.tukenyahub.Activities.MainActivity;
import com.patrickarungari.tukenyahub.Activities.SignupActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by ${Saquib} on 03-05-2018.
 */

@Component(modules = {AppModule.class, UtilsModule.class})
@Singleton
public interface AppComponent {


    //void inject(ChatFirebaseMessagingService chatFirebaseMessagingService);

    @Component.Builder
    interface Builder {
        AppComponent build();
        Builder appModule(AppModule appModule);

        Builder utilsModule(UtilsModule utilsModule);
    }
    void doInjection(LoginActivity loginActivity);
    void doInjection(MainActivity mainActivity);
    void doInjection(SignupActivity signupActivity);
}