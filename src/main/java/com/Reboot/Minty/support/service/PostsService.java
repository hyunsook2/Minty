package com.Reboot.Minty.support.service;

import com.Reboot.Minty.member.constant.Role;
import com.Reboot.Minty.member.repository.UserRepository;
import com.Reboot.Minty.support.dto.ReplyDto;
import com.Reboot.Minty.support.dto.UserSupportDto;
import com.Reboot.Minty.support.entity.ReplyEntity;
import com.Reboot.Minty.support.entity.UserSupport;
import com.Reboot.Minty.support.repository.PostsRepository;
import com.Reboot.Minty.support.repository.ReplyRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PostsService {
    @Autowired
    PostsRepository postsRepository;
    @Autowired
    ReplyRepository replyRepository;
    @Autowired
    FileService fileService;
    private final UserRepository userRepository;

    public void userSavePosts(UserSupportDto userSupportDto) {
        postsRepository.save(userSupportDto.toEntity()).getId();
    }

    public void adminSavePosts(ReplyEntity replyEntity){

        replyRepository.save(replyEntity);
    }

    public UserSupport getUserSupport(Long id){
        return postsRepository.findById(id).orElseThrow(EntityExistsException::new);
    }

    public UserSupportDto getPost(Long id) {
        Optional<UserSupport> userSupportOptional = postsRepository.findById(id);
        UserSupport userSupport = userSupportOptional.get();

        UserSupportDto userSupportDto = UserSupportDto.builder()

                .title(userSupport.getTitle())
                .name(userSupport.getName())
                .content(userSupport.getContent())
                .nickname(userSupport.getNickname())
                .fileId(userSupport.getFileId())
                .fileName(userSupport.getFileName())
                .verify_reply(userSupport.getVerifyReply())
                .userId(userSupport.getUserId())
                .createdDate(userSupport.getCreatedDate())
                .modifiedDate((userSupport.getModifiedDate()))
                .build();


        return userSupportDto;
    }

    public ReplyDto getReply(Long supportId) {
        Optional<ReplyEntity> replyEntityOptional = replyRepository.findByUserSupportId(supportId);

        if (replyEntityOptional.isPresent()) {
            ReplyEntity replyEntity = replyEntityOptional.get();
            ReplyDto replyDto = ReplyDto.builder()
                    .replyTitle(replyEntity.getReplyTitle())
                    .replyContent(replyEntity.getReplyContent())
                    .nickname(replyEntity.getNickname())
                    .createdDate(replyEntity.getCreatedDate())
                    .modifiedDate(replyEntity.getModifiedDate())
                    .build();
            return replyDto;
        } else {
            return null; // Return null if reply is not found
        }
    }

//    public Page<UserSupport> getPostList(Pageable pageable, Role userRole, Long userId, String searchBy, String keyword) {
//        Sort sort = Sort.by(Sort.Direction.DESC, "id");
//        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
//
//
//        if (userRole == Role.ADMIN) {
//            if (searchBy != null && searchBy.equals("이름")) {
//                return postsRepository.findAllByNameContainingIgnoreCase(keyword, pageable);
//            } else if (searchBy != null && searchBy.equals("닉네임")) {
//                return postsRepository.findAllByNicknameContainingIgnoreCase(keyword, pageable);
//            } else {
//                return postsRepository.findAll(pageable);
//            }
//        } else {
//            if (searchBy != null && searchBy.equals("이름")) {
//                return postsRepository.findByUserIdAndNameContainingIgnoreCase(userId, keyword, pageable);
//            } else if (searchBy != null && searchBy.equals("닉네임")) {
//                return postsRepository.findByUserIdAndNicknameContainingIgnoreCase(userId, keyword, pageable);
//            }
//            return postsRepository.findByUserId(userId, pageable);
//        }
//    }

    public Page<UserSupport> getPostListByVerifyStatus(Pageable pageable, Role userRole, Long userId, String searchBy, String keyword, String verifyStatus) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        if (userRole == Role.ADMIN) {
            if (searchBy != null && searchBy.equals("이름")) {
                return postsRepository.findAllByNameContainingIgnoreCaseAndVerifyReply(keyword, verifyStatus, pageable);
            } else if (searchBy != null && searchBy.equals("닉네임")) {
                return postsRepository.findAllByNicknameContainingIgnoreCaseAndVerifyReply(keyword, verifyStatus, pageable);
            } else {
                return postsRepository.findAllByVerifyReply(verifyStatus, pageable);
            }
        } else {
            if (searchBy != null && searchBy.equals("이름")) {
                return postsRepository.findByUserIdAndNameContainingIgnoreCaseAndVerifyReply(userId, keyword, verifyStatus, pageable);
            } else if (searchBy != null && searchBy.equals("닉네임")) {
                return postsRepository.findByUserIdAndNicknameContainingIgnoreCaseAndVerifyReply(userId, keyword, verifyStatus, pageable);
            }
            return postsRepository.findByUserIdAndVerifyReply(userId, verifyStatus, pageable);
        }
    }

//    public List<String> getAllVerifyReplies() {
//        List<UserSupport> userSupportList = postsRepository.findAll();
//        List<String> verifyReplies = new ArrayList<>();
//
//        for (UserSupport userSupport : userSupportList) {
//            verifyReplies.add(userSupport.getVerify_reply());
//        }
//
//        return verifyReplies;
//    }

    public void updatePost(Long id , UserSupportDto updateDto){
        UserSupport post = postsRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));
        post.setTitle(updateDto.getTitle());
        post.setContent(updateDto.getContent());
        post.setFileId(updateDto.getFileId());
        post.setFileName(updateDto.getFileName());
        postsRepository.save(post);
    }

    public void updateVerfyReply(Long id, UserSupportDto userSupportDto) {
        UserSupport userSupport = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));
        userSupport.setVerifyReply("처리완료");
        postsRepository.save(userSupport);
    }

    public void updateAdminPost(Long supportId , ReplyDto updateReplyDto){
        ReplyEntity reply = replyRepository.findByUserSupportId(supportId)
                .orElseThrow(() -> new IllegalArgumentException("Reply not found for support_Id: " + supportId));
        reply.setReplyTitle(updateReplyDto.getReplyTitle());
        reply.setReplyContent(updateReplyDto.getReplyContent());
        replyRepository.save(reply);
    }

    public void updateAdminVerfyReply(Long id, UserSupportDto userSupportDto) {
        UserSupport userSupport = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));
        userSupport.setVerifyReply("처리중");
        postsRepository.save(userSupport);
    }

    @Transactional
    public void deletePost(Long id){
        UserSupport userSupport = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("지원 게시물을 찾을 수 없습니다. id: " + id));

        replyRepository.deleteByUserSupportId(userSupport.getId());
        postsRepository.deleteById(id);
    }
    @Transactional
    public void deleteAdminPost(Long id){
        UserSupport userSupport = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("지원 게시물을 찾을 수 없습니다. id: " + id));

        replyRepository.deleteByUserSupportId(userSupport.getId());
    }
}
