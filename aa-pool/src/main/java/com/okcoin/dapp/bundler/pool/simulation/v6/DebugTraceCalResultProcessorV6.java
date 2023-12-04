package com.okcoin.dapp.bundler.pool.simulation.v6;

import com.alibaba.fastjson2.JSON;
import com.esaulpaugh.headlong.abi.ABIObject;
import com.esaulpaugh.headlong.abi.Tuple;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.okcoin.dapp.bundler.infra.chain.CodecUtil;
import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;
import com.okcoin.dapp.bundler.pool.config.AbiConfig;
import com.okcoin.dapp.bundler.pool.constant.Eip4377MethodConstant;
import com.okcoin.dapp.bundler.pool.constant.OpCodeConstant;
import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.pool.domain.debug.*;
import com.okcoin.dapp.bundler.pool.domain.error.StakeInfo;
import com.okcoin.dapp.bundler.pool.domain.error.ValidationResult;
import com.okcoin.dapp.bundler.pool.exception.AAException;
import com.okcoin.dapp.bundler.pool.exception.AAExceptionData;
import com.okcoin.dapp.bundler.pool.exception.AAExceptionEnum;
import com.okcoin.dapp.bundler.pool.exception.UnexpectedException;
import com.okcoin.dapp.bundler.pool.reputation.ReputationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.okcoin.dapp.bundler.pool.constant.Eip4377CommonConstant.*;
import static com.okcoin.dapp.bundler.pool.constant.OpCodeConstant.*;

@Component
public class DebugTraceCalResultProcessorV6 {

    private static final Set<String> BANNED_OP_CODES = Sets.newHashSet(GASPRICE, GASLIMIT, DIFFICULTY, TIMESTAMP, BASEFEE, BLOCKHASH, NUMBER, SELFBALANCE, BALANCE, ORIGIN, GAS, CREATE, COINBASE, SELFDESTRUCT, RANDOM, PREVRANDAO, INVALID);

    private static final Map<String, String> CALLS_FROM_ENTRY_POINT_METHOD_SIGS = ImmutableMap.of(
            FACTORY_TITLE, Eip4377MethodConstant.CREATE_SENDER,
            ACCOUNT_TITLE, Eip4377MethodConstant.VALIDATE_USER_OP,
            PAYMASTER_TITLE, Eip4377MethodConstant.VALIDATE_PAYMASTER_USER_OP);

    @Autowired
    private AbiConfig abiConfig;

    @Autowired
    private ReputationService reputationService;

