// adapters/MentorAdapter.java
package com.example.activesenior.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activesenior.R;
import com.example.activesenior.models.Mentor;

import java.util.List;

public class MentorAdapter extends RecyclerView.Adapter<MentorAdapter.MentorViewHolder> {

    private List<Mentor> mentorList;

    public MentorAdapter(List<Mentor> mentorList) {
        this.mentorList = mentorList;
    }

    @Override
    public MentorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mentor, parent, false);
        return new MentorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MentorViewHolder holder, int position) {
        Mentor mentor = mentorList.get(position);
        holder.nameTextView.setText(mentor.getName());
        holder.fieldTextView.setText(mentor.getField());
        holder.emailTextView.setText(mentor.getEmail());
    }

    @Override
    public int getItemCount() {
        return mentorList.size();
    }

    public static class MentorViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, fieldTextView, emailTextView;

        public MentorViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            fieldTextView = itemView.findViewById(R.id.fieldTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
        }
    }
}
