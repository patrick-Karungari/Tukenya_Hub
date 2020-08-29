package com.patrickarungari.tukenyahub.Modules;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.patrickarungari.sharedPrefManager.SharedPrefManager;
import com.patrickarungari.tukenyahub.R;

public class AsyncClass {


    public AsyncClass(Context context, @Nullable NetworkImageView viewImage, @Nullable TextView textName, @Nullable TextView textUniNum) {
        Context mContext;
        NetworkImageView imageView;
        TextView name, uniNum,email;
        mContext = context;
        imageView = viewImage;
        name = textName;
        uniNum = textUniNum;
        ImageLoader imageLoader = RequestHandler.getInstance(context).getImageLoader();
        imageView.setErrorImageResId(R.drawable.pic_profile);
        imageView.setImageUrl(SharedPrefManager.getInstance(mContext).getimageData(), imageLoader);
        name.setText(SharedPrefManager.getInstance(mContext).getUsername());
        uniNum.setText(SharedPrefManager.getInstance(mContext).getuniNum());
    }
    public static void getInstance(Context context){
        String name, uniNum,email;
        name = SharedPrefManager.getInstance(context).getUsername();
        uniNum = SharedPrefManager.getInstance(context).getuniNum();
        email = SharedPrefManager.getInstance(context).getUserEmail();
    }
}
