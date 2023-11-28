package com.okcoin.dapp.bundler.infra.chain;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.Decoder;
import com.aayushatharva.brotli4j.decoder.DirectDecompress;
import com.aayushatharva.brotli4j.encoder.Encoder;
import com.esaulpaugh.headlong.abi.Address;
import com.esaulpaugh.headlong.abi.Function;
import com.esaulpaugh.headlong.abi.Tuple;
import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;
import lombok.SneakyThrows;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

public class CodecUtil {

    static {
        Brotli4jLoader.ensureAvailability();
    }

    public static String abiEncodePacked(Type... parameters) {
        StringBuilder sb = new StringBuilder();
        for (Type parameter : parameters) {
            sb.append(TypeEncoder.encodePacked(parameter));
        }
        return sb.toString();
    }

    public static String abiEncode(Type... parameters) {
        return TypeEncoder.encode(new DynamicStruct(parameters));
    }


    public static List<Type> decodeError(String hexData, Event event) {
        String error = hexData.substring(Web3Constant.METHOD_ID_LENGTH);
        return FunctionReturnDecoder.decode(error, event.getParameters());
    }

    public static Tuple decodeFunctionOrError(String input, String methodSig) {
        Function func = Function.parse(methodSig.replace(" ", ""));
        return func.decodeCall(Numeric.hexStringToByteArray(input));
    }

    public static byte[] keccak256(String input) {
        return Hash.sha3(Numeric.hexStringToByteArray(input));
    }

    public static byte[] keccak256(byte[] input) {
        return Hash.sha3(input);
    }

    public static boolean hasValidMethodId(String value) {
        return value.length() >= Web3Constant.METHOD_ID_LENGTH;
    }

    public static String buildMethodId(String methodSignature) {
        final byte[] input = methodSignature.getBytes();
        final byte[] hash = keccak256(input);
        return Numeric.toHexString(hash).substring(0, 10);
    }

    public static String toHexStringWithPrefix(int n) {
        return Numeric.encodeQuantity(BigInteger.valueOf(n));
    }

    public static String toHexStringWithPrefix(long n) {
        return Numeric.encodeQuantity(BigInteger.valueOf(n));
    }

    @SneakyThrows
    public static byte[] brotliFastCompress(byte[] bytes) {
        return brotliCompress(bytes, 0, 22);
    }

    @SneakyThrows
    public static byte[] brotliCompress(byte[] bytes, int quality, int lgWin) {
        Encoder.Parameters parameters = new Encoder.Parameters();
        parameters.setQuality(quality);
        parameters.setWindow(lgWin);
        parameters.setMode(Encoder.Mode.GENERIC);
        return Encoder.compress(bytes, parameters);
    }

    @SneakyThrows
    public static byte[] brotliDecompress(byte[] bytes) {
        DirectDecompress decompress = Decoder.decompress(bytes);
        return decompress.getDecompressedData();
    }


    public static String create2(byte[] salt, String bytecodeHash, String deployer) {
        byte[] input = Numeric.hexStringToByteArray("0xff" + Numeric.cleanHexPrefix(deployer) + Numeric.toHexStringNoPrefix(salt) + Numeric.cleanHexPrefix(bytecodeHash));
        byte[] addressBytes = Hash.sha3(input);
        return Numeric.toHexString(addressBytes, 12, 20, true);
    }

    public static String toChecksumAddress(String address) {
        return Address.toChecksumAddress(address);
    }


}
