package com.okcoin.dapp.bundler.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.okcoin.dapp.bundler.config.BundleConfig;
import com.okcoin.dapp.bundler.config.OnChainConfig;
import com.okcoin.dapp.bundler.domain.UopBundleDO;
import com.okcoin.dapp.bundler.infra.chain.FieldUtil;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.infra.chain.ReceiptUtil;
import com.okcoin.dapp.bundler.infra.chain.web3j.resp.TransactionReceiptCommon;
import com.okcoin.dapp.bundler.pool.bundler.IBundleService;
import com.okcoin.dapp.bundler.pool.config.ChainConfig;
import com.okcoin.dapp.bundler.pool.config.PoolConfig;
import com.okcoin.dapp.bundler.pool.domain.TxAndOpHashMappingDO;
import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.pool.domain.debug.SimulateValidationResult;
import com.okcoin.dapp.bundler.pool.domain.debug.SlotMap;
import com.okcoin.dapp.bundler.pool.domain.pool.MempoolEntry;
import com.okcoin.dapp.bundler.pool.domain.reputation.ReputationStatusEnum;
import com.okcoin.dapp.bundler.pool.entrypoint.Entrypoint;
import com.okcoin.dapp.bundler.pool.gasprice.GasPriceInfo;
import com.okcoin.dapp.bundler.pool.gasprice.GasService;
import com.okcoin.dapp.bundler.pool.mem.MempoolService;
import com.okcoin.dapp.bundler.pool.reputation.ReputationService;
import com.okcoin.dapp.bundler.pool.result.OnChainTxFailedService;
import com.okcoin.dapp.bundler.pool.simulation.EntryPointSimulationsFactory;
import com.okcoin.dapp.bundler.pool.simulation.IEntryPointSimulations;
import com.okcoin.dapp.bundler.pool.util.AddressUtil;
import com.okcoin.dapp.bundler.pool.util.MathUtil;
import com.okcoin.dapp.bundler.task.logevent.LogEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ClassName BundlerServiceImpl
 * @Author qunqin
 * @Date 2023/10/25
 **/
@Component
@Slf4j
public class BundlerServiceImpl implements IBundleService {

    @Resource
    private MempoolService mempoolService;

    @Resource
    private BundleConfig bundleConfig;

    @Resource
    private GasService gasService;

    @Resource
    private EntryPointSimulationsFactory entryPointSimulationsFactory;

    @Autowired
    private OnChainConfig onChainConfig;

    @Autowired
    private ChainConfig chainConfig;

    @Autowired
    private LogEventService logEventService;

    @Autowired
    private ReputationService reputationService;

    @Autowired
    private PoolConfig poolConfig;

    @Autowired
    private OnChainTxFailedService onChainTxFailedService;

    private static final Long THROTTLED_ENTITY_BUNDLE_COUNT = 4L;


