package com.Reboot.Minty.support.controller;

import com.Reboot.Minty.member.constant.Role;
import com.Reboot.Minty.member.entity.User;
import com.Reboot.Minty.member.repository.UserRepository;
import com.Reboot.Minty.member.service.UserService;
import com.Reboot.Minty.support.domain.MD5Generator;
import com.Reboot.Minty.support.dto.FileDto;
import com.Reboot.Minty.support.dto.ReplyDto;
import com.Reboot.Minty.support.dto.UserSupportDto;
import com.Reboot.Minty.support.entity.ReplyEntity;
import com.Reboot.Minty.support.entity.UserSupport;
import com.Reboot.Minty.support.repository.PostsRepository;
import com.Reboot.Minty.support.service.FileService;
import com.Reboot.Minty.support.service.PostsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

@RequestMapping("/support")
@Controller
public class SupportController {

    @Autowired
    private UserService userService;
    @Autowired
    private PostsService postsService;
    @Autowired
    private FileService fileService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostsRepository postsRepository;


    //일반문의 게시판 이동
    @GetMapping("/supportBoard")
    public String postList(Model model, @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable, HttpServletRequest request
                            ,@RequestParam(required = false) String searchBy, @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String verifyStatus) {
        HttpSession session = request.getSession();
        String userEmail = (String) session.getAttribute("userEmail");
        User user = userService.getUserInfo(userEmail);
        Role userRole = user.getRole();
        Long userId = user.getId();
        Page<UserSupport> postList;
        postList = postsService.getPostListByVerifyStatus(pageable, userRole, userId, searchBy, keyword, verifyStatus);

        model.addAttribute("postList", postList);
        model.addAttribute("searchBy", searchBy);
        model.addAttribute("keyword", keyword);

        return "support/supportBoard";
    }

    // 문의 게시물 보기
    @GetMapping("/supportView/{id}")
    public String supportView(@PathVariable("id") Long id, Model model) {

        UserSupportDto postsDto = postsService.getPost(id);
        ReplyDto replyDto = postsService.getReply(id);

        model.addAttribute("replyDto", replyDto);
        model.addAttribute("postDto", postsDto);
        model.addAttribute("id", id);

        return "support/supportView";
    }

    //유저가 문의글 작성할때 가져오는 정보
    @GetMapping(value = "/new")
    public String supportForm(UserSupportDto userSupportDto, Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        String userEmail = (String) session.getAttribute("userEmail");
        User user = userService.getUserInfo(userEmail);
        userSupportDto.setNickname(user.getNickName());
        userSupportDto.setName(user.getName());
        userSupportDto.setUserId(user.getId());
        model.addAttribute("userSupportDto", userSupportDto);
        return "support/supportPage";
    }

    //유저가 문의글 포스팅할때
    @PostMapping(value = "/new")
    public String supportSaveForm(@RequestParam("file") MultipartFile files, @ModelAttribute UserSupportDto userSupportDto, HttpServletRequest request){
        try {
            HttpSession session = request.getSession();
            String userEmail = (String) session.getAttribute("userEmail");
            User user = userService.getUserInfo(userEmail);
            userSupportDto.setNickname(user.getNickName());
            userSupportDto.setName(user.getName());
            userSupportDto.setUserId(user.getId());


                userSupportDto.setVerify_reply("처리중");


            String origFilename = files.getOriginalFilename();
            String filename = new MD5Generator(origFilename).toString();
            /* 실행되는 위치의 'files' 폴더에 파일이 저장 */
            String savePath = System.getProperty("user.dir") + "\\files";
            /* 파일이 저장되는 폴더가 없으면 폴더를 생성 */
            if (!new File(savePath).exists()) {
                try{
                    new File(savePath).mkdir();
                }
                catch(Exception e){
                    e.getStackTrace();
                }
            }
            String filePath = savePath + "\\" + filename;
            files.transferTo(new File(filePath));

            FileDto fileDto = new FileDto();
            fileDto.setOrigFilename(origFilename);
            fileDto.setFilename(filename);
            fileDto.setFilePath(filePath);
            userSupportDto.setFileName(origFilename);

            Long fileId = fileService.saveFile(fileDto);
            userSupportDto.setFileId(fileId);

            postsService.userSavePosts(userSupportDto);
        } catch (Exception e){
            e.printStackTrace();
        }
        return "redirect:/support/supportBoard";
    }

