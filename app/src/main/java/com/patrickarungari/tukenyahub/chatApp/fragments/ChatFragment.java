package com.patrickarungari.tukenyahub.chatApp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.patrickarungari.messenger.ui.ChatUI;
import com.patrickarungari.tukenyahub.R;
import com.patrickarungari.tukenyahub.chatApp.ui.main.PageViewModel;

import java.util.List;

import static android.content.ContentValues.TAG;

/*import com.patrickarungari.messenger.ui.ChatUI;
import static com.patrickarungari.messenger.ui.chat_groups.fragments.BottomSheetGroupAdminPanelMember.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private ShimmerFrameLayout containerView;
    private PageViewModel pageViewModel;
    RecyclerView recyclerView;

    List<String> usersList;
    private Context context;
    private FloatingActionButton addNewConversation;


    public ChatFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(int index) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        setHasOptionsMenu(false); // disable fragment option menu
        addNewConversation = view.findViewById(R.id.new_chat);
        setAddNewConversationClickBehaviour();
        // starts the chat inside a container
        ChatUI.getInstance().openConversationsListFragment(getChildFragmentManager(), R.id.container);

        return view;
    }

    private void setAddNewConversationClickBehaviour() {
        Log.d(TAG, "ConversationListFragment.setAddNewConversationClickBehaviour");

        addNewConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ChatUI.getInstance().getOnNewConversationClickListener() != null) {
                    ChatUI.getInstance().getOnNewConversationClickListener().onNewConversationClicked();
                }
            }
        });
    }

}