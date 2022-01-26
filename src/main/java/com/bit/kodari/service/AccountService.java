package com.bit.kodari.service;

import com.bit.kodari.config.BaseException;
import com.bit.kodari.config.BaseResponse;
import com.bit.kodari.dto.AccountDto;
import com.bit.kodari.repository.account.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.bit.kodari.config.BaseResponseStatus.*;
import static com.bit.kodari.dto.AccountDto.*;

@Slf4j
@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;

    //계좌 등록
    public PostAccountRes registerAccount(PostAccountReq postAccountReq) throws BaseException {
        //같은 이름은 안되게 validation 추가
        //같은 유저의 같은 거래소에는 같은 이름을 가진 계좌가 있으면 안됨.
        //계좌 3개 이상 오류
        String accountName = postAccountReq.getAccountName();
        int userIdx = postAccountReq.getUserIdx();
        int marketIdx = postAccountReq.getMarketIdx();
        long property = postAccountReq.getProperty();
        long max = 100000000000L;
        List<AccountDto.GetAccountIdxRes> getAccountIdxRes = accountRepository.getAccountIdxByIdx(userIdx, marketIdx);

        if(getAccountIdxRes.size() >= 3){
            throw  new BaseException(OVER_ACCOUNT_THREE); //3042
        }

        // 스페이스바, 널값 X validation
        accountName = accountName.replaceAll(" ", "");
        List<AccountDto.GetAccountNameRes> getAccountNameRes = accountRepository.getAccountNameByIdx(userIdx, marketIdx);
        // 계좌 이름 null X
        if (StringUtils.isEmpty(accountName) == true || accountName.length()==0) {
            throw new BaseException(POST_ACCOUNT_NAME_NULL); //2040
        }
        if(property < 0 || property > max){
            // 현금 자산 범위 초과
            throw new BaseException(PROPERTY_RANGE_ERROR);
        }else {
            for(int i=0; i< getAccountNameRes.size(); i++){
                if(getAccountNameRes.get(i).getAccountName().equals(accountName)){
                    // 계좌 이름 중복
                    throw new BaseException(DUPLICATED_ACCOUNT_NAME); //4040
                }
            }
        }

        try {
            PostAccountRes postAccountRes = accountRepository.insert(postAccountReq);
            return postAccountRes;
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            throw new BaseException(DATABASE_ERROR);
        }

    }

    //총자산 수정
    public void updateTotal(PatchTotalReq account) throws BaseException {
        int accountIdx = account.getAccountIdx();
        String accountName = accountRepository.getAccountNameByAccountIdx(accountIdx);
        double totalProperty = accountRepository.getTotalPropertyByAccount(accountIdx);
        int userIdx = accountRepository.getUserIdxByAccountIdx(accountIdx);
        double property = accountRepository.getPropertyByAccount(accountIdx);
        double priceAvg = 0;
        double amount = 0;

        List<AccountDto.GetUserCoinRes> getUserCoinRes = accountRepository.getUserCoinByIdx(userIdx, accountIdx);

        for(int i=0; i< getUserCoinRes.size(); i++){
            priceAvg = getUserCoinRes.get(i).getPriceAvg();
            amount = getUserCoinRes.get(i).getAmount();
            totalProperty = totalProperty + (priceAvg * amount);
        }

        try {
            int result = accountRepository.modifyTotal(accountIdx, totalProperty);
            if (result == 0) { // 0이면 에러가 발생
                throw new BaseException(MODIFY_FAIL_TOTAL); //4051
            }
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            throw new BaseException(DATABASE_ERROR);
        }

    }

    //계좌 이름 수정
    //스페이스바 validation 추가
    public void updateAccountName(PatchAccountNameReq account) throws BaseException{
        //같은 이름은 안되게 validation 추가
        //같은 유저의 같은 거래소에는 같은 이름을 가진 계좌가 있으면 안됨.
        int accountIdx1 = account.getAccountIdx();
        int userIdx = accountRepository.getUserIdxByAccountIdx(accountIdx1);
        int marketIdx = accountRepository.getMarketIdxByAccountIdx(accountIdx1);

        List<AccountDto.GetAccountNameRes> getAccountNameRes = accountRepository.getAccountNameByIdx(userIdx, marketIdx);

        String accountName = account.getAccountName().replaceAll(" ", "");
        // null값 안됨. 스페이스바 validation 추가
        if (StringUtils.isEmpty(accountName) == true || accountName.length()==0) {
            throw new BaseException(POST_ACCOUNT_NAME_NULL); //2040
        }

        for(int i=0; i< getAccountNameRes.size(); i++){
            if(getAccountNameRes.get(i).getAccountName().equals(account.getAccountName())){
                throw new BaseException(DUPLICATED_ACCOUNT_NAME); //4040
            }else {

                int result = accountRepository.modifyAccountName(account);
                if (result == 0) { // 0이면 에러가 발생
                    throw new BaseException(MODIFY_FAIL_ACCOUNTNAME); //4040
                }
            }
        }
        try {

        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //현금 자산 수정
    public void updateProperty(PatchPropertyReq account) throws BaseException{
        //음수, 너무 큰 수 안되게
        //계좌 활성 상태 확인
        double totalProperty = accountRepository.getTotalPropertyByAccount(account.getAccountIdx());
        double property = accountRepository.getPropertyByAccount(account.getAccountIdx());
        long newProperty = account.getProperty();
        long max = 100000000000L;
        int userIdx = accountRepository.getUserIdxByAccountIdx(account.getAccountIdx());
        if(newProperty < 0 || newProperty > max){
            throw new BaseException(PROPERTY_RANGE_ERROR); //4044
        }
        totalProperty = totalProperty - property + newProperty;
        try {
            int result = accountRepository.modifyProperty(account, totalProperty);
            if(result == 0){ // 0이면 에러가 발생
                throw new BaseException(MODIFY_FAIL_PROPERTY); //4041
            }
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // Trade - 현금 자산 수정
    // tradeIdx만 받는걸로 수정함.
    public void updateTradeProperty(int tradeIdx) throws BaseException{
        int portIdx = accountRepository.getPortIdx(tradeIdx);
        int accountIdx = accountRepository.getAccountIdx(portIdx);
        double property = accountRepository.getPropertyByAccount(accountIdx);
        String category = accountRepository.getCategory(tradeIdx); //매수인지 매도인지
        double price = accountRepository.getPrice(tradeIdx);
        double amount = accountRepository.getAmount(tradeIdx); //새로 산 코인 갯수
        double fee = accountRepository.getFee(tradeIdx);
        double totalProperty = accountRepository.getTotalPropertyByAccount(accountIdx); //총자산

        double newProperty = 0;

        long max = 100000000000L;

        //매수일때
        if(category.equals("buy")){
            //현금 자산 새로 계산해서 업데이트
            //총자산 새로 업데이트
            newProperty = property - (price * amount) - (price * amount * fee);
            totalProperty = totalProperty - property + newProperty + (price * amount);
            if(newProperty < 0 || newProperty > max){
                throw new BaseException(PROPERTY_RANGE_ERROR); //4044
            }
        }else if(category.equals("sell")){
            //매도일때
            //현금 자산 새로 계산해서 업데이트
            newProperty = property + (price * amount) - (price * amount * fee);
            totalProperty = totalProperty - property + newProperty - (price * amount);
        }else{
            throw new BaseException(MODIFY_FAIL_PRICE_AVG); //4048
        }

        try {
            int result = accountRepository.modifyTradeProperty(newProperty, totalProperty, accountIdx);
            if(result == 0){ // 0이면 에러가 발생
                throw new BaseException(MODIFY_FAIL_PROPERTY); //4041
            }
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //계좌 삭제
    public void deleteByIdx(PatchAccountDelReq patchAccountDelReq) throws BaseException{
        List<AccountDto.GetUserCoinIdxRes> getUserCoinIdxRes = accountRepository.getUserCoinIdx(patchAccountDelReq.getAccountIdx());
        try {
            int result;
            //소유코인 없을때
            if(getUserCoinIdxRes.size() == 0){
                result = accountRepository.deleteTwo(patchAccountDelReq);
            }
            //세개 다 삭제할때
            else{
                result = accountRepository.deleteByIdx(patchAccountDelReq);
            }
            if(result == 0){
                throw new BaseException(MODIFY_FAIL_ACCOUNT_STATUS); //4042
            }
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 해당 userIdx를 갖는 User의 계좌 조회
    public List<GetAccountRes> getAccountByUserIdx(int userIdx) throws BaseException {
        try {
            List<GetAccountRes> getAccountRes = accountRepository.getAccountByUserIdx(userIdx);
            return getAccountRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // accountIdx로 계좌 단일 조회
    public List<GetAccountByAccountIdxRes> getAccountByAccountIdx(int accountIdx) throws BaseException {
        try {
            List<GetAccountByAccountIdxRes> getAccountByAccountIdxRes = accountRepository.getAccountByAccountIdx(accountIdx);
            return getAccountByAccountIdxRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }



    // 해당 accountIdx를 갖는 계좌의 현금 자산 조회
    public  List<GetPropertyRes> getProperty(int accountIdx) throws BaseException {
        //status가 inactive인 account는 오류 메시지
        String status = accountRepository.getStatusByAccountIdx(accountIdx);
        if(status.equals("inactive")){
            throw new BaseException(FAILED_TO_PROPERTY_RES); //3040
        }
        try {
            List<GetPropertyRes> getPropertyRes = accountRepository.getProperty(accountIdx);
            return getPropertyRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