    private UopBundleDO createBundle() {
        List<MempoolEntry> entryList = Lists.newArrayList();
        GasComputer gasComputer = new GasComputer(chainConfig, bundleConfig.getMaxBundleGas());
        Set<String> senders = Sets.newHashSet();
        Map<String, Integer> stakedEntityCount = Maps.newHashMap();
        Map<String, BigInteger> paymasterDeposit = Maps.newHashMap();
        Set<String> knownSenders = mempoolService.getKnownSenders();
        Map<String, SlotMap> storageMap = Maps.newHashMap();
        List<MempoolEntry> priorityEntryList = mempoolService.getAllEntryWithPriority();
        IEntryPointSimulations entryPointSimulations = entryPointSimulationsFactory.get(poolConfig.getEntrypoint());
        BigInteger totalGas = BigInteger.ZERO;

        mainLoop:
        for (MempoolEntry entry : priorityEntryList) {
            UserOperationDO uop = entry.getUop();
            String sender = uop.getSender();
            String paymaster = entry.getUop().getPaymaster();
            String factory = entry.getUop().getFactory();
            ReputationStatusEnum paymasterStatus = reputationService.getStatus(paymaster);
            ReputationStatusEnum factoryStatus = reputationService.getStatus(factory);
            if (paymasterStatus == ReputationStatusEnum.BANNED || factoryStatus == ReputationStatusEnum.BANNED) {
                mempoolService.removeUop(sender, uop.getNonce());
                continue;
            }
            if (!AddressUtil.isEmpty(paymaster) && (paymasterStatus == ReputationStatusEnum.THROTTLED || stakedEntityCount.getOrDefault(paymaster, 0) > THROTTLED_ENTITY_BUNDLE_COUNT)) {
                continue;
            }

            if (!AddressUtil.isEmpty(factory) && (factoryStatus == ReputationStatusEnum.THROTTLED || stakedEntityCount.getOrDefault(factory, 0) > THROTTLED_ENTITY_BUNDLE_COUNT)) {
                continue;
            }

            if (senders.contains(sender)) {
                continue;
            }

            SimulateValidationResult simulateValidationResult;
            try {
                simulateValidationResult = entryPointSimulations.simulateValidation(uop, entry.getReferencedContracts());
            } catch (Exception e) {
                log.error("bundle simulateValidation error", e);
                mempoolService.removeUop(sender, uop.getNonce());
                continue;
            }

            //validate storage sender
            for (String storageAddress : simulateValidationResult.getStorageMap().keySet()) {
                if (!storageAddress.equals(sender) && knownSenders.contains(storageAddress)) {
                    continue mainLoop;
                }
            }

            BigInteger userOpGasCost = simulateValidationResult.getReturnInfo().getPreOpGas().add(uop.getCallGasLimit());
            BigInteger newTotalGas = totalGas.add(userOpGasCost);
            if (newTotalGas.compareTo(bundleConfig.getMaxBundleGas()) > 0) {
                break;
            }

            if (!FieldUtil.isEmpty(paymaster)) {
                if (!paymasterDeposit.containsKey(paymaster)) {
                    paymasterDeposit.put(paymaster, Entrypoint.balanceOf(chainConfig, poolConfig.getEntrypoint(), paymaster));
                }

                if (paymasterDeposit.get(paymaster).compareTo(simulateValidationResult.getReturnInfo().getPrefund()) < 0) {
                    continue;
                }

                stakedEntityCount.compute(paymaster, (k, v) -> {
                    if (v == null) {
                        return 1;
                    } else {
                        return v + 1;
                    }
                });

                paymasterDeposit.put(paymaster,
                        paymasterDeposit.get(paymaster).subtract(simulateValidationResult.getReturnInfo().getPrefund()));
            }

            if (!FieldUtil.isEmpty(factory)) {
                stakedEntityCount.compute(factory, (k, v) -> {
                    if (v == null) {
                        return 1;
                    } else {
                        return v + 1;
                    }
                });
            }

            // TODO YUKINO 2023/10/31: hash root
            mergeStorageMap(storageMap, simulateValidationResult.getStorageMap());
            senders.add(sender);

            totalGas = newTotalGas;
            //calculate gas
            gasComputer.add(uop);
            entryList.add(entry);

        }

        if (entryList.isEmpty()) {
            return UopBundleDO.NULL;
        }
        return new UopBundleDO(entryList, storageMap,
                gasComputer.toTransactionGas(gasService.getGasPriceInfoWithCache(chainConfig).getBaseFee()));
    }

