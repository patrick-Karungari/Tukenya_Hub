package com.patrickarungari.tukenyahub.Activities.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.patrickarungari.tukenyahub.R;

import java.text.MessageFormat;

public class LauncherAdapter extends RecyclerView.Adapter<LauncherAdapter.ViewHolder> {


    @NonNull
    @Override
    public LauncherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.result_table, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    public LauncherAdapter() {

    }

    @Override
    public void onBindViewHolder(@NonNull LauncherAdapter.ViewHolder holder, int position) {

        TextView year = holder.yearTextView;
        year.setText(MessageFormat.format("YEAR {0}", position));
        TextView semester = holder.semesterTextView;
        semester.setText(MessageFormat.format("SEMESTER {0}", position));
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView yearTextView, semesterTextView;
        public Button downloadButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            yearTextView = (TextView) itemView.findViewById(R.id.year);
            semesterTextView = (TextView) itemView.findViewById(R.id.semester);
            downloadButton = (Button) itemView.findViewById(R.id.download_results);
        }
    }
}
