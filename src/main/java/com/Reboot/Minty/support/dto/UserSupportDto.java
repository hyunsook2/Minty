package com.Reboot.Minty.support.dto;

import com.Reboot.Minty.support.entity.UserSupport;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserSupportDto {

    @NotEmpty(message = "필수")
    private String title;

    @NotEmpty(message = "필수")
    private String content;
    private String name;
    private String verify_reply;
    private String nickname;
    private Long fileId;
    private String fileName;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private Long userId;

    public UserSupport toEntity() {
        return UserSupport.builder()

                .title(title)
                .name(name)
                .content(content)
                .nickname(nickname)
                .userId(userId)
                .fileId(fileId)
                .fileName(fileName)
                .verifyReply(verify_reply)
                .build();
    }

    @Builder
    public UserSupportDto( String title, String content, String name,
                    String verify_reply, String nickname, LocalDateTime createdDate, LocalDateTime modifiedDate
                    ,Long fileId , String fileName, Long userId) {

        this.title = title;
        this.content = content;
        this.name = name;
        this.nickname = nickname;
        this.verify_reply = verify_reply;
        this.fileId = fileId;
        this.fileName = fileName;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.userId = userId;
    }
}
