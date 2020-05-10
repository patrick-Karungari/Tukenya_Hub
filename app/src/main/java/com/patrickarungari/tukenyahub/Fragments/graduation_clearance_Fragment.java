package com.patrickarungari.tukenyahub.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.patrickarungari.tukenyahub.R;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class graduation_clearance_Fragment extends Fragment {


    public graduation_clearance_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View  view= inflater.inflate(R.layout.fragment_graduation_clearance_, container, false);
        initUi(view);
        return view;
    }

    private void initUi(View view) {
        Spinner spinner4 = view.findViewById(R.id.spinner4);
        Spinner spinner5 = view.findViewById(R.id.spinner5);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                Objects.requireNonNull(getActivity()),
                R.layout.custom_spinner,
                getResources().getStringArray(R.array.year_of_graduation));
        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(
                Objects.requireNonNull(getActivity()),
                R.layout.custom_spinner,
                getResources().getStringArray(R.array.sem_of_study));

        arrayAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        spinner4.setAdapter(arrayAdapter);
        spinner5.setAdapter(arrayAdapter2);

    }

}
