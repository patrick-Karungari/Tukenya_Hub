package com.kcbbankgroup.tukenyahub.Fragments;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.kcbbankgroup.tukenyahub.R;

import java.util.Calendar;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class DeferFragment extends Fragment {
    private DatePickerDialog datePickerDialog;
    private EditText datePicker;
    private Button button;
    private Spinner spinner;
    private EditText cardView;

    public DeferFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.defer_fragment, container, false);
        Thread thread = new Thread(() -> Objects.requireNonNull(getActivity()).runOnUiThread(() -> alertBuilder(view)));
        thread.start();
        initUi(view);

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void alertBuilder(View view) {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        datePickerDialog = new DatePickerDialog(Objects.requireNonNull(getContext()), (view1, mYear, mMonth, mDay) -> datePicker.setText(mDay + "/" + mMonth + "/" + mYear), year, month, day);
        datePicker.setCursorVisible(false);
        Spinner spinner2 = view.findViewById(R.id.spinner2);
        Spinner spinner3 = view.findViewById(R.id.spinner3);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                Objects.requireNonNull(getActivity()),
                R.layout.custom_spinner,
                getResources().getStringArray(R.array.term_of_deferment));
        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(
                Objects.requireNonNull(getActivity()),
                R.layout.custom_spinner,
                getResources().getStringArray(R.array.year_of_study));
        ArrayAdapter<String> arrayAdapter3 = new ArrayAdapter<>(
                Objects.requireNonNull(getActivity()),
                R.layout.custom_spinner,
                getResources().getStringArray(R.array.sem_of_study));

        arrayAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        spinner.setAdapter(arrayAdapter);
        spinner2.setAdapter(arrayAdapter2);
        spinner3.setAdapter(arrayAdapter3);
    }

    private void initUi(View view) {
        button = view.findViewById(R.id.button2);
        spinner = view.findViewById(R.id.spinner);
        datePicker = view.findViewById(R.id.datePicker);
        cardView = view.findViewById(R.id.quoteTextArea2);
        datePicker.setOnClickListener(v -> {
            datePicker.setShowSoftInputOnFocus(false);
            datePickerDialog.show();
        });

        datePicker.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    datePicker.setShowSoftInputOnFocus(false);
                    datePickerDialog.show();
                }
            }
        });
    }

}
