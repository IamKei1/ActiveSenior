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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private List<User> userList;
    private OnUserClickListener listener;

    public UserAdapter(List<User> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    public void setUserList(List<User> list) {
        this.userList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.nameTextView.setText(user.getName());
        holder.roleTextView.setText("역할: " + user.getRole());
        holder.distanceTextView.setText(String.format("거리: %.0f m", user.getDistance()));
        holder.requestButton.setOnClickListener(v -> listener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, roleTextView, distanceTextView;
        Button requestButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.userNameTextView);
            roleTextView = itemView.findViewById(R.id.userRoleTextView);
            distanceTextView = itemView.findViewById(R.id.userDistanceTextView);
            requestButton = itemView.findViewById(R.id.requestButton);

        }
    }
}
