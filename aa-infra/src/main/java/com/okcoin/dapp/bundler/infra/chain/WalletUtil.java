package com.okcoin.dapp.bundler.infra.chain;

import org.web3j.crypto.*;
import org.web3j.utils.Numeric;

import java.security.SecureRandom;

public class WalletUtil {

    /**
     * generate a random group of mnemonics
     * 生成一组随机的助记词
     */
    public static String generateMnemonics() {
        byte[] initialEntropy = new byte[16];
        new SecureRandom().nextBytes(initialEntropy);

        return MnemonicUtils.generateMnemonic(initialEntropy);
    }

    public static byte[] generateSeed(String mnemonic) {
        return MnemonicUtils.generateSeed(mnemonic, "");
    }

    public static Bip32ECKeyPair createBip32ECKeyPair(byte[] seed) {
        return Bip32ECKeyPair.generateKeyPair(seed);
    }

    public static Bip32ECKeyPair createBip44ECKeyPair(byte[] seed) {
        Bip32ECKeyPair masterKeypair = Bip32ECKeyPair.generateKeyPair(seed);
        return Bip44WalletUtils.generateBip44KeyPair(masterKeypair, false);

    }

    public static Credentials generateBip44Credentials(String mnemonic, int index) {
        byte[] seed = generateSeed(mnemonic);
        Bip32ECKeyPair master = createBip32ECKeyPair(seed);
        int[] path = {44 | Bip32ECKeyPair.HARDENED_BIT, 60 | Bip32ECKeyPair.HARDENED_BIT, 0 | Bip32ECKeyPair.HARDENED_BIT, 0, index};
        Bip32ECKeyPair bip44ECKeyPair = Bip32ECKeyPair.deriveKeyPair(master, path);
        return Credentials.create(bip44ECKeyPair);
    }

    public static byte[] toEthSignedMessageHash(byte[] message) {
        return Sign.getEthereumMessageHash(message);
    }

    public static Sign.SignatureData signPrefixedMessage(String message, ECKeyPair keyPair) {
        return Sign.signPrefixedMessage(Numeric.hexStringToByteArray(message), keyPair);
    }

    public static Sign.SignatureData signPrefixedMessage(byte[] message, ECKeyPair keyPair) {
        return Sign.signPrefixedMessage(message, keyPair);
    }

    public static Sign.SignatureData signMessage(String message, ECKeyPair keyPair) {
        return Sign.signMessage(Numeric.hexStringToByteArray(message), keyPair, false);
    }

    public static Sign.SignatureData signMessage(byte[] message, ECKeyPair keyPair) {
        return Sign.signMessage(message, keyPair, false);
    }

    public static byte[] signToBytes(Sign.SignatureData data) {
        int rL = data.getR().length;
        int sL = data.getS().length;
        int vL = data.getV().length;
        byte[] bytes = new byte[rL + sL + vL];
        System.arraycopy(data.getR(), 0, bytes, 0, rL);
        System.arraycopy(data.getS(), 0, bytes, rL, sL);
        System.arraycopy(data.getV(), 0, bytes, rL + sL, vL);
        return bytes;
    }

    public static String signToString(Sign.SignatureData data) {
        return Numeric.toHexString(signToBytes(data));
    }

    public static String signToStringNoPrefix(Sign.SignatureData data) {
        return Numeric.toHexStringNoPrefix(signToBytes(data));
    }
}
