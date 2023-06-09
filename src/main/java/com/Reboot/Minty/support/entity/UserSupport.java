package com.Reboot.Minty.support.entity;


import com.Reboot.Minty.support.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name="support")
@NoArgsConstructor
@Entity
@Getter
@Setter
public class UserSupport extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column
    private String name;

    @Column
    private String nickname;

    @Column
    private String verifyReply;

    @Column
    private Long fileId;

    @Column
    private String fileName;

    @Column
    private Long userId;

    @Builder
    public UserSupport(Long id, String title, String name, String content, String verifyReply, String nickname
                        , Long fileId, String fileName, Long userId) {
        this.id = id;
        this.title = title;
        this.name = name;
        this.content = content;
        this.verifyReply = verifyReply;
        this.nickname = nickname;
        this.fileId = fileId;
        this.fileName = fileName;
        this.userId = userId;

    }

    public void doneReply() {
        this.verifyReply = "답변 완료";
    }

    public void undoReply() {
        this.verifyReply = "처리중";
    }
}
