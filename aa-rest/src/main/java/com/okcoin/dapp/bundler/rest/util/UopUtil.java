package com.okcoin.dapp.bundler.rest.util;

import com.esaulpaugh.headlong.abi.Tuple;
import com.okcoin.dapp.bundler.infra.chain.CodecUtil;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;
import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.pool.domain.UserOperationStatusEnum;
import com.okcoin.dapp.bundler.rest.api.req.UserOperationParam;
import com.okcoin.dapp.bundler.rest.api.resp.UserOperationOnChainVO;
import com.okcoin.dapp.bundler.rest.api.resp.UserOperationVO;
import lombok.extern.slf4j.Slf4j;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Numeric;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UopUtil {

    public static UserOperationDO toUserOperationDO(UserOperationParam param, IChain chain, String entryPointAddress) {
        UserOperationDO uop = UserOperationDO.newUserOperationDO();
        uop.setOpStatus(UserOperationStatusEnum.WAITING);
        uop.setChain(chain);
        uop.setEntryPoint(entryPointAddress);
        uop.setSender(param.getSender());
        uop.setNonce(Numeric.decodeQuantity(param.getNonce()));
        uop.setInitCode(param.getInitCode());
        uop.setCallData(param.getCallData());
        uop.setPaymasterAndData(param.getPaymasterAndData());
        uop.setSignature(param.getSignature());

        uop.setCallGasLimit(Numeric.decodeQuantity(param.getCallGasLimit()));
        uop.setVerificationGasLimit(Numeric.decodeQuantity(param.getVerificationGasLimit()));
        uop.setPreVerificationGas(Numeric.decodeQuantity(param.getPreVerificationGas()));
        uop.setMaxFeePerGas(Numeric.decodeQuantity(param.getMaxFeePerGas()));
        uop.setMaxPriorityFeePerGas(Numeric.decodeQuantity(param.getMaxPriorityFeePerGas()));
        return uop;
    }

    public static UserOperationDO copy(UserOperationDO oldUop) {
        UserOperationDO uop = UserOperationDO.newUserOperationDO();
        uop.setChain(oldUop.getChain());
        uop.setEntryPoint(oldUop.getEntryPoint());
        uop.setOpStatus(oldUop.getOpStatus());
        uop.setSender(oldUop.getSender());
        uop.setNonce(oldUop.getNonce());
        uop.setInitCode(oldUop.getInitCode());
        uop.setCallData(oldUop.getCallData());
        uop.setCallGasLimit(oldUop.getCallGasLimit());
        uop.setVerificationGasLimit(oldUop.getVerificationGasLimit());
        uop.setPreVerificationGas(oldUop.getPreVerificationGas());
        uop.setMaxFeePerGas(oldUop.getMaxFeePerGas());
        uop.setMaxPriorityFeePerGas(oldUop.getMaxPriorityFeePerGas());
        uop.setPaymasterAndData(oldUop.getPaymasterAndData());
        uop.setSignature(oldUop.getSignature());
        return uop;
    }

    public static UserOperationDO toUserOperationDO(IChain chain, String entryPoint, Tuple tuple) {
        UserOperationDO uop = UserOperationDO.newUserOperationDO();
        uop.setChain(chain);
        uop.setEntryPoint(entryPoint.toLowerCase());
        uop.setOpStatus(UserOperationStatusEnum.SUCCEED);
        uop.setSender(tuple.get(0).toString().toLowerCase());
        uop.setNonce(tuple.get(1));
        uop.setInitCode(Numeric.toHexString(tuple.get(2)));
        uop.setCallData(Numeric.toHexString(tuple.get(3)));
        uop.setCallGasLimit(tuple.get(4));
        uop.setVerificationGasLimit(tuple.get(5));
        uop.setPreVerificationGas(tuple.get(6));
        uop.setMaxFeePerGas(tuple.get(7));
        uop.setMaxPriorityFeePerGas(tuple.get(8));
        uop.setPaymasterAndData(Numeric.toHexString(tuple.get(9)));
        uop.setSignature(Numeric.toHexString(tuple.get(10)));
        return uop;
    }

    public static UserOperationOnChainVO toUserOperationVo(Transaction transaction, UserOperationDO uop) {
        UserOperationOnChainVO resultVO = new UserOperationOnChainVO();
        resultVO.setUserOperation(toUserOperationVO(uop));
        resultVO.setEntryPoint(CodecUtil.toChecksumAddress(uop.getEntryPoint()));
        resultVO.setBlockHash(transaction.getBlockHash());
        resultVO.setBlockNumber(Numeric.encodeQuantity(transaction.getBlockNumber()));
        resultVO.setTransactionHash(transaction.getHash());
        return resultVO;
    }

    public static List<UserOperationVO> convertDoToUserOperationVO(List<UserOperationDO> userOperationDOs) {
        List<UserOperationVO> userOperationOnChainVOS = new ArrayList<>();
        for (UserOperationDO userOperationDO : userOperationDOs) {
            userOperationOnChainVOS.add(toUserOperationVO(userOperationDO));
        }
        return userOperationOnChainVOS;
    }

    private static UserOperationVO toUserOperationVO(UserOperationDO uop) {
        UserOperationVO uopVO = new UserOperationVO();
        uopVO.setSender(CodecUtil.toChecksumAddress(uop.getSender()));
        uopVO.setNonce(Numeric.toHexStringWithPrefix(uop.getNonce()));
        uopVO.setInitCode(uop.getInitCode());
        // TODO YUKINO 2023/10/30: 不应该有大小写区分
        if (!uop.getFactory().equals(Web3Constant.HEX_PREFIX)) {
            uopVO.setInitCode(CodecUtil.toChecksumAddress(uop.getFactory()) + uop.getInitCode().substring(Web3Constant.ADDRESS_WITH_HEX_PREFIX));
        }
        uopVO.setCallData(uop.getCallData());
        uopVO.setCallGasLimit(Numeric.toHexStringWithPrefix(uop.getCallGasLimit()));
        uopVO.setVerificationGasLimit(Numeric.toHexStringWithPrefix(uop.getVerificationGasLimit()));
        uopVO.setPreVerificationGas(Numeric.toHexStringWithPrefix(uop.getPreVerificationGas()));
        uopVO.setMaxFeePerGas(Numeric.toHexStringWithPrefix(uop.getMaxFeePerGas()));
        uopVO.setMaxPriorityFeePerGas(Numeric.toHexStringWithPrefix(uop.getMaxPriorityFeePerGas()));
        uopVO.setPaymasterAndData(uop.getPaymasterAndData());
        uopVO.setSignature(uop.getSignature());

        return uopVO;
    }
}
