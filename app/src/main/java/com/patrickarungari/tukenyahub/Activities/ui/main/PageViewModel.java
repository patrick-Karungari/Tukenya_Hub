package com.patrickarungari.tukenyahub.Activities.ui.main;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

public class PageViewModel extends ViewModel {
    int indicies;
    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();
    private LiveData<String> mText = Transformations.map(mIndex, new Function<Integer, String>() {
        @Override
        public String apply(Integer input) {
            String text = null;
            if (indicies == 1) {
                text = "Please Note. This is not the final result until approved by the Technical University of Kenya Senate";
            } else if (indicies == 2) {
                text = "Please Register for your examinations to be allowed in the examination room.";
            } else {
                text = "These are your final results approved by the senate.";
            }
            return text;
        }
    });

    public void setIndex(int index) {
        indicies = index;
        mIndex.setValue(index);
    }

    public LiveData<String> getText() {
        return mText;
    }
}