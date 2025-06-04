package com.example.activesenior.adapters;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activesenior.R;
import com.example.activesenior.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_DATE = 0;
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_OTHER = 2;

    private List<ChatMessage> chatMessages;
    private String currentUserId;

    public ChatAdapter(List<ChatMessage> chatMessages, String currentUserId) {
        this.chatMessages = chatMessages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);
        if (message.isDateHeader()) {
            return VIEW_TYPE_DATE;
        }
        return message.getSenderId().equals(currentUserId) ? VIEW_TYPE_USER : VIEW_TYPE_OTHER;
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_DATE) {
            View view = inflater.inflate(R.layout.item_date_header, parent, false);
            return new DateViewHolder(view);
        } else if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_user_message, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_ai_message, parent, false);
            return new OtherViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);

        if (holder instanceof DateViewHolder) {
            ((DateViewHolder) holder).bind(message);
        } else if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(message);
        } else if (holder instanceof OtherViewHolder) {
            ((OtherViewHolder) holder).bind(message);
        }


    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;

        public DateViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }

        public void bind(ChatMessage message) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 E요일", Locale.getDefault());
            dateTextView.setText(sdf.format(message.getTimestamp()));
        }
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userMessage, timeTextView, readStatusTextView; // ✅ 읽음 상태 표시 추가

        UserViewHolder(View itemView) {
            super(itemView);
            userMessage = itemView.findViewById(R.id.userMessage);
            timeTextView = itemView.findViewById(R.id.userTime);
            readStatusTextView = itemView.findViewById(R.id.readStatusTextView); // ✅ 레이아웃에 추가 필요
        }

        void bind(ChatMessage message) {
            userMessage.setText(message.getMessage());

            SimpleDateFormat sdf = new SimpleDateFormat("a h:mm", Locale.getDefault());
            timeTextView.setText(sdf.format(message.getTimestamp()));

            // ✅ 읽음 여부 표시
            if (message.getReadBy() != null && message.getReadBy().contains(message.getReceiverId())) {
                readStatusTextView.setText("읽음");
            } else {
                readStatusTextView.setText(" ");
            }

            readStatusTextView.setVisibility(View.VISIBLE);
        }
    }


    static class OtherViewHolder extends RecyclerView.ViewHolder {
        TextView aiMessage, timeTextView;

        OtherViewHolder(View itemView) {
            super(itemView);
            aiMessage = itemView.findViewById(R.id.aiMessage);
            timeTextView = itemView.findViewById(R.id.aiTime);
        }

        void bind(ChatMessage message) {
            aiMessage.setText(message.getMessage());
            SimpleDateFormat sdf = new SimpleDateFormat("a h:mm", Locale.getDefault());
            timeTextView.setText(sdf.format(message.getTimestamp()));
        }
    }
}
