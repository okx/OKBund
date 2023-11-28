package com.okcoin.dapp.bundler.rest.api.service.impl.debug;

import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.pool.mem.MempoolService;
import com.okcoin.dapp.bundler.rest.api.resp.UserOperationVO;
import com.okcoin.dapp.bundler.rest.api.service.AAMethodProcessor;
import com.okcoin.dapp.bundler.rest.util.UopUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.okcoin.dapp.bundler.rest.constant.AaMethodConstant.DEBUG_BUNDLER_DUMP_MEMPOOL;

@Service(DEBUG_BUNDLER_DUMP_MEMPOOL)
@Slf4j
public class DebugBundlerDumpMempoolService implements AAMethodProcessor {

    @Autowired
    private MempoolService mempoolService;

    @Override
    public List<UserOperationVO> process(IChain chain, List<Object> params) {
        String entrypoint = ((String) params.get(0)).toLowerCase();
        List<UserOperationDO> userOperationDOS = mempoolService.dump();
        return UopUtil.convertDoToUserOperationVO(userOperationDOS);
    }
}
