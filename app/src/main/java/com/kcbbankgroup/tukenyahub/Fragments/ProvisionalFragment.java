package com.kcbbankgroup.tukenyahub.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.kcbbankgroup.tukenyahub.R;

public class ProvisionalFragment extends Fragment {
    public ProvisionalFragment() {
        //Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_examinations, container, false);

        return view;
    }
}