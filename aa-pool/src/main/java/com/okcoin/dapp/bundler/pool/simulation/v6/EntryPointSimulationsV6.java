package com.okcoin.dapp.bundler.pool.simulation.v6;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.ChainDebugUtil;
import com.okcoin.dapp.bundler.infra.chain.ChainErrorUtil;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.infra.chain.TransactionUtil;
import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;
import com.okcoin.dapp.bundler.infra.chain.error.ChainErrorMsg;
import com.okcoin.dapp.bundler.infra.chain.error.UnKnowError;
import com.okcoin.dapp.bundler.infra.chain.web3j.req.OverrideAccount;
import com.okcoin.dapp.bundler.infra.chain.web3j.req.StateOverride;
import com.okcoin.dapp.bundler.pool.config.PoolConfig;
import com.okcoin.dapp.bundler.pool.config.ValidationConfig;
import com.okcoin.dapp.bundler.pool.constant.OpCodeConstant;
import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.pool.domain.debug.*;
import com.okcoin.dapp.bundler.pool.domain.error.*;
import com.okcoin.dapp.bundler.pool.exception.AAException;
import com.okcoin.dapp.bundler.pool.exception.AAExceptionData;
import com.okcoin.dapp.bundler.pool.exception.AAExceptionEnum;
import com.okcoin.dapp.bundler.pool.exception.UnexpectedException;
import com.okcoin.dapp.bundler.pool.simulation.IEntryPointSimulations;
import com.okcoin.dapp.bundler.pool.util.ValidateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.utils.Numeric;
import org.web3j.utils.Strings;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.okcoin.dapp.bundler.pool.constant.Eip4377CommonConstant.*;


@Service(EntryPointSimulationsV6.VERSION)
@Slf4j
public class EntryPointSimulationsV6 implements IEntryPointSimulations {

    private static final String MAX_BALANCE = Web3Constant.HEX_PREFIX + "7" + Strings.repeat('f', 63);

    public static final String VERSION = "entry_point_simulations_v6";

