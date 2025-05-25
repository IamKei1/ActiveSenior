package com.example.activesenior.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activesenior.R;
import com.example.activesenior.models.User;

import java.util.List;

public class MentorAdapter extends RecyclerView.Adapter<MentorAdapter.MentorViewHolder> {

    public interface OnMentorClickListener {
        void onMentorClick(User mentor);
    }

    private List<User> mentorList;
    private OnMentorClickListener listener;

    public MentorAdapter(List<User> mentorList, OnMentorClickListener listener) {
        this.mentorList = mentorList;
        this.listener = listener;
    }

    public void setMentorList(List<User> list) {
        this.mentorList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MentorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mentor, parent, false);
        return new MentorViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MentorViewHolder holder, int position) {
        User mentor = mentorList.get(position);
        holder.nameTextView.setText(mentor.getName());
        holder.roleTextView.setText("역할: " + mentor.getRole());

        holder.requestButton.setOnClickListener(v -> listener.onMentorClick(mentor));
        holder.distanceTextView.setText(String.format("거리: %.0f m", mentor.getDistance()));

    }

    @Override
    public int getItemCount() {
        return mentorList.size();
    }

    static class MentorViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, roleTextView;
        Button requestButton;
        TextView distanceTextView;

        public MentorViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.mentorNameTextView);
            roleTextView = itemView.findViewById(R.id.mentorRoleTextView);
            requestButton = itemView.findViewById(R.id.requestButton);
            distanceTextView = itemView.findViewById(R.id.mentorDistanceTextView);
        }
    }
}
