package com.patrickarungari.tukenyahub.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.fragment.app.Fragment;

import com.patrickarungari.tukenyahub.R;

import org.angmarch.views.NiceSpinner;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class graduation_clearance_Fragment extends Fragment {

    private ArrayAdapter<CharSequence> arrayAdapter, arrayAdapter2;

    public graduation_clearance_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_graduation_clearance_, container, false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                arrayAdapter = ArrayAdapter.createFromResource(requireActivity(),
                        R.array.term_of_deferment, android.R.layout.simple_spinner_item);
                arrayAdapter2 = ArrayAdapter.createFromResource(getActivity(),
                        R.array.term_of_deferment, android.R.layout.simple_spinner_item);
                graduation_clearance_Fragment.this.requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initUi(view);
                    }
                });
            }
        }).start();
        return view;
    }

    private void initUi(View view) {
        NiceSpinner spinner4 = view.findViewById(R.id.spinner4);
        NiceSpinner spinner5 = view.findViewById(R.id.spinner5);
        spinner4.setAdapter(arrayAdapter);
        spinner5.setAdapter(arrayAdapter2);

    }

}
