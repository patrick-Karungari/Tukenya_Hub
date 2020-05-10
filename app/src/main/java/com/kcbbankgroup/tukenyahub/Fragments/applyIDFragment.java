package com.kcbbankgroup.tukenyahub.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.kcbbankgroup.tukenyahub.R;


public class applyIDFragment extends Fragment {


    public applyIDFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        container.findViewById(R.id.grid).setVisibility(View.GONE);
        return inflater.inflate(R.layout.apply_id_fragment, container, false);
    }

}
