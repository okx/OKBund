package com.okcoin.dapp.bundler.rest.api.resp;

import com.okcoin.dapp.bundler.infra.chain.CodecUtil;
import lombok.Data;

@Data
public class ReputationEntryVO {
    /**
     * The address to set the reputation for.
     */
    private String address;
    /**
     * number of times a user operations with that entity was seen and added to the mempool
     */
    private String opsSeen;
    /**
     * number of times a user operations that uses this entity was included on-chain
     */
    private String opsIncluded;
    /**
     * "status - (string) The status of the address in the bundler ‘ok’
     * status - ok/throttled/banned
     */
    private String status;

    public void setAddress(String address) {
        this.address = CodecUtil.toChecksumAddress(address);
    }
}