    public ScannerResult parseScannerResult(UserOperationDO uop, ValidationResult validationResult, BundlerCollectorReturn tracerResults) {
        String entryPointAddress = uop.getEntryPoint();

        if (tracerResults.getCallsFromEntryPoint().isEmpty()) {
            throw new UnexpectedException("Unexpected traceCall result: no calls from entrypoint.");
        }

        List<CallEntry> callStack = parseCallStack(tracerResults.getCalls());
        // [OP-052], [OP-053]
        CallEntry callInfoEntryPoint = callStack.stream()
                .filter(call -> entryPointAddress.equals(call.getTo()) && !entryPointAddress.equals(call.getFrom())
                        && (!Web3Constant.HEX_PREFIX.equals(call.getMethod()) && !Eip4377MethodConstant.DEPOSIT_TO.equals(call.getMethod())))
                .findFirst().orElse(null);

        // [OP-054]
        if (callInfoEntryPoint != null) {
            String method = callInfoEntryPoint.getMethod();
            ABIObject func = abiConfig.getFunc(method);
            throw new AAException(AAExceptionEnum.OPCODE_VALIDATION, "illegal call into EntryPoint during validation {}", func == null ? method : func.getName());
        }

        // [OP-061]
        CallEntry illegalNonZeroValueCall = callStack.stream().filter(call -> !entryPointAddress.equals(call.getTo())
                        && (call.getValue() != null && !BigInteger.ZERO.equals(call.getValue())))
                .findFirst().orElse(null);

        if (illegalNonZeroValueCall != null) {
            throw new AAException(AAExceptionEnum.OPCODE_VALIDATION, "May not may CALL with value");
        }

        String sender = uop.getSender();

        Map<String, StakeInfo> stakeInfoEntities = Maps.newHashMap();
        StakeInfo factoryInfo = validationResult.getFactoryInfo();
        factoryInfo.setAddr(uop.getFactory());
        StakeInfo senderInfo = validationResult.getSenderInfo();
        senderInfo.setAddr(sender);
        StakeInfo paymasterInfo = validationResult.getPaymasterInfo();
        paymasterInfo.setAddr(uop.getPaymaster());

        stakeInfoEntities.put(FACTORY_TITLE, factoryInfo);
        stakeInfoEntities.put(ACCOUNT_TITLE, senderInfo);
        stakeInfoEntities.put(PAYMASTER_TITLE, paymasterInfo);
        Map<String, Set<String>> entitySlots = parseEntitySlots(stakeInfoEntities, tracerResults.getKeccak());

        for (Map.Entry<String, StakeInfo> e : stakeInfoEntities.entrySet()) {
            String entityTitle = e.getKey();
            StakeInfo entStake = e.getValue();
            String entityAddr = entStake.getAddr();

            TopLevelCallInfo currentNumLevel = tracerResults.getCallsFromEntryPoint().stream()
                    .filter(info -> info.getTopLevelMethodSig().equals(CALLS_FROM_ENTRY_POINT_METHOD_SIGS.get(entityTitle)))
                    .findFirst().orElse(null);
            if (currentNumLevel == null) {
                if (ACCOUNT_TITLE.equals(entityTitle)) {
                    throw new UnexpectedException("missing trace into validateUserOp");
                }
                continue;
            }

            // [OP-020]
            if (currentNumLevel.isOog()) {
                throw new AAException(AAExceptionEnum.OPCODE_VALIDATION, "{} internally reverts on oog", entityTitle);
            }

            Map<String, Integer> opcodes = currentNumLevel.getOpcodes();

            // [OP-011]
            for (String opcode : opcodes.keySet()) {
                if (BANNED_OP_CODES.contains(opcode)) {
                    throw new AAException(AAExceptionEnum.OPCODE_VALIDATION, "{} uses banned opcode: {}", entityTitle, opcode);
                }
            }

            // [OP-031]
            if (FACTORY_TITLE.equals(entityTitle)) {
                if (opcodes.getOrDefault(CREATE2, 0) > 1) {
                    throw new AAException(AAExceptionEnum.OPCODE_VALIDATION, "{} with too many CREATE2", entityTitle);
                }
            } else {
                if (opcodes.containsKey(CREATE2)) {
                    throw new AAException(AAExceptionEnum.OPCODE_VALIDATION, "{} uses banned opcode: CREATE2", entityTitle);
                }
            }

            Map<String, AccessInfo> access = currentNumLevel.getAccess();
            for (Map.Entry<String, AccessInfo> ee : access.entrySet()) {
                String addr = ee.getKey();
                Map<String, String> reads = ee.getValue().getReads();
                Map<String, Integer> writes = ee.getValue().getWrites();
                if (sender.equals(addr)) {
                    // [STO-010]
                    continue;
                }

                if (entryPointAddress.equals(addr)) {
                    continue;
                }

                String requireStakeSlot = null;
                List<String> slots = Lists.newArrayList();
                slots.addAll(writes.keySet());
                slots.addAll(reads.keySet());
                for (String slot : slots) {
                    if (associatedWith(slot, sender, entitySlots)) {
                        if (uop.getInitCode().length() > 2) {
                            // [STO-022], [STO-021]
                            if (!(sender.equals(entityAddr) && isStaked(stakeInfoEntities.get(FACTORY_TITLE)))) {
                                requireStakeSlot = slot;
                            }
                        }
                    } else if (associatedWith(slot, entityAddr, entitySlots)) {
                        // [STO-032]
                        requireStakeSlot = slot;
                    } else if (addr.equals(entityAddr)) {
                        // [STO-031]
                        requireStakeSlot = slot;
                    } else if (!writes.containsKey(slot)) {
                        // [STO-033]
                        requireStakeSlot = slot;
                    } else {
                        String readWrite = writes.containsKey(addr) ? "write to" : "read from";
                        throw new AAException(new AAExceptionData(entityTitle, entStake.getAddr()), AAExceptionEnum.OPCODE_VALIDATION, "{} has forbidden {} {} slot {}", entityTitle, readWrite, nameAddr(addr, stakeInfoEntities), slot);
                    }
                }

                requireCondAndStake(requireStakeSlot != null, entStake, entityTitle, access, "unstaked {} accessed {} slot {}", entityTitle, nameAddr(addr, stakeInfoEntities), requireStakeSlot);
            }

            // [EREP-050]
            if (PAYMASTER_TITLE.equals(entityTitle)) {
                CallEntry validatePaymasterUserOp = callStack.stream()
                        .filter(call -> Eip4377MethodConstant.VALIDATE_PAYMASTER_USER_OP.equals(call.getMethod()) && entityAddr.equals(call.getTo()))
                        .findFirst().orElse(null);
                String context = Web3Constant.HEX_PREFIX;
                if (validatePaymasterUserOp != null) {
                    Tuple decodeReturn = abiConfig.getFunc(Eip4377MethodConstant.VALIDATE_PAYMASTER_USER_OP).decodeReturn(Numeric.hexStringToByteArray(validatePaymasterUserOp.getResult()));
                    context = Numeric.toHexString(decodeReturn.get(0));
                }
                requireCondAndStake(!Web3Constant.HEX_PREFIX.equals(context), entStake, entityTitle, access, "unstaked paymaster must not return context");
            }

            ContractSizeInfo illegalZeroCodeAccess = null;
            String illegalZeroCodeAccessAddress = null;

            for (Map.Entry<String, ContractSizeInfo> ee : currentNumLevel.getContractSize().entrySet()) {
                String addr = ee.getKey();
                // [OP-042]
                if (!sender.equals(addr) && ee.getValue().getContractSize() <= 2) {
                    illegalZeroCodeAccess = ee.getValue();
                    illegalZeroCodeAccessAddress = addr;
                    break;
                }
            }

            // [OP-041]
            if (illegalZeroCodeAccess != null) {
                throw new AAException(AAExceptionEnum.OPCODE_VALIDATION, "{} accesses un-deployed contract address {} with opcode {}", entityTitle, illegalZeroCodeAccessAddress, illegalZeroCodeAccess.getOpcode());
            }

            String illegalEntryPointCodeAccess = null;
            for (Map.Entry<String, String> ee : currentNumLevel.getExtCodeAccessInfo().entrySet()) {
                String addr = ee.getKey();
                if (entryPointAddress.equals(addr)) {
                    illegalEntryPointCodeAccess = ee.getValue();
                    break;
                }
            }

            if (illegalEntryPointCodeAccess != null) {
                throw new AAException(AAExceptionEnum.OPCODE_VALIDATION, "{} accesses EntryPoint contract address {} with opcode {}", entityTitle, entryPointAddress, illegalEntryPointCodeAccess);
            }
        }

        List<String> addresses = tracerResults.getCallsFromEntryPoint().stream().flatMap(level -> level.getContractSize().keySet().stream()).collect(Collectors.toList());
        Map<String, SlotMap> storageMap = Maps.newHashMap();

        for (TopLevelCallInfo level : tracerResults.getCallsFromEntryPoint()) {
            for (Map.Entry<String, AccessInfo> e : level.getAccess().entrySet()) {
                String addr = e.getKey();
                storageMap.computeIfAbsent(addr, (key) -> new SlotMap(e.getValue().getReads()));
            }
        }

        return new ScannerResult(addresses, storageMap);
    }

