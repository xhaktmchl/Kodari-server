package com.bit.kodari.repository.postcomment;

import com.bit.kodari.dto.PostCommentDto;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PostCommentRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    PostCommentSql postCommentSql;
    public PostCommentRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    //토론장 게시글 댓글등록
    public PostCommentDto.RegisterCommentRes insertComment(PostCommentDto.RegisterCommentReq post) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("userIdx", post.getUserIdx())
                .addValue("postIdx", post.getPostIdx())
                .addValue("content", post.getContent());
        int affectedRows = namedParameterJdbcTemplate.update(postCommentSql.INSERT_COMMENT, parameterSource, keyHolder);
        return PostCommentDto.RegisterCommentRes.builder().userIdx(post.getUserIdx()).build();
    }

    //postCommentIdx로 댓글 쓴 userIdx 가져오기
    public int getUserIdxByPostCommentIdx(int postCommentIdx) {
        SqlParameterSource parameterSource = new MapSqlParameterSource("postCommentIdx", postCommentIdx);
        return namedParameterJdbcTemplate.query(postCommentSql.GET_USER_IDX, parameterSource, rs -> {
            int userIdx = 0;
            if (rs.next()) {
                userIdx = rs.getInt("userIdx");
            }

            return userIdx;
        });
    }

    //postCommentIdx로 댓글 쓴 게시글의 postIdx 가져오기
    public int getPostIdxByPostCommentIdx(int postCommentIdx) {
        SqlParameterSource parameterSource = new MapSqlParameterSource("postCommentIdx", postCommentIdx);
        return namedParameterJdbcTemplate.query(postCommentSql.GET_POST_IDX, parameterSource, rs -> {
            int postIdx = 0;
            if (rs.next()) {
                postIdx = rs.getInt("postIdx");
            }

            return postIdx;
        });
    }

    //postCommentIdx로 댓글 Status 가져오기
    public String getStatusByPostCommentIdx(int postCommentIdx) {
        SqlParameterSource parameterSource = new MapSqlParameterSource("postCommentIdx", postCommentIdx);
        return namedParameterJdbcTemplate.query(postCommentSql.GET_STATUS, parameterSource, rs -> {
            String status = " ";
            if (rs.next()) {
                status = rs.getString("status");
            }

            return status;
        });
    }

    //postIdx로 댓글쓴 게시글의 status 가져오기
    public String getStatusByPostIdx(int postIdx) {
        SqlParameterSource parameterSource = new MapSqlParameterSource("postIdx", postIdx);
        return namedParameterJdbcTemplate.query(postCommentSql.GET_POST_STATUS, parameterSource, rs -> {
            String post_status = " ";
            if (rs.next()) {
                post_status = rs.getString("status");
            }

            return post_status;
        });

    }

    //postCommentIdx 댓글 삭제 시 관련된 댓글 좋아요 삭제
    public List<PostCommentDto.GetCommentLikeDeleteRes> getCommentLikeIdxByPostCommentIdx(int postCommentIdx){
        SqlParameterSource parameterSource = new MapSqlParameterSource("postCommentIdx", postCommentIdx);
        try {
            List<PostCommentDto.GetCommentLikeDeleteRes> getCommentLikeDeleteRes =  namedParameterJdbcTemplate.query(PostCommentSql.GET_COMMENT_LIKE_IDX, parameterSource,
                    (rs, rowNum) -> new PostCommentDto.GetCommentLikeDeleteRes(
                            rs.getInt("commentLikeIdx"))
            );
            return getCommentLikeDeleteRes;

        }catch(EmptyResultDataAccessException e){
            return null;
        }
    }



    //댓글 수정
    public int modifyComment(PostCommentDto.PatchCommentReq patchCommentReq) {
        String qry = PostCommentSql.UPDATE_COMMENT;
        SqlParameterSource parameterSource = new MapSqlParameterSource("postCommentIdx", patchCommentReq.getPostCommentIdx())
                .addValue("userIdx", patchCommentReq.getUserIdx())
                .addValue("postIdx", patchCommentReq.getPostIdx())
                .addValue("content", patchCommentReq.getContent());
        return namedParameterJdbcTemplate.update(qry, parameterSource);
    }

    //댓글 삭제
    public int modifyCommentStatus(PostCommentDto.PatchDeleteReq patchDeleteReq) {
        String qry = PostCommentSql.DELETE_COMMENT;
        SqlParameterSource parameterSource = new MapSqlParameterSource("postCommentIdx", patchDeleteReq.getPostCommentIdx());
        return namedParameterJdbcTemplate.update(qry, parameterSource);
    }

    //삭제된 댓글과 관련된 댓글 좋아요 삭제
    public int deleteCommentLikeStatus(int commentLikeIdx) {
        String qry = PostCommentSql.DELETE_COMMENT_LIKE;
        SqlParameterSource parameterSource = new MapSqlParameterSource("commentLikeIdx", commentLikeIdx);
        return namedParameterJdbcTemplate.update(qry, parameterSource);
    }


    //토론장 게시글별 댓글 조회
    public List<PostCommentDto.GetCommentRes> getCommentsByPostIdx(int postIdx){
        SqlParameterSource parameterSource = new MapSqlParameterSource("postIdx", postIdx);
        List<PostCommentDto.GetCommentRes> getCommentRes = namedParameterJdbcTemplate.query(PostCommentSql.LIST_POST_COMMENT,parameterSource,
                (rs, rowNum) -> new PostCommentDto.GetCommentRes(
                        rs.getString("nickName"),
                        rs.getString("profileImgUrl"),
                        rs.getString("content")) // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
        );

        return getCommentRes;
    }

    //토론장 특정 유저의 게시글 조회
    public List<PostCommentDto.GetCommentRes> getCommentsByUserIdx(int userIdx) {
        SqlParameterSource parameterSource = new MapSqlParameterSource("userIdx", userIdx);
        List<PostCommentDto.GetCommentRes> getCommentsRes = namedParameterJdbcTemplate.query(PostCommentSql.LIST_USER_COMMENT, parameterSource,
                (rs, rowNum) -> new PostCommentDto.GetCommentRes(
                        rs.getString("nickName"),
                        rs.getString("profileImgUrl"),
                        rs.getString("content"))
        );

        return getCommentsRes;
    }

    //토론장 게시글별 댓글 수 조회
    public List<PostCommentDto.GetCommentCntRes> getCommentCntByPostIdx(int postIdx){
        SqlParameterSource parameterSource = new MapSqlParameterSource("postIdx", postIdx);
        List<PostCommentDto.GetCommentCntRes> getCommentCntRes = namedParameterJdbcTemplate.query(postCommentSql.LIST_COMMENT_CNT,parameterSource,
                (rs, rowNum) -> new PostCommentDto.GetCommentCntRes(
                        rs.getInt("comment_cnt")) // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
        );
        return getCommentCntRes;
    }

}
