package com.okcoin.dapp.bundler.pool.domain.debug;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yukino.xin
 * @date 2023/10/26 14:31
 */
public class SlotMap extends HashMap<String, String> {

    public SlotMap() {
        super();
    }

    public SlotMap(Map<String, String> m) {
        super(m);
    }
}