    //관리자가 답변글 작성할때 가져오는 정보
    @GetMapping(value = "/new/admin/{id}")
    public String supportAdminForm(@PathVariable("id") Long id,ReplyDto replyDto, Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        String userEmail = (String) session.getAttribute("userEmail");
        User user = userService.getUserInfo(userEmail);
        replyDto.setNickname(user.getNickName());
        model.addAttribute("id",id);
        model.addAttribute("replyDto", replyDto);
        return "support/supportAdminPage";
    }

    //관리자가 답변글 포스팅할때
    @PostMapping(value = "/new/admin")
    public String supportAdminSaveForm(  @RequestParam("id") Long id,
                                          ReplyDto replyDto,
                                          HttpServletRequest request,
                                         UserSupportDto userSupportDto){

        HttpSession session = request.getSession();
        String userEmail = (String) session.getAttribute("userEmail");
        User user = userService.getUserInfo(userEmail);
        replyDto.setNickname(user.getNickName());
        ReplyEntity replyEntity = replyDto.toEntity();
        UserSupport userSupport = postsService.getUserSupport(id);

        replyEntity.setUserSupport(userSupport);
        postsService.adminSavePosts(replyEntity);

        if (userSupport.getVerifyReply().equals("처리중")) {
            postsService.updateVerfyReply(id, userSupportDto);
        }

        return "redirect:/support/supportBoard";
    }

    //파일 다운로드
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> fileDownload(@PathVariable("fileId") Long fileId) throws IOException {
        FileDto fileDto = fileService.getFile(fileId);
        Path path = Paths.get(fileDto.getFilePath());
        Resource resource = new InputStreamResource(Files.newInputStream(path));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDto.getOrigFilename() + "\"")
                .body(resource);
    }

    //게시글 수정
    // 게시글 수정 페이지
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model) {
        UserSupportDto postDto = postsService.getPost(id);
        model.addAttribute("postDto", postDto);
        return "support/supportEdit";
    }

    // 게시글 수정 처리
    @PostMapping("/edit/{id}")
    public String update(@PathVariable("id") Long id, @ModelAttribute("postDto") UserSupportDto updatedDto,
                         @RequestParam(value = "file", required = false) MultipartFile file) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        if (file != null && !file.isEmpty()) {
            // 새로운 파일이 전송되었을 경우 파일 처리
            String origFilename = file.getOriginalFilename();
            String filename = new MD5Generator(origFilename).toString();
            String savePath = System.getProperty("user.dir") + "/files";

            try {
                File dir = new File(savePath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String filePath = savePath + "/" + filename;
                file.transferTo(new File(filePath));

                FileDto fileDto = new FileDto();
                fileDto.setOrigFilename(origFilename);
                fileDto.setFilename(filename);
                fileDto.setFilePath(filePath);
                updatedDto.setFileName(origFilename);

                Long fileId = fileService.saveFile(fileDto);
                updatedDto.setFileId(fileId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 파일 필드가 비어 있을 경우, 기존 파일 정보를 유지
            UserSupportDto existingDto = postsService.getPost(id);
            updatedDto.setFileId(existingDto.getFileId());
            updatedDto.setFileName(existingDto.getFileName());
        }

        postsService.updatePost(id, updatedDto);
        return "redirect:/support/supportView/{id}";
    }

    //게시물 삭제
    @GetMapping("/delete/{id}")
    public String deletePost(@PathVariable("id") Long id) {
        postsService.deletePost(id);
        return "redirect:/support/supportBoard";
    }

    // 관리자 답글 수정
    @GetMapping("/edit/admin/{id}")
    public String editAdmin(@PathVariable("id") Long id, Model model) {
        ReplyDto replyDto = postsService.getReply(id);
        model.addAttribute("id",id);
        model.addAttribute("replyDto", replyDto);
        return "support/supportAdminEdit";
    }

    // 관리자 답글 수정처리
    @PostMapping("/edit/admin/{id}")
    public String updateAdmin(@PathVariable("id") Long id, ReplyDto replyDto) {
        postsService.updateAdminPost(id, replyDto);
        return "redirect:/support/supportView/{id}";
    }

    //관리자 답글 삭제
    @GetMapping("/delete/admin/{id}")
    public String deleteAdminPost(@PathVariable("id") Long id, UserSupportDto userSupportDto) {
        UserSupport userSupport = postsService.getUserSupport(id);
        if (userSupport.getVerifyReply().equals("처리완료")) {
            postsService.updateAdminVerfyReply(id, userSupportDto);
        }
        postsService.deleteAdminPost(id);
        return "redirect:/support/supportBoard";
    }
}
