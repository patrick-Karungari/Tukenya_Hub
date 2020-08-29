package com.patrickarungari.messenger.ui.chat_groups.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.patrickarungari.messenger.core.ChatManager;
import com.patrickarungari.messenger.core.messages.models.Message;
import com.patrickarungari.messenger.core.users.models.IChatUser;

import com.patrickarungari.messenger.R;

import com.patrickarungari.messenger.core.chat_groups.models.ChatGroup;
import com.patrickarungari.messenger.core.chat_groups.syncronizers.GroupsSyncronizer;
import com.patrickarungari.messenger.ui.messages.activities.MessageListActivity;
import com.patrickarungari.messenger.ui.users.activities.PublicProfileActivity;

import static com.patrickarungari.messenger.ui.ChatUI.BUNDLE_CHANNEL_TYPE;
import static com.patrickarungari.messenger.ui.ChatUI.BUNDLE_RECIPIENT;

/**
 * Created by frontiere21 on 25/11/16.
 */
public class BottomSheetGroupAdminPanelMember extends BottomSheetDialogFragment {
    public static final String TAG = BottomSheetGroupAdminPanelMember.class.getName();
    private static final String PRIVATE_BUNDLE_GROUP_MEMBER = "PRIVATE_BUNDLE_GROUP_MEMBER";
    private static final String PRIVATE_BUNDLE_GROUP = "PRIVATE_BUNDLE_GROUP";
    private static String stringActivity;
    private IChatUser groupMember;
    private ChatGroup chatGroup;
    private IChatUser loggedUser;
    private static int layout;
    private Button removeMember;

    private GroupsSyncronizer groupsSyncronizer;

    public static BottomSheetGroupAdminPanelMember newInstance(IChatUser groupMember, ChatGroup chatGroup) {
        Log.i(TAG, "newInstance");
        layout = R.layout.fragment_bottom_sheet_group_admin_panel_member;
        BottomSheetGroupAdminPanelMember f =
                new BottomSheetGroupAdminPanelMember();
        Bundle args = new Bundle();
        args.putSerializable(PRIVATE_BUNDLE_GROUP_MEMBER, groupMember);
        args.putSerializable(PRIVATE_BUNDLE_GROUP, chatGroup);
        f.setArguments(args);
        return f;
    }
    public static BottomSheetGroupAdminPanelMember newInstance(int layouts, String string){
        layout = layouts;
        stringActivity = string;
        return new BottomSheetGroupAdminPanelMember();
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (layout == R.layout.fragment_bottom_sheet_group_admin_panel_member){
            // retrieves the username from newInstance params
            groupMember = (IChatUser) getArguments().getSerializable(PRIVATE_BUNDLE_GROUP_MEMBER);

            // retrieves the groupId from newInstance params
            chatGroup = (ChatGroup) getArguments().getSerializable(PRIVATE_BUNDLE_GROUP);

            // retrieves the logged userId from chant configuration
            loggedUser = ChatManager.getInstance().getLoggedUser();

            groupsSyncronizer = ChatManager.getInstance().getGroupsSyncronizer();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater
                .inflate(layout,
                        container, false);
        if (layout == R.layout.fragment_bottom_sheet_group_admin_panel_member){
            registerViews(rootView);
            initRemoveMemberButton();
        }else{
            registerView(rootView);
        }


        return rootView;
    }

    private void registerView(View rootView) {
        TextView title = rootView.findViewById(R.id.title);
        ImageView gallery = rootView.findViewById(R.id.gallery);
        ImageView camera = rootView.findViewById(R.id.camera);
        gallery.setOnClickListener(v -> {

        });
        camera.setOnClickListener(v -> {

        });
        if (stringActivity != null && stringActivity.equals("group")){
            title.setText("Set group icon");
        }
    }


    private void registerViews(View rootView) {
        Log.i(TAG, "registerViews");

        // contact username
        TextView username = (TextView) rootView.findViewById(R.id.username);
        username.setText(groupMember.getFullName());

        // remove member
        removeMember = (Button) rootView.findViewById(R.id.btn_remove_member);
        removeMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRemoveMemberAlertDialog();
            }
        });

        // see profile
        Button seeProfile = (Button) rootView.findViewById(R.id.btn_see_profile);
        if (!groupMember.equals(loggedUser)) {
            seeProfile.setVisibility(View.VISIBLE);
            seeProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity()
                            .getApplicationContext(), PublicProfileActivity.class);
                    intent.putExtra(BUNDLE_RECIPIENT, groupMember);
                    startActivity(intent);

                    // dismiss the bottomsheet
                    getDialog().dismiss();
                }
            });
        } else {
            seeProfile.setVisibility(View.GONE);
        }

        // send direct message
        Button sendMessage = (Button) rootView.findViewById(R.id.btn_send_message);
        if (!groupMember.equals(loggedUser)) {
            sendMessage.setVisibility(View.VISIBLE);
            sendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), MessageListActivity.class);
                    intent.putExtra(BUNDLE_RECIPIENT, groupMember);
                    intent.putExtra(BUNDLE_CHANNEL_TYPE, Message.DIRECT_CHANNEL_TYPE);
                    getActivity().startActivity(intent);

                    // dismiss the bottomsheet
                    getDialog().dismiss();
                }
            });
        } else {
            sendMessage.setVisibility(View.GONE);
        }

        // cancel
        Button cancel = (Button) rootView.findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // dismiss the bottomsheet
                getDialog().dismiss();
            }
        });
    }

    private void initRemoveMemberButton() {
        Log.d(TAG, "initRemoveMemberButton");

        // check logged user is the admin of the group
        if (chatGroup.getOwner().equals(loggedUser.getId()) &&
                chatGroup.getMembersList().contains(loggedUser)) {
            // the clicked user is an admin
            if (groupMember.getId().equals(chatGroup.getOwner())) {
                // cannot delete and admin
                removeMember.setVisibility(View.GONE);
            } else {
                removeMember.setVisibility(View.VISIBLE);
            }
        } else {
            removeMember.setVisibility(View.GONE);
        }

        // allows the logged user to leave the chatGroup
        if (groupMember.equals(loggedUser)) {
            removeMember.setText(getString(
                    R.string.bottom_sheet_group_admin_panel_member_leave_group_btn_label));
            removeMember.setVisibility(View.VISIBLE);
        }
    }

    private void showRemoveMemberAlertDialog() {
        Log.d(TAG, "showRemoveMemberAlertDialog");

        String message, positiveClickMessage;

        // allows the logged user to leave the chatGroup
        if (groupMember.equals(loggedUser)) {
            message = getString(R.string.bottom_sheet_group_admin_panel_member_leave_group_alert_message);
            positiveClickMessage = getString(R.string.bottom_sheet_group_admin_panel_member_leave_group_alert_positive_click);
        } else {
            message = getString(R.string.bottom_sheet_group_admin_panel_member_remove_member_alert_message, groupMember.getFullName());
            positiveClickMessage = getString(R.string.bottom_sheet_group_admin_panel_member_remove_member_alert_positive_click);
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.bottom_sheet_group_admin_panel_member_remove_member_alert_title))
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(positiveClickMessage, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        groupsSyncronizer.removeMemberFromChatGroup(chatGroup.getGroupId(), groupMember);

                        // dismiss the dialog
                        dialog.dismiss();

                        // dismiss the bottomsheet
                        getDialog().dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.bottom_sheet_group_admin_panel_member_remove_member_alert_negative_click), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // dismiss the dialog
                        dialogInterface.dismiss();

                        // dismiss the bottomsheet
                        getDialog().dismiss();
                    }
                }).show();
    }
}