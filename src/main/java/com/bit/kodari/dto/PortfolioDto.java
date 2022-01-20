package com.bit.kodari.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class PortfolioDto {
    //Portfolio 기본 정보
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Portfolio {
        private int portIdx;
        private int userIdx;
        private int accountIdx;
        private String status;
    }

    //포트폴리오 생성 Request Dto
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostPortfolioReq{
        private int userIdx;
        private int accountIdx;
    }

    //포트폴리오 생성 Response Dto
    @Data
    @Builder // 빌더 클래스 자동 생성
    public static class PostPortfolioRes{
        private int portIdx;
        private int userIdx;
        private int accountIdx;
    }

    // 포트폴리오 조회 RESPONSE DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GetPortfolioRes{
        //private List<UserCoinDto.UserCoin> userCoinList;
        //대표코인 리스트 - 해당 포트폴리오의
        //아래 지우기
        private int portIdx;
        private int accountIdx;
        private String accountName;
        private double property;
        private int userIdx;
        private String marketName;
        private int userCoinIdx;
        private String coinName;
        private String coinImg;
        private double priceAvg;
        private double amount;
        //    private String jwt;
    }

    //포트폴리오 삭제 Request Dto
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PatchPortfolioDelReq{
        private int portIdx;
        //    private String jwt;
    }

    //포트폴리오 삭제 Response Dto
    @Data
    @Builder // 빌더 클래스 자동 생성
    public static class PatchPortfolioDelRes{
        private int portIdx;
        private int userIdx;
        private int accountIdx;
        private String status;
        //    private String jwt;
    }

    // 모든 포트폴리오 userIdx, accountIdx RESPONSE DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GetAllPortfolioRes{
        private int userIdx;
        private int accountIdx;
    }


}
