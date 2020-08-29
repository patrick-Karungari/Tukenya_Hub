package com.patrickarungari.messenger.ui.messages.adapters;

import android.text.Html;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.patrickarungari.messenger.core.messages.models.Message;
import com.vanniktech.emoji.EmojiTextView;

import com.patrickarungari.messenger.R;

/**
 * Created by stefano on 25/11/2016.
 */

class SystemViewHolder extends RecyclerView.ViewHolder {

    private final EmojiTextView mMessage;

    SystemViewHolder(View itemView) {
        super(itemView);
        mMessage = (EmojiTextView) itemView.findViewById(R.id.message);

    }



    void bind(final Message message) {

        Log.d("TAG", "RecipientViewHolder");


        mMessage.setVisibility(View.VISIBLE);

        setMessage(message);
    }


    private void setMessage(Message message) {
        // set message text
        mMessage.setText(Html.fromHtml(message.getText()));
        }

}