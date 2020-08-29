package com.patrickarungari.messenger.ui.messages.adapters;

import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.style.ClickableSpan;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionValues;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.patrickarungari.messenger.core.messages.models.Message;
import com.patrickarungari.messenger.ui.messages.activities.ImageDetailsActivity;
import com.patrickarungari.messenger.ui.messages.activities.MessageListActivity;
import com.patrickarungari.messenger.ui.messages.listeners.OnMessageClickListener;
import com.patrickarungari.messenger.utils.TimeUtils;
import com.patrickarungari.messenger.utils.image.ImageUtils;
import com.patrickarungari.messenger.utils.views.TextViewLinkHandler;
import com.vanniktech.emoji.EmojiTextView;

import com.patrickarungari.messenger.R;

import com.patrickarungari.messenger.ui.ChatUI;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by stefano on 25/11/2016.
 */
class SenderViewHolder extends RecyclerView.ViewHolder {

    private final EmojiTextView mMessage;
    private final TextView mDate,time;

    //private final CardView card;
    private final TextView mTimestamp;
    private final ConstraintLayout mBackgroundBubble;
    private final ConstraintLayout mPreview; // Resolve Issue #32
    private final ImageView mMessageStatus,preview;
    private final ProgressBar mProgressBar;   // Resolve Issue #52
    private final LinearLayout msg_content;

    SenderViewHolder(View itemView) {
        super(itemView);
       // card = itemView.findViewById(R.id.card);
        mMessage = (EmojiTextView) itemView.findViewById(R.id.message);
        time = itemView.findViewById(R.id.time);
        mDate = (TextView) itemView.findViewById(R.id.date);
        mTimestamp = (TextView) itemView.findViewById(R.id.timestamp);
        mPreview = (ConstraintLayout) itemView.findViewById(R.id.box_image); // Resolve Issue #32
        mBackgroundBubble = itemView.findViewById(R.id.message_group);
        msg_content = itemView.findViewById(R.id.msg_content);
        preview = itemView.findViewById(R.id.preview);
        mMessageStatus = (ImageView) itemView.findViewById(R.id.status);
        mProgressBar = (ProgressBar) itemView.findViewById(R.id.progress);   // Resolve Issue #52

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void bind(RecyclerView.ViewHolder holder, final Message previousMessage, final Message message,
              int position, OnMessageClickListener onMessageClickListener) {
       /* ViewCompat.setTransitionName(holder.itemView.findViewById(R.id.preview), "transition"+holder.getAdapterPosition());
        ((MessageListActivity)itemView.getContext()).getWindow().setExitTransition(TransitionInflater.from(itemView.getContext())
                .inflateTransition(R.transition.grid_exit_transition));

        // A similar mapping is set at the ImagePagerFragment with a setEnterSharedElementCallback.
        ((MessageListActivity)itemView.getContext()).setExitSharedElementCallback(
                new SharedElementCallback() {
                    @Override
                    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                        // Locate the ViewHolder for the clicked position.
                        // Map the first shared element name to the child ImageView.
                        sharedElements
                                .put(names.get(0), itemView.findViewById(R.id.preview));
                    }
                });*/
        switch (message.getType()) {
            case Message.TYPE_IMAGE:
                mMessage.setVisibility(View.GONE);
                mPreview.setVisibility(View.VISIBLE);
                msg_content.setVisibility(View.GONE);
                setImagePreview(message);
                break;
            case Message.TYPE_FILE:
                mMessage.setVisibility(View.GONE);
                mPreview.setVisibility(View.VISIBLE);
                setFilePreview(message);
                break;
            case Message.TYPE_TEXT:
                mProgressBar.setVisibility(View.GONE);   // Resolve Issue #52
                mMessage.setVisibility(View.VISIBLE);
                msg_content.setVisibility(View.VISIBLE);
                mPreview.setVisibility(View.GONE);
                setMessage(message);
                break;
        }

        setBubble();

        setDate(previousMessage, message, position);

        setTimestamp(message);

        // message status icon
        setStatus(message.getStatus());

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
    private void setImagePreview(final Message message) {

        // Resolve Issue #52
        mProgressBar.setVisibility(View.VISIBLE);

        Glide.with(itemView.getContext())
                .load(getImageUrl(message))
                .placeholder(ContextCompat.getDrawable(itemView.getContext(),R.drawable.ic_image_holder))
                .listener(new RequestListener<Drawable>() {
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
        mMessage.setText(message.getText());

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
        Drawable bubble = ImageUtils.changeDrawableColor(itemView.getContext(),
                R.color.whatsapp, R.drawable.balloon_sender);
        mBackgroundBubble.setBackground(bubble);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setStatus(long status) {

        Drawable drawableStatus;
        boolean visible;

        if (status == Message.STATUS_SENDING) {
            drawableStatus = itemView.getContext()
                    .getResources()
                    .getDrawable(R.drawable.ic_message_sending_16dp);
            visible = true;
            drawableStatus.setTint(ContextCompat.getColor(itemView.getContext(), R.color.grey_600));
        } else if (status == Message.STATUS_SENT) {
            drawableStatus = itemView.getContext()
                    .getResources()
                    .getDrawable(R.drawable.ic_message_sent_16dp);
            drawableStatus.setTint(ContextCompat.getColor(itemView.getContext(), R.color.grey_600));
            visible = true;
        } else if (status == Message.STATUS_RETURN_RECEIPT) {
            drawableStatus = itemView.getContext()
                    .getResources()
                    .getDrawable(R.drawable.ic_message_receipt_16dp);
            drawableStatus.setTint(ContextCompat.getColor(itemView.getContext(), R.color.blue_300));
            visible = true;
        } else {
            // status not valid or undefined
            drawableStatus = null;
            visible = false;
        }

        if (drawableStatus != null && visible) {
            mMessageStatus.setBackground(drawableStatus);
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