    private static final String TRACE_CODE = "{    callsFromEntryPoint: [],        currentLevel: null,    keccak: [],    calls: [],    logs: [],    debug: [],    lastOp: '',    lastThreeOpcodes: [],        stopCollectingTopic: 'bb47ee3e183a558b1a2ff0874b079f3fc5478b7454eacf2bfc5af2ff5878f972',    stopCollecting: false,    topLevelCallCounter: 0,    fault(log, db) {    this.debug.push('fault depth=', log.getDepth(), ' gas=', log.getGas(), ' cost=', log.getCost(), ' err=', log.getError());},    result(ctx, db) {    return {        callsFromEntryPoint: this.callsFromEntryPoint,        keccak: this.keccak,        logs: this.logs,        calls: this.calls,        debug: this.debug     };},    enter(frame) {    if (this.stopCollecting) {        return;    }        this.calls.push({        type: frame.getType(),        from: toHex(frame.getFrom()),        to: toHex(frame.getTo()),        method: toHex(frame.getInput()).slice(0, 10),        gas: frame.getGas(),        value: frame.getValue()    });},    exit(frame) {    if (this.stopCollecting) {        return;    }    this.calls.push({        type: frame.getError() != null ? 'REVERT' : 'RETURN',        gasUsed: frame.getGasUsed(),        data: toHex(frame.getOutput()).slice(0, 4000)    });},        countSlot(list, key) {    var _a;    list[key] = ((_a = list[key]) !== null && _a !== void 0 ? _a : 0) + 1;},    step(log, db) {    var _a;    if (this.stopCollecting) {        return;    }    opcode = log.op.toString();    stackSize = log.stack.length();    stackTop3 = [];    for (i = 0; i < 3 && i < stackSize; i++) {        stackTop3.push(log.stack.peek(i));    }    this.lastThreeOpcodes.push({ opcode, stackTop3 });    if (this.lastThreeOpcodes.length > 3) {        this.lastThreeOpcodes.shift();    }        if (log.getGas() < log.getCost() || (                opcode === 'SSTORE' && log.getGas() < 2300)) {        this.currentLevel.oog = true;    }    if (opcode === 'REVERT' || opcode === 'RETURN') {        if (log.getDepth() === 1) {                                    ofs = parseInt(log.stack.peek(0).toString());            len = parseInt(log.stack.peek(1).toString());            data = toHex(log.memory.slice(ofs, ofs + len)).slice(0, 4000);                        this.calls.push({                type: opcode,                gasUsed: 0,                data            });        }                this.lastThreeOpcodes = [];    }    if (log.getDepth() === 1) {        if (opcode === 'CALL' || opcode === 'STATICCALL') {                        addr = toAddress(log.stack.peek(1).toString(16));            topLevelTargetAddress = toHex(addr);                        ofs = parseInt(log.stack.peek(3).toString());                        topLevelMethodSig = toHex(log.memory.slice(ofs, ofs + 4));            this.currentLevel = this.callsFromEntryPoint[this.topLevelCallCounter] = {                topLevelMethodSig,                topLevelTargetAddress,                access: {},                opcodes: {},                extCodeAccessInfo: {},                contractSize: {}            };            this.topLevelCallCounter++;        }        else if (opcode === 'LOG1') {                        topic = log.stack.peek(2).toString(16);            if (topic === this.stopCollectingTopic) {                this.stopCollecting = true;            }        }        this.lastOp = '';        return;    }    lastOpInfo = this.lastThreeOpcodes[this.lastThreeOpcodes.length - 2];        if (((_a = lastOpInfo === null || lastOpInfo === void 0 ? void 0 : lastOpInfo.opcode) === null || _a === void 0 ? void 0 : _a.match(/^(EXT.*)$/)) != null) {        addr = toAddress(lastOpInfo.stackTop3[0].toString(16));        addrHex = toHex(addr);        last3opcodesString = this.lastThreeOpcodes.map(x => x.opcode).join(' ');                        if (last3opcodesString.match(/^(\\w+) EXTCODESIZE ISZERO$/) == null) {            this.currentLevel.extCodeAccessInfo[addrHex] = opcode;                    }        else {                    }    }            isAllowedPrecompiled = (address) => {        addrHex = toHex(address);        addressInt = parseInt(addrHex);                return addressInt > 0 && addressInt < 10;    };        if (opcode.match(/^(EXT.*|CALL|CALLCODE|DELEGATECALL|STATICCALL)$/) != null) {        idx = opcode.startsWith('EXT') ? 0 : 1;        addr = toAddress(log.stack.peek(idx).toString(16));        addrHex = toHex(addr);                if (this.currentLevel.contractSize[addrHex] == null && !isAllowedPrecompiled(addr)) {            this.currentLevel.contractSize[addrHex] = {                contractSize: db.getCode(addr).length,                opcode            };        }    }        if (this.lastOp === 'GAS' && !opcode.includes('CALL')) {                this.countSlot(this.currentLevel.opcodes, 'GAS');    }    if (opcode !== 'GAS') {                if (opcode.match(/^(DUP\\d+|PUSH\\d+|SWAP\\d+|POP|ADD|SUB|MUL|DIV|EQ|LTE?|S?GTE?|SLT|SH[LR]|AND|OR|NOT|ISZERO)$/) == null) {            this.countSlot(this.currentLevel.opcodes, opcode);        }    }    this.lastOp = opcode;    if (opcode === 'SLOAD' || opcode === 'SSTORE') {        slot = toWord(log.stack.peek(0).toString(16));        slotHex = toHex(slot);        addr = log.contract.getAddress();        addrHex = toHex(addr);        access = this.currentLevel.access[addrHex];        if (access == null) {            access = {                reads: {},                writes: {}            };            this.currentLevel.access[addrHex] = access;        }        if (opcode === 'SLOAD') {                                    if (access.reads[slotHex] == null && access.writes[slotHex] == null) {                access.reads[slotHex] = toHex(db.getState(addr, slot));            }        }        else {            this.countSlot(access.writes, slotHex);        }    }    if (opcode === 'KECCAK256') {                ofs = parseInt(log.stack.peek(0).toString());        len = parseInt(log.stack.peek(1).toString());                        if (len > 20 && len < 512) {                        this.keccak.push(toHex(log.memory.slice(ofs, ofs + len)));        }    }    else if (opcode.startsWith('LOG')) {        count = parseInt(opcode.substring(3));        ofs = parseInt(log.stack.peek(0).toString());        len = parseInt(log.stack.peek(1).toString());        topics = [];        for (i = 0; i < count; i++) {                        topics.push('0x' + log.stack.peek(2 + i).toString(16));        }        data = toHex(log.memory.slice(ofs, ofs + len));        this.logs.push({            topics,            data        });    }}}";

    @Autowired
    private DebugTraceCalResultProcessorV6 debugTraceCalResultProcessor;

    @Autowired
    private PoolConfig poolConfig;

    @Autowired
    private ValidationConfig validationConfig;