    private void requireCondAndStake(boolean cond, StakeInfo entStake, String entityTitle, Map<String, AccessInfo> access, String failureMessageFormat, Object... failureMessageArgs) {
        if (!cond) {
            return;
        }
        if (entStake == null) {
            throw new UnexpectedException("internal: {} not in userOp, but has storage accesses in {}", entityTitle, JSON.toJSONString(access));
        }
        if (!isStaked(entStake)) {
            throw new AAException(new AAExceptionData(entityTitle, entStake.getAddr()), AAExceptionEnum.OPCODE_VALIDATION, failureMessageFormat, failureMessageArgs);
        }

        reputationService.checkStake(entityTitle, entStake);
    }

    private String nameAddr(String addr, Map<String, StakeInfo> stakeInfoEntities) {
        String title = stakeInfoEntities.entrySet().stream()
                .filter(e -> e.getValue().getAddr().equals(addr)).map(Map.Entry::getKey)
                .findFirst().orElse(null);
        if (title == null) {
            return addr;
        }
        return title;
    }

    private boolean isStaked(StakeInfo entStake) {
        return entStake != null && entStake.getStake().compareTo(BigInteger.ZERO) > 0 && entStake.getUnstakeDelaySec() > 0;
    }

    private boolean associatedWith(String slot, String addr, Map<String, Set<String>> entitySlots) {
        String addrPadded = hexZeroPad(addr, 32);

        if (slot.equals(addrPadded)) {
            return true;
        }

        Set<String> k = entitySlots.get(addr);
        if (k == null) {
            return false;
        }

        BigInteger slotN = Numeric.decodeQuantity(slot);
        for (String k1 : k) {
            BigInteger kn = Numeric.decodeQuantity(k1);
            if (slotN.compareTo(kn) >= 0 && slotN.compareTo(kn.add(BigInteger.valueOf(128))) < 0) {
                return true;
            }
        }

        return false;

    }


