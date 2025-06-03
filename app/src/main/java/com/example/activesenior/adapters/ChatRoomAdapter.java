package com.example.activesenior.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activesenior.R;
import com.example.activesenior.models.ChatRoom;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private List<ChatRoom> chatRoomList;
    private OnChatRoomClickListener listener;

    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatRoom chatRoom);
    }

    public ChatRoomAdapter(List<ChatRoom> chatRoomList, OnChatRoomClickListener listener) {
        this.chatRoomList = chatRoomList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        holder.bind(chatRoomList.get(position));
    }

    @Override
    public int getItemCount() {
        return chatRoomList.size();
    }

    class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView participantNameText;
        TextView lastMessageText;
        TextView lastTimestampText;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            participantNameText = itemView.findViewById(R.id.participantNameText);
            lastMessageText = itemView.findViewById(R.id.lastMessageText);
            lastTimestampText = itemView.findViewById(R.id.lastTimestampText);
        }

        void bind(ChatRoom chatRoom) {
            participantNameText.setText(chatRoom.getParticipantName());

            // 메시지 텍스트 처리
            String lastMessage = chatRoom.getLastMessage();
            if (lastMessage == null || lastMessage.trim().isEmpty()) {
                lastMessageText.setText("(메시지 없음)");
            } else {
                lastMessageText.setText(lastMessage);
            }

            // 날짜 포맷 처리 (오늘인 경우 시간만 표시)
            Date timestamp = chatRoom.getLastTimestamp();
            if (timestamp != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                SimpleDateFormat dateFormat = new SimpleDateFormat("M월 d일", Locale.getDefault());

                // 오늘 날짜인지 비교
                Date now = new Date();
                SimpleDateFormat dayCheckFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                boolean isToday = dayCheckFormat.format(now).equals(dayCheckFormat.format(timestamp));

                String formatted = isToday ? timeFormat.format(timestamp) : dateFormat.format(timestamp);
                lastTimestampText.setText(formatted);
            } else {
                lastTimestampText.setText("");
            }

            // 채팅방 클릭 리스너
            itemView.setOnClickListener(v -> listener.onChatRoomClick(chatRoom));
        }

    }
}
