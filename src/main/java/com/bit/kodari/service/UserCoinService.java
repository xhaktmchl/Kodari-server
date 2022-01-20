package com.bit.kodari.service;

import com.bit.kodari.config.BaseException;
import com.bit.kodari.dto.AccountDto;
import com.bit.kodari.dto.UserCoinDto;
import com.bit.kodari.repository.usercoin.UserCoinRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.bit.kodari.config.BaseResponseStatus.*;

@Slf4j
@Service
public class UserCoinService {

    @Autowired
    private UserCoinRepository userCoinRepository;

    //소유 코인 등록

    // TODO 매수평단가, amount 0, 음수는 안됨, max 값 수정
    public UserCoinDto.PostUserCoinRes registerUserCoin(UserCoinDto.PostUserCoinReq postUserCoinReq) throws BaseException {
        //계좌 활성 상태 확인
        int accountIdx = postUserCoinReq.getAccountIdx();
        String status = userCoinRepository.getAccountStatus(accountIdx);
        if(status.equals("inactive")){
            throw new BaseException(FAILED_TO_PROPERTY_RES); //3040
        }
        try {
            UserCoinDto.PostUserCoinRes postUserCoinRes = userCoinRepository.insert(postUserCoinReq);
            return postUserCoinRes;
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            throw new BaseException(DATABASE_ERROR);
        }

    }

    //소유 코인 수정
    public void updateUserCoin(UserCoinDto.PatchUserCoinReq userCoin) throws BaseException{
        //같은 유저의 같은 계좌인지 확인
        int accountIdx = userCoinRepository.getAccountIdxByUserCoinIdx(userCoin.getUserCoinIdx());
        int userIdx = userCoinRepository.getUserIdxByUserCoinIdx(userCoin.getUserCoinIdx());

        try {
            int result = userCoinRepository.updateUserCoin(userCoin);
            if(result == 0) { // 0이면 에러가 발생
                throw new BaseException(MODIFY_FAIL_USERCOIN); //4041
            }
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //특정 코인 조회
    //매수평단가, 코인 이름, 갯수
    //소유 코인 조회
    public List<UserCoinDto.GetUserCoinIdxRes> getUserCoinIdx(int userCoinIdx) throws BaseException {
        try {
            List<UserCoinDto.GetUserCoinIdxRes> getUserCoinIdxRes = userCoinRepository.getUserCoinByUserCoinIdx(userCoinIdx);
            return getUserCoinIdxRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //소유 코인 조회
    public List<UserCoinDto.GetUserCoinRes> getUserCoin(int userIdx) throws BaseException {
        try {
            List<UserCoinDto.GetUserCoinRes> getUserCoinRes = userCoinRepository.getUserCoinByUserIdx(userIdx);
            return getUserCoinRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //소유 코인 삭제
    public void deleteByUserCoinIdx(UserCoinDto.PatchUserCoinDelReq patchUserCoinDelReq) throws BaseException{

        try {
            int result = userCoinRepository.deleteByUserCoinIdx(patchUserCoinDelReq);
            if(result == 0){
                throw new BaseException(MODIFY_FAIL_USERCOIN_STATUS); //4045
            }
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //소유 코인 전체 삭제
    public void deleteByUserIdx(UserCoinDto.PatchDelByUserIdxReq patchDelByUserIdxReq) throws BaseException{

        try {
            int result = userCoinRepository.deletebyUserIdx(patchDelByUserIdxReq);
            if(result == 0){
                throw new BaseException(MODIFY_FAIL_ALL_USERCOIN_STATUS); //4046
            }
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //매수, 매도 계산(매수 평단가), 수수료 0.05%
    //계산하는거 여기에

    /**
     * 수정할것
     * 매수평단가 수정하면서 property(현금자산)도 수정되게 - 매수(-), 매도(+)
     * 총자산 업데이트
     */
    public void updatePriceAvg(UserCoinDto.PatchBuySellReq patchBuySellReq) throws BaseException{
        int userCoinIdx = patchBuySellReq.getUserCoinIdx();
        int coinIdx = userCoinRepository.getCoinIdxByUserCoinIdx(userCoinIdx);
        int accountIdx = userCoinRepository.getAccountIdxByUserCoinIdx(userCoinIdx);
        int portIdx = userCoinRepository.getPortIdx(accountIdx); //빼야함
        int tradeIdx = userCoinRepository.getTradeIdx(coinIdx, portIdx); // portIdx를 accountIdx로 바꿔야함
        String category = userCoinRepository.getCategory(tradeIdx); //매수인지 매도인지
        double price = userCoinRepository.getPrice(tradeIdx);
        double newCoinAmount = userCoinRepository.getAmount(tradeIdx); //새로 산 코인 갯수
        double fee = userCoinRepository.getFee(tradeIdx);

        //기존 총자산
        double totalProperty = userCoinRepository.getTotalProperty(accountIdx);
        //기존 현금자산
        double property = userCoinRepository.getProperty(accountIdx);
        //기존 코인 갯수
        double existCoinAmount = userCoinRepository.getAmountByUserCoinIdx(userCoinIdx);
        //기존 매수평단가
        double priceAvg = userCoinRepository.getPriceAvg(userCoinIdx);
        double total = 0; //새로운 매수평단가
        double sumCoinAmount = 0; //새로운 코인 전체 갯수
        double newTotal = 0; //새로운 총자산

        //매수일때
        if(category.equals("buy")){
            //매수평단가 새로 계산해서 업데이트
            sumCoinAmount = existCoinAmount + newCoinAmount;
            total = (priceAvg * existCoinAmount + price * newCoinAmount) / sumCoinAmount;
        }else if(category.equals("sell")) {
            sumCoinAmount = existCoinAmount - newCoinAmount;
            // TODO 기존 수량보다 new가 더 크면 안됨. 오류 이름 수정
            if(sumCoinAmount < 0){
                throw new BaseException(MODIFY_FAIL_PRICE_AVG); //4048
            }
            total = (priceAvg * existCoinAmount - price * newCoinAmount) / sumCoinAmount;
            if(total < 0){
                throw new BaseException(MODIFY_FAIL_PRICE_AVG); //4048
            }
        }
        else{
            throw new BaseException(MODIFY_FAIL_PRICE_AVG); //4048
        }

        try {
            int result = userCoinRepository.updatePriceAvg(userCoinIdx, total, sumCoinAmount);
            if(result == 0) { // 0이면 에러가 발생
                throw new BaseException(MODIFY_FAIL_PRICE_AVG); //4048
            }
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
