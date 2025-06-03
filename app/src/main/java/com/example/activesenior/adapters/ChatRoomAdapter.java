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
    private String currentUid;

    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatRoom chatRoom);
    }


    public ChatRoomAdapter(List<ChatRoom> chatRoomList, String currentUid, OnChatRoomClickListener listener) {
        this.chatRoomList = chatRoomList;
        this.currentUid = currentUid;
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
            // 현재 사용자 UID에 따라 상대방 이름 선택
            String participantName;
            if (currentUid.equals(chatRoom.getParticipant1Id())) {
                participantName = chatRoom.getParticipant2Name();
            } else {
                participantName = chatRoom.getParticipant1Name();
            }
            participantNameText.setText(participantName != null ? participantName : "상대방");

            // 메시지 텍스트 처리
            String lastMessage = chatRoom.getLastMessage();
            lastMessageText.setText(
                    (lastMessage == null || lastMessage.trim().isEmpty()) ? "(메시지 없음)" : lastMessage
            );

            // 날짜 포맷 처리
            Date timestamp = chatRoom.getLastTimestamp();
            if (timestamp != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                SimpleDateFormat dateFormat = new SimpleDateFormat("M월 d일", Locale.getDefault());

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
