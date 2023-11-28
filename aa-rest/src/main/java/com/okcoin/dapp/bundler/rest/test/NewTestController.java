package com.okcoin.dapp.bundler.rest.test;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.AccountUtil;
import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;
import com.okcoin.dapp.bundler.pool.config.ChainConfig;
import com.okcoin.dapp.bundler.pool.config.PoolConfig;
import com.okcoin.dapp.bundler.pool.entrypoint.Entrypoint;
import com.okcoin.dapp.bundler.pool.gasprice.GasPriceInfo;
import com.okcoin.dapp.bundler.pool.gasprice.GasService;
import com.okcoin.dapp.bundler.rest.account.AccountFactory;
import com.okcoin.dapp.bundler.rest.account.AccountSignContext;
import com.okcoin.dapp.bundler.rest.account.IAccount;
import com.okcoin.dapp.bundler.rest.account.SingleCallDataContext;
import com.okcoin.dapp.bundler.rest.api.AAController;
import com.okcoin.dapp.bundler.rest.api.req.AAReq;
import com.okcoin.dapp.bundler.rest.api.req.UserOperationParam;
import com.okcoin.dapp.bundler.rest.api.resp.AAResp;
import com.okcoin.dapp.bundler.rest.api.resp.EstimateUserOperationGasVO;
import com.okcoin.dapp.bundler.rest.constant.AaMethodConstant;
import com.okcoin.dapp.bundler.rest.factory.AccountFactoryFactory;
import com.okcoin.dapp.bundler.rest.factory.IAccountFactory;
import com.okcoin.dapp.bundler.rest.factory.InitCodeContext;
import com.okcoin.dapp.bundler.rest.factory.SenderAddressContext;
import com.okcoin.dapp.bundler.rest.util.UopUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.crypto.Credentials;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping
@Slf4j
public class NewTestController {

    private static final String FAKE_SIGN_FOR_EVM = "0x7fffffffffffffffffffffffffffffff5d576e7357a4501ddfe92f46681b20a07fffffffffffffffffffffffffffffff5d576e7357a4501ddfe92f46681b20a01b";


    @Autowired
    private AAController aaController;

    @Autowired
    private AccountFactory accountFactory;

    @Autowired
    private AccountFactoryFactory accountFactoryFactory;

    @Autowired
    private GasService gasService;

    @Autowired
    private PoolConfig poolConfig;

    @Autowired
    private ChainConfig chainConfig;

    @PostMapping("test")
    public String testAllInterface(@RequestBody TestUserOperationReq req) {
        Credentials credentials = Credentials.create(req.getPrivateKey());
        String owner = credentials.getAddress();
        String entryPoint = req.getEntryPoint();
        String accountFactoryAddress = req.getAccountFactory();
        String accountAddress = req.getAccount();
        String callData = req.getCallData();
        BigInteger salt = BigInteger.ZERO;
        String paymaster = Web3Constant.HEX_PREFIX;

        AAReq AAReq = new AAReq();
        AAReq.setId(1L);
        AAReq.setJsonrpc("2.0");

        IAccount account = accountFactory.get(accountAddress);

        IAccountFactory factory = accountFactoryFactory.get(accountFactoryAddress);
        InitCodeContext initCodeContext = new InitCodeContext(accountFactoryAddress, owner, salt);
        String initCode = factory.getInitCode(initCodeContext);
        String sender = factory.getSenderAddress(new SenderAddressContext(initCode, entryPoint), chainConfig);
        String senderCode = AccountUtil.getCode(sender, chainConfig);
        if (!Web3Constant.HEX_PREFIX.equals(senderCode)) {
            initCode = Web3Constant.HEX_PREFIX;
        }

        if (callData == null) {
            List<SingleCallDataContext> context = Lists.newArrayList();
            SingleCallDataContext callDataContext = new SingleCallDataContext(owner, BigInteger.ZERO, DynamicBytes.DEFAULT.getValue());
            context.add(callDataContext);
            callData = account.getCallData(context);
        }

        BigInteger nonce = Entrypoint.getNonce(entryPoint, sender, BigInteger.ZERO, chainConfig);

        UserOperationParam uopInit = initUop(sender, nonce, initCode, callData, paymaster);

        AAReq.setMethod(AaMethodConstant.ETH_ESTIMATE_USER_OPERATION_GAS);
        AAReq.setParams(Arrays.asList(uopInit, entryPoint));
        AAResp<EstimateUserOperationGasVO> estimateUserOperationGasResp = aaController.chain(AAReq);
        if (estimateUserOperationGasResp.getError() != null) {
            return "eth_estimateUserOperationGas failed: " + JSON.toJSONString(estimateUserOperationGasResp.getError());
        }

        EstimateUserOperationGasVO gasResp = estimateUserOperationGasResp.getResult();

        uopInit.setCallGasLimit(gasResp.getCallGasLimit());
        uopInit.setVerificationGasLimit(gasResp.getVerificationGasLimit());
        uopInit.setPreVerificationGas(gasResp.getPreVerificationGas());

        GasPriceInfo gasPriceInfo = gasService.getGasPriceInfoWithCache(chainConfig);
        BigInteger maxPriorityFeePerGas = gasPriceInfo.getMaxPriorityFeePerGas();
        BigInteger maxFeePerGas = gasPriceInfo.resolveMaxFeePerGas();
        uopInit.setMaxFeePerGas(Numeric.toHexStringWithPrefix(maxFeePerGas));
        uopInit.setMaxPriorityFeePerGas(Numeric.toHexStringWithPrefix(maxPriorityFeePerGas));

        AccountSignContext signContext = new AccountSignContext(UopUtil.toUserOperationDO(uopInit, chainConfig, entryPoint), credentials);
        String signature = account.sign(signContext);
        uopInit.setSignature(signature);

        //5.eth_sendUserOperation
        AAReq.setMethod(AaMethodConstant.ETH_SEND_USER_OPERATION);
        AAReq.setParams(Arrays.asList(uopInit, entryPoint));
        AAResp<String> sendUserOperationResp = aaController.chain(AAReq);
        if (sendUserOperationResp.getError() != null) {
            return "eth_sendUserOperation failed: " + JSON.toJSONString(sendUserOperationResp.getError());
        }

        return sendUserOperationResp.getResult();
    }

    private UserOperationParam initUop(String sender, BigInteger nonce, String initCode, String callData, String paymaster) {
        UserOperationParam uop = new UserOperationParam();
        uop.setSender(sender);
        uop.setNonce(Numeric.encodeQuantity(nonce));
        uop.setInitCode(initCode);
        uop.setCallData(callData);
        uop.setPaymasterAndData(paymaster);
        uop.setSignature(FAKE_SIGN_FOR_EVM);
        uop.setCallGasLimit(Web3Constant.HEX_ZERO);
        uop.setVerificationGasLimit(Web3Constant.HEX_ZERO);
        uop.setMaxFeePerGas(Web3Constant.HEX_ZERO);
        uop.setMaxPriorityFeePerGas(Web3Constant.HEX_ZERO);
        uop.setPreVerificationGas(Web3Constant.HEX_ZERO);
        return uop;
    }

}