    private boolean checkUopMaxFee(IChain chain, UserOperationDO uop) {
        BigInteger uopMaxPriorityFeePerGas = uop.getMaxPriorityFeePerGas();
        BigInteger uopBaseFee = uop.getMaxFeePerGas().subtract(uopMaxPriorityFeePerGas);
        GasPriceInfo gasPriceInfo = gasService.getGasPriceInfoWithCache(chain);
        BigInteger clientBaseFee = gasPriceInfo.getBaseFee();
        BigDecimal bundlerBaseFeeCoefficient = bundleConfig.getBaseFeeMinCoefficient();
        if (uopBaseFee.compareTo(MathUtil.multiply(clientBaseFee, bundlerBaseFeeCoefficient)) < 0) {
            log.warn("skip to send onchain, chain {}, baseFee is to low, uopHash: {}, uopBaseFee: {}, clientBaseFee: " +
                    "{},  coefficient: {}", chain, uop.getOpHash(), uopBaseFee, clientBaseFee, bundlerBaseFeeCoefficient);
            return false;
        }

        BigInteger clientMaxPriorityFeePerGas = gasPriceInfo.getMaxPriorityFeePerGas();
        BigDecimal bundlerMaxFeePerGasCoefficient = bundleConfig.getMaxPriorityFeePerGasMinCoefficient();
        if (uopMaxPriorityFeePerGas.compareTo(MathUtil.multiply(clientMaxPriorityFeePerGas, bundlerMaxFeePerGasCoefficient)) < 0) {
            log.warn("skip to send onchain, chain {}, maxPriorityFeePerGas is to low, uopHash: {}, " +
                            "uopMaxPriorityFeePerGas: {}, clientMaxPriorityFeePerGas: {},  coefficient: {}", chain, uop.getOpHash(),
                    uopMaxPriorityFeePerGas, clientMaxPriorityFeePerGas, bundlerMaxFeePerGasCoefficient);
            return false;
        }

        return true;
    }

    @Override
    public void handlePastEvents() {
        logEventService.handlePastEvents();
    }

    @Override
    public TxAndOpHashMappingDO sendNextBundle() {
        handlePastEvents();
        UopBundleDO bundle = createBundle();
        if (bundle == UopBundleDO.NULL) {
            return null;
        }
        Credentials bundlerCredential = onChainConfig.getBundlerCredential();
        List<MempoolEntry> entryList = bundle.getMempoolEntryList();
        TransactionGas transactionGas = bundle.getTransactionGas();
        return sendBundle(entryList, bundle.getStorageMap(), transactionGas, bundlerCredential);
    }

    private TxAndOpHashMappingDO sendBundle(List<MempoolEntry> entryList, Map<String, SlotMap> storageMap,
                                            TransactionGas transactionGas, Credentials bundlerCredential) {
        List<UserOperationDO> uopList = entryList.stream().map(MempoolEntry::getUop).collect(Collectors.toList());
        // TODO YUKINO 2023/10/28: 给钱最少得Bundler
        String txHash = Entrypoint.handleOps(uopList, bundlerCredential.getAddress(), onChainConfig.getPrivateKey(), transactionGas.getGasLimit(), transactionGas.getMaxFeePerGas(), transactionGas.getMaxPriorityFeePerGas());
        // sleep one block time
        try {
            Thread.sleep(chainConfig.getBlockTime());
        } catch (InterruptedException e) {
        }
        // TODO YUKINO 2023/10/30: 优化
        TransactionReceiptCommon receipt;
        do {
            receipt = ReceiptUtil.getTransactionReceipt(txHash, chainConfig);
        } while (receipt == null);
        // process failed tx logic
        onChainTxFailedService.processSingleTx(receipt, uopList);
        return new TxAndOpHashMappingDO(txHash, uopList.stream().map(UserOperationDO::getOpHash).collect(Collectors.toList()));
    }

    private void mergeStorageMap(Map<String, SlotMap> mergedStorageMap, Map<String, SlotMap> validationStorageMap) {
        // TODO YUKINO 2023/10/31: rootHash
        for (Map.Entry<String, SlotMap> e : validationStorageMap.entrySet()) {
            String addr = e.getKey();
            SlotMap slotMap = mergedStorageMap.getOrDefault(addr, new SlotMap());
            slotMap.putAll(validationStorageMap.get(addr));
            mergedStorageMap.put(addr, slotMap);
        }
    }
}
