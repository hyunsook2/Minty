package com.Reboot.Minty.support.repository;

import com.Reboot.Minty.support.entity.UserSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostsRepository extends JpaRepository<UserSupport, Long> {
    Page<UserSupport> findAllByNameContainingIgnoreCaseAndVerifyReply(String name, String verifyReply, Pageable pageable);

    Page<UserSupport> findAllByNicknameContainingIgnoreCaseAndVerifyReply(String nickname, String verifyReply, Pageable pageable);

    Page<UserSupport> findAllByVerifyReply(String verifyReply, Pageable pageable);

    Page<UserSupport> findByUserIdAndNameContainingIgnoreCaseAndVerifyReply(Long userId, String name, String verifyReply, Pageable pageable);

    Page<UserSupport> findByUserIdAndNicknameContainingIgnoreCaseAndVerifyReply(Long userId, String nickname, String verifyReply, Pageable pageable);

    Page<UserSupport> findByUserIdAndVerifyReply(Long userId, String verifyReply, Pageable pageable);
}
