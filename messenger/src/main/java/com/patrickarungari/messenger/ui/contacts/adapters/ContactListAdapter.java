package com.patrickarungari.messenger.ui.contacts.adapters;


import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.patrickarungari.messenger.R;
import com.patrickarungari.messenger.core.messages.models.Message;
import com.patrickarungari.messenger.core.users.models.IChatUser;
import com.patrickarungari.messenger.ui.ChatUI;
import com.patrickarungari.messenger.ui.contacts.listeners.OnContactClickListener;
import com.patrickarungari.messenger.ui.messages.activities.MessageListActivity;
import com.patrickarungari.messenger.utils.StringUtils;
import com.patrickarungari.messenger.utils.image.CropCircleTransformation;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//import static com.patrickarungari.messenger.ui.contacts.activites.ContactListActivity.TAG_CONTACTS_SEARCH;

/**
 * Created by stefanodp91 on 05/01/17.
 */

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder>
        implements Filterable {

    private List<IChatUser> contactList;

    private List<IChatUser> contactListFiltered;

    private OnContactClickListener onContactClickListener;

    public ContactListAdapter(List<IChatUser> contactList) {
        this.contactList = contactList;
        this.contactListFiltered = contactList;
    }

    public void setList(List<IChatUser> list) {
        this.contactList = list;
    }

    public void setOnContactClickListener(OnContactClickListener onContactClickListener) {
        this.onContactClickListener = onContactClickListener;
    }

    public OnContactClickListener getOnContactClickListener() {
        return onContactClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_contact_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactListAdapter.ViewHolder holder, final int position) {
        final IChatUser contact = contactListFiltered.get(position);
        if (StringUtils.isValid(contact.getFullName()))
            holder.mContactFullName.setText(contact.getFullName());
        else holder.mContactFullName.setText(contact.getId());
        holder.mContactUsername.setText(contact.getRegNum());

        Glide.with(holder.itemView.getContext())
                .load(contact.getProfilePictureUrl())
                .placeholder(R.drawable.ic_person_avatar)
                .transform(new CropCircleTransformation(holder.itemView.getContext()))
                .into(holder.mProfilePicture);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getOnContactClickListener() != null) {
                    getOnContactClickListener().onContactClicked(contact, position);
                }

            }
        });
    }



    @Override
    public int getItemCount() {
        return contactListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
//                Log.d(TAG_CONTACTS_SEARCH, "ContactListAdapter.getFilter.performFiltering: " +
//                        "charString == " + charString);
                if (charString.isEmpty()) {
                    contactListFiltered = contactList;
                } else {
                    List<IChatUser> filteredList = new CopyOnWriteArrayList<>();
                    for (IChatUser row : contactList) {
                        // search on the user fullname
                        if (row.getFullName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    contactListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = contactListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                contactListFiltered = (CopyOnWriteArrayList<IChatUser>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mContactFullName;
        private final TextView mContactUsername;
        private final ImageView mProfilePicture;

        ViewHolder(View itemView) {
            super(itemView);
            mContactFullName = (TextView) itemView.findViewById(R.id.fullname);
            mContactUsername = (TextView) itemView.findViewById(R.id.username);
            mProfilePicture = (ImageView) itemView.findViewById(R.id.profile_picture);
        }
    }
}