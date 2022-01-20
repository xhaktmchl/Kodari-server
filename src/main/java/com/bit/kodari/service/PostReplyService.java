package com.bit.kodari.service;

import com.bit.kodari.config.BaseException;
import com.bit.kodari.dto.PostReplyDto;
import com.bit.kodari.repository.postreply.PostReplyRepository;
import com.bit.kodari.utils.JwtService;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.bit.kodari.config.BaseResponseStatus.*;

@Slf4j
@Service
public class PostReplyService {
    @Autowired
    PostReplyRepository postReplyRepository;
    //private final JwtService jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!
    @Autowired
    JwtService jwtService;

    public PostReplyService(PostReplyRepository postReplyRepository, JwtService jwtService) {
        this.postReplyRepository = postReplyRepository;
        this.jwtService = jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!
    }

    //토론장 게시글 답글 등록
    public PostReplyDto.RegisterReplyRes insertCommentReply(PostReplyDto.RegisterReplyReq registerReplyReq) throws BaseException {
        int postCommentIdx = registerReplyReq.getPostCommentIdx();
        String content = registerReplyReq.getContent();
        String comment_status = postReplyRepository.getStatusByPostCommentIdx(postCommentIdx);
        String post_status = postReplyRepository.getPostStatusByPostCommentIdx(postCommentIdx);
        if(post_status.equals("inactive")) {//삭제된 게시글에 답글 등록 불가
            throw new BaseException(IMPOSSIBLE_POST);
        }
        else if(comment_status.equals("inactive")) { //삭제된 댓글에 답글 등록 불가
            throw new BaseException(IMPOSSIBLE_POST_COMMENT);
        }
        else if(content.isEmpty()) { //빈 내용은 등록 불가
            throw new BaseException(EMPTY_CONTENT);
        }
        else if(content.length() >= 100) { //내용 100자 이내 제한
            throw new BaseException(OVER_CONTENT);
        }
        try {
            PostReplyDto.RegisterReplyRes registerReplyRes = postReplyRepository.insertReply(registerReplyReq);
            return registerReplyRes;
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //토론장 게시글 답글 수정
    public void modifyReply(PostReplyDto.PatchReplyReq post) throws BaseException{
        int postReplyIdx = post.getPostReplyIdx();
        int userIdx = post.getUserIdx();
        int postCommentIdx = post.getPostCommentIdx();
        String content = post.getContent();
        String comment_status = postReplyRepository.getStatusByPostCommentIdx(postCommentIdx);
        String post_status = postReplyRepository.getPostStatusByPostCommentIdx(postCommentIdx);
        int user = postReplyRepository.getUserIdxByPostReplyIdx(postReplyIdx);
        if(userIdx != user) { //답글 쓴 유저가 아니면 수정불가
            throw new BaseException(USER_NOT_EQUAL_REPLY);
        }
        else if(post_status.equals("inactive")) {//삭제된 게시글에 답글 수정 불가
            throw new BaseException(IMPOSSIBLE_POST);
        }
        else if(comment_status.equals("inactive")) { //삭제된 댓글에 답글 수정 불가
            throw new BaseException(IMPOSSIBLE_POST_COMMENT);
        }
        else if(content.isEmpty()) { //빈 내용이면 수정 불가
            throw new BaseException(EMPTY_CONTENT);
        }
        else if(content.length() >= 100) { //내용 100자 이내 제한
            throw new BaseException(OVER_CONTENT);
        }
        else{
            int result = postReplyRepository.modifyReply(post);
            if(result == 0){ // 0이면 에러가 발생
                throw new BaseException(MODIFY_FAIL_POST_REPLY); //4077
            }
        }

        try {

        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //토론장 게시글 답글 삭제
    public void modifyReplyStatus(PostReplyDto.PatchReplyDeleteReq post) throws BaseException{
        int postReplyIdx = post.getPostReplyIdx();
        int userIdx = post.getUserIdx();
        int user = postReplyRepository.getUserIdxByPostReplyIdx(postReplyIdx);
        if(userIdx != user) { //답글 쓴 유저가 아니면 삭제불가
           throw new BaseException(USER_NOT_EQUAL_REPLY);
        }
        else {
            int result = postReplyRepository.modifyReplyStatus(post);
            if(result == 0){ // 0이면 에러가 발생
                throw new BaseException(DELETE_FAIL_COMMENT_REPLY);
            }
        }
        try {

        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 특정 댓글별 답글 조회
    public List<PostReplyDto.GetReplyRes> getReplyByPostCommentIdx(int postCommentIdx) throws BaseException {
        try {
            List<PostReplyDto.GetReplyRes> getReplyRes = postReplyRepository.getReplyByPostCommentIdx(postCommentIdx);
            return getReplyRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    // 특정 유저의 답글 조회
    public List<PostReplyDto.GetReplyRes> getReplyByUserIdx(int userIdx) throws BaseException {
        try {
            List<PostReplyDto.GetReplyRes> getReplysRes = postReplyRepository.getReplyByUserIdx(userIdx);
            return getReplysRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }



}
