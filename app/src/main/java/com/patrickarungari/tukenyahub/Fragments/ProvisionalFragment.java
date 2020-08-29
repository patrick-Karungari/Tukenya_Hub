package com.patrickarungari.tukenyahub.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.patrickarungari.tukenyahub.Activities.ui.main.LauncherAdapter;
import com.patrickarungari.tukenyahub.Activities.ui.main.PageViewModel;
import com.patrickarungari.tukenyahub.R;

public class ProvisionalFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    PageViewModel pageViewModel;
    public ProvisionalFragment() {
        //Required empty public constructor
    }

    public static ProvisionalFragment newInstance(int index){
        ProvisionalFragment fragment = new ProvisionalFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_examinations, container, false);
        // Lookup the recyclerview in activity layout
        RecyclerView rvContacts = (RecyclerView) view.findViewById(R.id.recycler_view);
        LauncherAdapter adapter = new LauncherAdapter();
        // Attach the adapter to the recyclerview to populate items
        rvContacts.setAdapter(adapter);
        // Set layout manager to position the items
        rvContacts.setLayoutManager(new LinearLayoutManager(getActivity()));
        // That's all!
        final TextView textView = view.findViewById(R.id.section_label);
        pageViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return view;
    }
}