    public SimulateHandleOpResultOKX simulateHandleOp(UserOperationDO uop, boolean isEstimate) {
        log.info("simulateHandleOp start, param: {}", JSON.toJSONString(uop));
        Function function = new Function("simulateHandleOp", Lists.newArrayList(uop.toDynamicStruct(), Address.DEFAULT, DynamicBytes.DEFAULT), Lists.newArrayList());
        String data = FunctionEncoder.encode(function);
        IChain chain = uop.getChain();

        String entryPoint = uop.getEntryPoint();
        StateOverride stateOverride = new StateOverride();

        OverrideAccount overrideEntrypoint = new OverrideAccount();
        overrideEntrypoint.setCode(poolConfig.getEntrypointRuntimeCodeV6());
        stateOverride.put(entryPoint, overrideEntrypoint);

        if (isEstimate || uop.getPaymasterAndData().equals(Web3Constant.HEX_PREFIX)) {
            OverrideAccount overrideSender = new OverrideAccount();
            overrideSender.setBalance(MAX_BALANCE);
            stateOverride.put(uop.getSender(), overrideSender);
        }

        EthCall call = TransactionUtil.call(null, entryPoint, data, chain, stateOverride);
        ChainErrorMsg chainErrorMsg = ChainErrorUtil.parseChainError(call);
        processExceptSimulateValidationError(chainErrorMsg, uop, isEstimate);

        SimulateHandleOpResultOKX result = new SimulateHandleOpResultOKX(chainErrorMsg);
        log.info("simulateHandleOp end, result: {}", JSON.toJSONString(result));
        return result;

    }

    @Override
    public SimulateValidationResult simulateValidation(UserOperationDO uop, ReferencedCodeHashes previousCodeHashes) {
        log.info("simulateValidation start, param: {}", JSON.toJSONString(uop));
        IChain chain = uop.getChain();
        if (previousCodeHashes != null && CollectionUtils.isNotEmpty(previousCodeHashes.getAddresses())) {
            String codeHashes = getCodeHashes(previousCodeHashes.getAddresses(), chain);
            if (!codeHashes.equals(previousCodeHashes.getHash())) {
                throw new AAException(AAExceptionEnum.OPCODE_VALIDATION, "modified code after first validation");
            }
        }

        BundlerCollectorReturn bundlerCollectorReturn = null;
        Map<String, SlotMap> storageMap = null;

        ChainErrorMsg chainErrorMsg;
        Function function = new Function("simulateValidation", Lists.newArrayList(uop.toDynamicStruct()), Lists.newArrayList());
        String data = FunctionEncoder.encode(function);
        String entryPoint = uop.getEntryPoint();
        if (poolConfig.isSafeMode()) {
            bundlerCollectorReturn = ChainDebugUtil.traceCall(Address.DEFAULT.getValue(), entryPoint, data, uop.resolveGasLimitForValidation(), chain, TRACE_CODE, BundlerCollectorReturn.class);
            FrameInfo lastResult = Iterables.getLast(bundlerCollectorReturn.getCalls());
            if (!OpCodeConstant.REVERT.equals(lastResult.getType())) {
                throw new UnexpectedException("Invalid response. simulateCall must revert");
            }

            String resultData = lastResult.getData();
            if (resultData == null || Web3Constant.HEX_PREFIX.equals(resultData)) {
                throw new UnexpectedException("simulateValidation reverted with no revert string!");
            }

            chainErrorMsg = new ChainErrorMsg(resultData);


        } else {
            EthCall call = TransactionUtil.call(null, uop.resolveGasLimitForValidation(), entryPoint, data, chain);
            chainErrorMsg = ChainErrorUtil.parseChainError(call);
        }

        processExceptSimulateValidationError(chainErrorMsg, uop, false);

        ValidationResult validationResult = new ValidationResult(chainErrorMsg);

        if (bundlerCollectorReturn != null) {
            ScannerResult parseResult = debugTraceCalResultProcessor.parseScannerResult(uop, validationResult, bundlerCollectorReturn);
            storageMap = parseResult.getStorageMap();
            if (previousCodeHashes == null) {
                String codeHashes = getCodeHashes(parseResult.getAddresses(), chain);
                previousCodeHashes = new ReferencedCodeHashes(parseResult.getAddresses(), codeHashes);
            }
        }

        ReturnInfo returnInfo = validationResult.getReturnInfo();
        StakeInfo senderInfo = validationResult.getSenderInfo();
        StakeInfo factoryInfo = uop.getFactory().equals(Web3Constant.HEX_PREFIX) ? null : validationResult.getFactoryInfo();
        StakeInfo paymasterInfo = uop.getPaymaster().equals(Web3Constant.HEX_PREFIX) ? null : validationResult.getPaymasterInfo();
        StakeInfo aggregatorInfo = null;

        SimulateValidationResult result = new SimulateValidationResult(returnInfo, senderInfo, factoryInfo, paymasterInfo, aggregatorInfo, previousCodeHashes, storageMap);
        log.info("simulateValidation end, result: {}", JSON.toJSONString(result));
        return result;

    }


