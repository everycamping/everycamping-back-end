package com.zerobase.everycampingbackend.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zerobase.everycampingbackend.domain.chat.entity.ChatRoom;
import com.zerobase.everycampingbackend.domain.chat.type.ChatRoomStatus;
import com.zerobase.everycampingbackend.domain.chat.type.UserType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {

    private Long chatRoomId;
    private String requesterEmail;
    private UserType requesterUserType;
    private String requesteeEmail;
    private UserType requesteeUserType;
    private ChatRoomStatus chatRoomStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public static ChatRoomDto from(ChatRoom chatRoom) {
        return ChatRoomDto.builder()
                .chatRoomId(chatRoom.getId())
                .requesterEmail(chatRoom.getRequesterEmail())
                .requesterUserType(chatRoom.getRequesterUserType())
                .requesteeEmail(chatRoom.getRequesteeEmail())
                .requesteeUserType(chatRoom.getRequesteeUserType())
                .chatRoomStatus(chatRoom.getChatRoomStatus())
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }
}
