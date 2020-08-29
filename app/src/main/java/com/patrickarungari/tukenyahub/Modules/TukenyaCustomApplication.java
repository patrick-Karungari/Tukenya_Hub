package com.patrickarungari.tukenyahub.Modules;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.style.URLSpan;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.patrickarungari.messenger.core.ChatManager;
import com.patrickarungari.messenger.core.users.models.IChatUser;
import com.patrickarungari.messenger.ui.ChatUI;
import com.patrickarungari.messenger.ui.contacts.activites.ContactListActivity;
import com.patrickarungari.messenger.ui.conversations.listeners.OnNewConversationClickListener;
import com.patrickarungari.messenger.ui.messages.listeners.OnMessageClickListener;
import com.patrickarungari.messenger.utils.IOUtils;
import com.patrickarungari.tukenyahub.R;
import com.patrickarungari.tukenyahub.utils.AppComponent;
import com.patrickarungari.tukenyahub.utils.AppModule;
import com.patrickarungari.tukenyahub.utils.DaggerAppComponent;
import com.patrickarungari.tukenyahub.utils.UtilsModule;
import com.pushlink.android.PushLink;
import com.pushlink.android.StatusBarStrategy;
import com.pushlink.android.StrategyEnum;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import static com.patrickarungari.messenger.core.ChatManager._SERIALIZED_CHAT_CONFIGURATION_LOGGED_USER;


public class TukenyaCustomApplication extends Application {
    final String TAG = getClass().getSimpleName();
    AppComponent appComponent;
    Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        appComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).utilsModule(new UtilsModule()).build();
        String yourDeviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        PushLink.start(this, R.raw.logo, "gqdgbnsgekv2vqel", yourDeviceID);
        PushLink.setCurrentStrategy(StrategyEnum.STATUS_BAR);
        StatusBarStrategy sbs = (StatusBarStrategy) PushLink.getCurrentStrategy();
        sbs.setStatusBarTitle("Hello, New version available!");
        sbs.setStatusBarDescription("Tap to discover more.");
        //This information will be shown in two places: "Installations" and "Exceptions" tabs of the web administration
        PushLink.addMetadata("Brand", Build.BRAND);
        PushLink.addMetadata("Model", Build.MODEL);
        PushLink.addMetadata("OS Version", Build.VERSION.RELEASE);
        EmojiManager.install(new IosEmojiProvider());
       initChatSDK();
    }
    public AppComponent getAppComponent() {
        return appComponent;
    }
    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }
  public void initChatSDK() {
        //enable persistence must be made before any other usage of FirebaseDatabase instance.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        // it creates the chat configurations
        ChatManager.Configuration mChatConfiguration =
                new ChatManager.Configuration.Builder(getString(R.string.chat_firebase_appId))
                        .firebaseUrl(getString(R.string.chat_firebase_url))
                        .storageBucket(getString(R.string.chat_firebase_storage_bucket))
                        .build();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //assuming you have a login, check if the logged user (converted to IChatUser) is valid
        //if (currentUser != null) {
        if (currentUser != null && currentUser.isEmailVerified()) {
            IChatUser iChatUser = (IChatUser) IOUtils.getObjectFromFile(context,
                    _SERIALIZED_CHAT_CONFIGURATION_LOGGED_USER);
            ChatManager.start(this, mChatConfiguration, iChatUser);
            Log.i(TAG, "chat has been initialized with success");
            ChatUI.getInstance().setContext(context);
            ChatUI.getInstance().enableGroups(true);
            ChatUI.getInstance().setOnMessageClickListener((OnMessageClickListener) (message, clickableSpan) -> {
                String text = ((URLSpan) clickableSpan).getURL();
                Uri uri = Uri.parse(text);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(browserIntent);
            });
            // set on new conversation click listener
            //final IChatUser support = new ChatUser("support", "Chat21 Support");
            final IChatUser support = null;
            ChatUI.getInstance().setOnNewConversationClickListener((OnNewConversationClickListener) () -> {
                if (support != null) {
                    ChatUI.getInstance().openConversationMessagesActivity(support);
                } else {
                    Intent intent = new Intent(context, ContactListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // start activity from context
                    startActivity(intent);
                }
            });
            Log.i(TAG, "ChatUI has been initialized with success");
        } else {
            Log.w(TAG, "chat can't be initialized because chatUser is null");
        }
    }
}