    private void processExceptSimulateValidationError(ChainErrorMsg chainErrorMsg, UserOperationDO uop, boolean isEstimate) {
        if (chainErrorMsg.isMethodId(SimulateHandleOpResultOKX.ERROR_METHOD_ID)) {
            SimulateHandleOpResultOKX handleOpResult = new SimulateHandleOpResultOKX(chainErrorMsg);
//            handleOpResult.tryRevert();
            if (isEstimate) {
                return;
            }
            ValidateTimeUtil.checkValidateTime(handleOpResult.getValidAfter(), handleOpResult.getValidUntil(), validationConfig.getValidUntilFutureSeconds());
            if (handleOpResult.getAggregator().equals(Numeric.toHexStringWithPrefixZeroPadded(BigInteger.ONE, 40))) {
                throw new AAException(AAExceptionEnum.INVALID_SIGNATURE, "Invalid UserOp signature or paymaster signature");
            }

        } else if (ValidationResult.isMatch(chainErrorMsg.getMethodId())) {
            ValidationResult validationResult = new ValidationResult(chainErrorMsg);
            //判断交易过期时间
            ReturnInfo returnInfo = validationResult.getReturnInfo();
            if (returnInfo.isSigFailed()) {
                throw new AAException(AAExceptionEnum.INVALID_SIGNATURE, "Invalid UserOp signature or paymaster signature");
            }

            ValidateTimeUtil.checkValidateTime(returnInfo.getValidAfter(), returnInfo.getValidUntil(), validationConfig.getValidUntilFutureSeconds());
            StakeInfo aggregatorInfo = validationResult.getAggregatorInfo();

            if (aggregatorInfo != null) {
                throw new AAException(new AAExceptionData(AGGREGATOR_TITLE, aggregatorInfo.getAddr()), AAExceptionEnum.UNSUPPORTED_SIGNATURE_AGGREGATOR, "Currently not supporting aggregator");
            }

            // TODO YUKINO 2023/10/24: agg support
//            StakeInfo stakeInfo = aggregatorInfo.getStakeInfo();
//            stakeInfo.setAddr(aggregatorInfo.getAggregator());
//            reputationService.checkStake(AGGREGATOR_TITLE, stakeInfo);

        } else if (chainErrorMsg.isMethodId(FailedOp.ERROR_METHOD_ID)) {
            FailedOp failedOp = new FailedOp(chainErrorMsg);
            String reason = failedOp.getReason();
            if (reason.startsWith("AA3")) {
                throw new AAException(new AAExceptionData(PAYMASTER_TITLE, uop.getPaymaster()), AAExceptionEnum.SIMULATE_PAYMASTER_VALIDATION, "paymaster validation failed: : {}", reason);

            } else {
                throw new AAException(AAExceptionEnum.SIMULATE_VALIDATION, "account validation failed: {}", reason);
            }
        } else {
            UnKnowError unKnowError = new UnKnowError(chainErrorMsg);
            throw new UnexpectedException("validation failed unknown: {}", unKnowError.getMessage());
        }
    }

    private String getCodeHashes(List<String> addresses, IChain chain) {
        DynamicArray<Address> arg = new DynamicArray<>(Address.class, addresses.stream().map(Address::new).collect(Collectors.toList()));
        Function function = new Function("getCodeHashes", Lists.newArrayList(arg), Lists.newArrayList(TypeReference.create(Bytes32.class)));
        String data = FunctionEncoder.encode(function);
        StateOverride stateOverride = new StateOverride();
        OverrideAccount overrideAccount = new OverrideAccount();
        overrideAccount.setCode(GET_CODE_HASHES_RUNTIME_CODE);
        stateOverride.put(GET_CODE_HASHES_ADDRESS, overrideAccount);
        EthCall call = TransactionUtil.call(null, GET_CODE_HASHES_ADDRESS, data, chain, stateOverride);
        ChainErrorMsg chainErrorMsg = ChainErrorUtil.parseChainError(call);
        if (chainErrorMsg != ChainErrorMsg.DEFAULT) {
            throw new UnexpectedException("unable to parse script (error) response: " + chainErrorMsg.getMessage());
        }
        List<Type> result = FunctionReturnDecoder.decode(call.getValue(), function.getOutputParameters());
        return Numeric.toHexString(((Bytes32) result.get(0)).getValue());
    }

}
