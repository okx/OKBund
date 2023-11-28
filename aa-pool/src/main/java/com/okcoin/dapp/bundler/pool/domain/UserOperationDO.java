package com.okcoin.dapp.bundler.pool.domain;


import com.okcoin.dapp.bundler.infra.chain.CodecUtil;
import com.okcoin.dapp.bundler.infra.chain.FieldUtil;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.infra.chain.constant.ChainIdConstant;
import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;
import lombok.Data;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

import static com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant.ADDRESS_WITH_HEX_PREFIX;
import static com.okcoin.dapp.bundler.pool.constant.Eip4377CommonConstant.ENTRYPOINT_EXT_GAS;


@Data
public class UserOperationDO {

    /**
     * UserOperation hash，可作为交易的唯一值
     */

    private String opHash;

    /**
     * 链id，防止重放攻击
     */

    private IChain chain;

    /**
     * 入口合约地址
     */

    private String entryPoint;


    private UserOperationStatusEnum opStatus;


    /**
     * account
     */

    private String sender;

    /**
     * account nonce
     */

    private BigInteger nonce;

    private String initCode;


    private String callData;

    /**
     * callData的gasLimit
     */

    private BigInteger callGasLimit;

    /**
     * 验证需要的gasLimit
     */

    private BigInteger verificationGasLimit;

    /**
     * 填补入口合约代码所需的gas窟窿
     */

    private BigInteger preVerificationGas;

    /**
     * 同eip1559
     */

    private BigInteger maxFeePerGas;

    /**
     * 同eip1559
     */

    private BigInteger maxPriorityFeePerGas;

    private String paymasterAndData;


    private String signature;


    private String factory;

    private String paymaster;

    private BigInteger gasLimitForPay;

    private BigInteger preVerificationGasL1;

    private UserOperationDO() {
    }

    public DynamicStruct toDynamicStruct() {
        return new DynamicStruct(new Address(sender), new Uint256(nonce),
                new DynamicBytes(Numeric.hexStringToByteArray(initCode)), new DynamicBytes(Numeric.hexStringToByteArray(callData)),
                new Uint256(callGasLimit), new Uint256(verificationGasLimit), new Uint256(preVerificationGas),
                new Uint256(maxFeePerGas), new Uint256(maxPriorityFeePerGas),
                new DynamicBytes(Numeric.hexStringToByteArray(paymasterAndData)), new DynamicBytes(Numeric.hexStringToByteArray(signature)));
    }

    public static UserOperationDO newUserOperationDO() {
        return new UserOperationDO();
    }

    public String getOpHash() {
        if (opHash != null) {
            return opHash;
        }
        byte[] initCodeHash = CodecUtil.keccak256(initCode);
        byte[] callDataHash = CodecUtil.keccak256(callData);
        byte[] paymasterAndDataHash = CodecUtil.keccak256(paymasterAndData);

        DynamicStruct uopStruct = new DynamicStruct(new Address(sender), new Uint256(nonce), new Bytes32(initCodeHash),
                new Bytes32(callDataHash), new Uint256(callGasLimit), new Uint256(verificationGasLimit), new Uint256(preVerificationGas),
                new Uint256(maxFeePerGas), new Uint256(maxPriorityFeePerGas), new Bytes32(paymasterAndDataHash));

        String uopEncode = TypeEncoder.encode(uopStruct);
        byte[] hash = CodecUtil.keccak256(uopEncode);
        String encode = CodecUtil.abiEncode(new Bytes32(hash), new Address(entryPoint), new Uint256(chain.getChainId()));
        return opHash = Numeric.toHexString(CodecUtil.keccak256(encode));

    }

    public BigInteger resolveGasLimitExe() {
        BigInteger gasLimit = resolveGasLimitForValidation().add(callGasLimit);
        if (!Web3Constant.HEX_PREFIX.equals(paymasterAndData)) {
            gasLimit = gasLimit.add(verificationGasLimit).add(verificationGasLimit);
        }

        return gasLimit;
    }

    public BigInteger resolveGasLimitForSimulation() {
        BigInteger gasLimit = resolveGasLimitForValidation().add(callGasLimit).add(verificationGasLimit);
        if (!Web3Constant.HEX_PREFIX.equals(paymasterAndData)) {
            gasLimit = gasLimit.add(verificationGasLimit);
        } else {
            gasLimit = gasLimit.add(ENTRYPOINT_EXT_GAS);
        }

        return gasLimit;
    }

    public BigInteger resolveGasLimitForValidation() {
        BigInteger gasLimit = preVerificationGas.add(verificationGasLimit);

        if (chain.getChainId() == ChainIdConstant.OP_MAIN) {
            gasLimit = gasLimit.subtract(preVerificationGasL1);
        }

        return gasLimit;
    }

    public String getFactory() {
        if (FieldUtil.isValidAddress(factory)) {
            return factory;
        }
        if (initCode.length() < ADDRESS_WITH_HEX_PREFIX) {
            return Web3Constant.HEX_PREFIX;
        } else {
            return initCode.substring(0, ADDRESS_WITH_HEX_PREFIX);
        }
    }

    public String getPaymaster() {
        if (FieldUtil.isValidAddress(paymaster)) {
            return paymaster;
        }

        if (paymasterAndData.length() < ADDRESS_WITH_HEX_PREFIX) {
            return Web3Constant.HEX_PREFIX;
        } else {
            return paymasterAndData.substring(0, ADDRESS_WITH_HEX_PREFIX);
        }
    }
}
