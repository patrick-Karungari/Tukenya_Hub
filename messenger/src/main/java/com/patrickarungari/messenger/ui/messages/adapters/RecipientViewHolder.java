package com.patrickarungari.messenger.ui.messages.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.patrickarungari.messenger.core.messages.models.Message;
import com.patrickarungari.messenger.ui.ChatUI;
import com.patrickarungari.messenger.ui.messages.activities.ImageDetailsActivity;
import com.patrickarungari.messenger.ui.messages.listeners.OnMessageClickListener;
import com.patrickarungari.messenger.utils.StringUtils;
import com.patrickarungari.messenger.utils.TimeUtils;
import com.patrickarungari.messenger.utils.image.ImageUtils;
import com.patrickarungari.messenger.utils.views.TextViewLinkHandler;
import com.vanniktech.emoji.EmojiTextView;

import com.patrickarungari.messenger.R;

import java.util.Date;
import java.util.Map;

/**
 * Created by stefano on 25/11/2016.
 */

class RecipientViewHolder extends RecyclerView.ViewHolder {

    private final EmojiTextView mMessage;
    private final TextView mDate;

    //private final CardView card;
    private final TextView mTimestamp,time;
    private final ConstraintLayout mBackgroundBubble;
    private final TextView mSenderDisplayName;
    private final ConstraintLayout mPreview; // Resolve Issue #32
    private final ProgressBar mProgressBar;   // Resolve Issue #52
    private final ImageView preview;
    private final LinearLayout msg_content;
    RecipientViewHolder(View itemView) {
        super(itemView);
        //card = itemView.findViewById(R.id.card);
        mMessage = (EmojiTextView) itemView.findViewById(R.id.message);
        mDate = (TextView) itemView.findViewById(R.id.date);
        time = (TextView) itemView.findViewById(R.id.time);
        mTimestamp = (TextView) itemView.findViewById(R.id.timestamp);
        msg_content = itemView.findViewById(R.id.msg_content);
        mBackgroundBubble = itemView.findViewById(R.id.message_group);
        mSenderDisplayName = (TextView) itemView.findViewById(R.id.sender_display_name);
        mPreview = (ConstraintLayout) itemView.findViewById(R.id.box_image); // Resolve Issue #32
        preview = itemView.findViewById(R.id.preview);
        mProgressBar = (ProgressBar) itemView.findViewById(R.id.progress);  // Resolve Issue #52
    }

    void bind(final Message previousMessage, final Message message,
              int position, OnMessageClickListener onMessageClickListener) {

        Log.d("TAG", "RecipientViewHolder");

        switch (message.getType()) {
            case Message.TYPE_IMAGE:
                mMessage.setVisibility(View.GONE);
                mPreview.setVisibility(View.VISIBLE);
                if (msg_content != null) {
                    msg_content.setVisibility(View.GONE);
                }

                setPreview(message);

                break;
            case Message.TYPE_FILE:
                mMessage.setVisibility(View.GONE);
                mPreview.setVisibility(View.VISIBLE);

                setFilePreview(message);

                break;
            case Message.TYPE_TEXT:
                mProgressBar.setVisibility(View.GONE);  // Resolve Issue #52
                msg_content.setVisibility(View.VISIBLE);
                mMessage.setVisibility(View.VISIBLE);
                mPreview.setVisibility(View.GONE);
                setMessage(message);
                break;
        }

        setBubble();

        setDate(previousMessage, message, position);

        setTimestamp(message);

        setSenderDisplayName(message);

        // click on the item
        setOnMessageClickListener(onMessageClickListener);
    }

    private String getImageUrl(Message message) {
        String imgUrl = "";

        Map<String, Object> metadata = message.getMetadata();
        if (metadata != null) {
            imgUrl = (String) metadata.get("src");
        }

        return imgUrl;
    }