    private Map<String, Set<String>> parseEntitySlots(Map<String, StakeInfo> stakeInfoEntities, List<String> keccak) {
        Map<String, Set<String>> entitySlots = Maps.newHashMap();
        for (String k : keccak) {
            for (StakeInfo info : stakeInfoEntities.values()) {
                String addr = info.getAddr();
                if (addr == null || Web3Constant.HEX_PREFIX.equals(addr)) {
                    continue;
                }

                Set<String> currentEntitySlots = entitySlots.computeIfAbsent(addr, (key) -> Sets.newHashSet());
                String addrPadded = hexZeroPad(addr, 32);

                if (k.startsWith(addrPadded)) {
                    currentEntitySlots.add(Numeric.toHexString(CodecUtil.keccak256(k)));
                }

            }
        }

        return entitySlots;
    }

    private List<CallEntry> parseCallStack(List<FrameInfo> calls) {
        List<CallEntry> out = Lists.newArrayList();
        LinkedList<FrameInfo> stack = Lists.newLinkedList();
        calls = calls.stream().filter(x -> !x.getType().startsWith("depth")).collect(Collectors.toList());
        for (FrameInfo c : calls) {
            if (!c.isExit()) {
                stack.add(c);
                continue;
            }

            FrameInfo top;
            if (stack.isEmpty()) {
                top = new FrameInfo();
                top.setType("top");
                top.setMethod(Eip4377MethodConstant.VALIDATE_USER_OP);
            } else {
                top = stack.pollLast();
            }

            String returnData = c.getData();
            if (top.getType().startsWith(OpCodeConstant.CREATE)) {
                CallEntry callEntry = new CallEntry(top.getTo(), top.getFrom(), top.getType(), "", null);
                callEntry.setResult("len=" + (returnData.length() - 2) / 2);
                out.add(callEntry);

            } else if (OpCodeConstant.REVERT.equals(c.getType())) {
                CallEntry callEntry = new CallEntry(top.getTo(), top.getFrom(), top.getType(), top.getMethod(), top.getValue());
                callEntry.setRevert(returnData);
                out.add(callEntry);
            } else {
                CallEntry callEntry = new CallEntry(top.getTo(), top.getFrom(), top.getType(), top.getMethod(), top.getValue());
                callEntry.setResult(returnData);
                out.add(callEntry);
            }
        }

        return out;
    }

    private String hexZeroPad(String addr, int length) {
        return Numeric.toHexStringWithPrefixZeroPadded(Numeric.decodeQuantity(addr), length * 2);

    }
}
