package com.okcoin.dapp.bundler.pool.domain.pool;


import com.google.common.base.Objects;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

@Getter
public class MempoolEntryKey implements Comparable<MempoolEntryKey> {

    private final String sender;

    private final BigInteger nonce;

    private final BigInteger maxPriorityFeePerGas;

    private final long timestamp;

    public MempoolEntryKey(String sender, BigInteger nonce, BigInteger maxPriorityFeePerGas) {
        this.sender = sender;
        this.nonce = nonce;
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public int compareTo(@NotNull MempoolEntryKey o) {
        int c = maxPriorityFeePerGas.compareTo(o.maxPriorityFeePerGas);
        if (c != 0) {
            return c;
        }

        if (sender.equals(o.sender)) {
            return -nonce.compareTo(o.nonce);
        }

        return -Long.compare(timestamp, o.timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MempoolEntryKey that = (MempoolEntryKey) o;
        return sender.equals(that.sender) && nonce.equals(that.nonce);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sender, nonce);
    }
}