    // Resolve Issue #32
    private void setPreview(final Message message) {

        // Resolve Issue #52
        mProgressBar.setVisibility(View.VISIBLE);

        Glide.with(itemView.getContext())
                .load(getImageUrl(message))
                .listener( new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mProgressBar.setVisibility(View.GONE);
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                            mProgressBar.setVisibility(View.GONE);
                            return false;
                    }
                })
                .into(preview);


        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startImagePreviewActivity(message);
            }
        });
    }

    private void setFilePreview(final Message message) {

        Glide.with(itemView.getContext())
                .load(message.getText())
                .placeholder(R.drawable.ic_placeholder_file_recipient_24dp)
                .into(preview);


        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 06/09/17 aprire il file in base al mime
            }
        });
    }

    private void startImagePreviewActivity(Message message) {
        int adapterPosition = getAdapterPosition();
        itemView.findViewById(R.id.preview).setTransitionName("image"+adapterPosition);
        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.
                makeSceneTransitionAnimation((Activity) itemView
                        .getContext(),(View) preview,"image"+adapterPosition);
        Intent intent = new Intent(itemView.getContext(), ImageDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ChatUI.BUNDLE_MESSAGE, message);
        intent.putExtra("transitionName","image"+adapterPosition);

        itemView.getContext().startActivity(intent,activityOptionsCompat.toBundle());
    }

    private void setMessage(Message message) {
        // set message text
        mMessage.setText(Html.fromHtml(message.getText()));
        // clickable link support

    }

    private void setTimestamp(Message message) {
        if (!DateFormat.is24HourFormat(itemView.getContext())) {
            mTimestamp.setText(TimeUtils.formatTimestamp(itemView.getContext(),message.getTimestamp(), "hh:mm a"));
            time.setText(TimeUtils.formatTimestamp(itemView.getContext(),message.getTimestamp(), "hh:mm a"));

        }else
            time.setText(TimeUtils.formatTimestamp(itemView.getContext(),message.getTimestamp(), "HH:mm"));
            mTimestamp.setText(TimeUtils.formatTimestamp(itemView.getContext(),message.getTimestamp(), "HH:mm"));
    }

    private void setDate(Message previousMessage, Message message, int position) {
        Date previousMessageDate = null;
        if (previousMessage != null) {
            previousMessageDate = new Date(previousMessage.getTimestamp());
        }

        Date messageDate = new Date(message.getTimestamp());
        // it's today. show the label "today"
        if (TimeUtils.isDateToday(message.getTimestamp())) {
            mDate.setText(itemView.getContext().getString(R.string.today));
        } else {
            // it's not today. shows the week of day label
            mDate.setText(TimeUtils.getFormattedTimestamp(itemView.getContext(), message.getTimestamp()));
        }

        // hides or shows the date label
        if (previousMessageDate != null && position > 0) {
            if (TimeUtils.getDayOfWeek(messageDate)
                    .equals(TimeUtils.getDayOfWeek(previousMessageDate))) {
                mDate.setVisibility(View.GONE);
            } else {
                mDate.setVisibility(View.VISIBLE);
            }
        } else {
            mDate.setVisibility(View.VISIBLE);
        }
    }

    private void setBubble() {
        // set bubble color and background
        Drawable drawable = ImageUtils.changeDrawableColor(itemView.getContext(),
                R.color.background_bubble_recipient, R.drawable.balloon_recipient);
       mBackgroundBubble.setBackground(drawable);
    }

    private void setSenderDisplayName(Message message) {

        if (message.isGroupChannel()) {
            mSenderDisplayName.setVisibility(View.VISIBLE);

            String senderDisplayName = StringUtils.isValid(message.getSenderFullname()) ?
                    message.getSenderFullname() : message.getSender();
            mSenderDisplayName.setText(senderDisplayName);
        } else if (message.isDirectChannel()) {
            mSenderDisplayName.setVisibility(View.GONE);
        } else {
            // default case: consider it as direct message
            mSenderDisplayName.setVisibility(View.GONE);
        }
    }

    private void setOnMessageClickListener(final OnMessageClickListener callback) {

        mMessage.setMovementMethod(new TextViewLinkHandler() {
            @Override
            public void onLinkClick(ClickableSpan clickableSpan) {
                callback.onMessageLinkClick(mMessage, clickableSpan);
            }
        });
    }
}