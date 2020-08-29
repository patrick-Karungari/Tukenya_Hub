package com.patrickarungari.messenger.ui.messages.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.patrickarungari.messenger.R;
import com.patrickarungari.messenger.core.messages.models.Message;
import com.patrickarungari.messenger.databinding.ActivityImageDetailsBinding;
import com.patrickarungari.messenger.ui.ChatUI;
import com.patrickarungari.messenger.utils.StringUtils;
import com.patrickarungari.messenger.utils.TimeUtils;

import java.util.Map;

/**
 * Created by stefanodp91 on 25/11/2016.
 * <p>
 * Resolve Issue #32
 */
public class ImageDetailsActivity extends AppCompatActivity {
    private static final String TAG = ImageDetailsActivity.class.getName();
    private ActivityImageDetailsBinding mBinnding;

    private Message message;

//    private FloatingActionButton mBtnShare;
//    private FloatingActionButton mBtnDownload;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ActivityImageDetailsBinding.inflate(getLayoutInflater()).getRoot());
        if (getIntent().getExtras() != null) {
            String transitionName = getIntent().getStringExtra("transitionName");
            findViewById(R.id.image).setTransitionName(transitionName);
            message = (Message) getIntent().getExtras().getSerializable(ChatUI.BUNDLE_MESSAGE);
        }


        android.transition.Transition transition = TransitionInflater.from(this)
                .inflateTransition(R.transition.image_shared_element_transition);
        (this).getWindow().setSharedElementEnterTransition(transition);
        (this).getWindow().setSharedElementReturnTransition(transition);

        //Button setup
        // ### begin toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // ### end toolbar


        registerViews();

        // ### begin image
        String imgUrl = getImageUrl(message);
        setImage(imgUrl);
        // ### end image

        // ### begin title
        String title = message.getText();
        if (StringUtils.isValid(title)) {
            TextView mTitle = findViewById(R.id.image_title);
            mTitle.setText(title);
        }
        // ### end title

        // ### begin sender
        String sender = message.getSenderFullname();
        if (StringUtils.isValid(sender)) {
            TextView mSender = findViewById(R.id.sender);
            mSender.setText(sender);
        }
        // ### end sender

        // ### begin timestamp
        TextView mTimestamp = findViewById(R.id.timestamp);
        try {
            long timestamp = message.getTimestamp();
            String formattedTimestamp = TimeUtils.getFormattedTimestamp(this, timestamp);
            mTimestamp.setText(formattedTimestamp);
        } catch (Exception e) {
            Log.e(TAG, "cannot retrieve the timestamp. " + e.getMessage());
        }
        // ### end timestamp


//        // change the statusbar color
//        ThemeUtils.changeStatusBarColor(this, getResources().getColor(R.color.black));

//        initListeners();
    }


    private void registerViews() {
        Log.i(TAG, "registerViews");

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
            case (MotionEvent.ACTION_MOVE):
            case (MotionEvent.ACTION_OUTSIDE):
            case (MotionEvent.ACTION_CANCEL):
            case (MotionEvent.ACTION_UP):
                onBackPressed();
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private String getImageUrl(Message message) {
        String imgUrl = "";

        Map<String, Object> metadata = message.getMetadata();
        if (metadata != null) {
            imgUrl = (String) metadata.get("src");
        }

        return imgUrl;
    }

    private void setImage(String imgUrl) {
        Log.i(TAG, "setImage");

        final ImageView mImage = findViewById(R.id.image);
        supportPostponeEnterTransition();

        // https://github.com/MikeOrtiz/TouchImageView/issues/135
        Glide.with(this)
                .asBitmap()
                .dontAnimate()
                .load(imgUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        mImage.setImageBitmap(resource);
                        supportPostponeEnterTransition();

                        startPostponedEnterTransition();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}