package com.okcoin.dapp.bundler.infra.chain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.esaulpaugh.headlong.abi.Tuple;
import org.apache.commons.lang3.StringUtils;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

import java.util.List;

public class PrintUtil {

    public static void print(RlpList rlpList) {
        print(rlpList, 0);
    }

    public static void print(RlpList rlpList, int deep) {
        List<RlpType> values = rlpList.getValues();
        int size = values.size();
        for (int i = 0; i < size; i++) {
            RlpType rlpType = values.get(i);
            if (rlpType instanceof RlpList) {
                print(((RlpList) rlpType), deep + 1);
            } else {
                System.out.println(prefix(deep, i) + ((RlpString) rlpType).asString());
            }
        }

    }

    public static void print(Object json) {
        if (json instanceof String) {
            System.out.println(JSON.toJSONString(JSON.parse((String) json), SerializerFeature.PrettyFormat));
        }

        System.out.println(JSON.toJSONString(json, SerializerFeature.PrettyFormat));
    }

    public static void print(List<Type> types) {
        print(types, 0);
    }

    public static void print(List<Type> types, int deep) {
        for (int i = 0; i < types.size(); i++) {
            Type type = types.get(i);
            if (type instanceof Uint256) {
                Uint256 uint256 = (Uint256) type;
                System.out.println(prefix(deep, i) + uint256.getValue());
            } else if (type instanceof Address) {
                Address address = (Address) type;
                System.out.println(prefix(deep, i) + address.getValue());
            }
        }
    }

    public static void print(Tuple tuple) {
        print(tuple, 0);
    }

    private static void print(Tuple tuple, int deep) {
        int size = tuple.size();
        for (int i = 0; i < size; i++) {
            Object t = tuple.get(i);
            if (t instanceof Tuple[]) {
                System.out.println(prefix(deep, i) + "Tuple[]");
                for (Tuple e : ((Tuple[]) t)) {
                    print(e, deep + 1);
                }
            } else if (t instanceof Tuple) {
                print((Tuple) t, deep + 1);
            } else if (t instanceof byte[]) {
                String v = Numeric.toHexString((byte[]) t);
                System.out.println(prefix(deep, i) + v);
            } else {
                System.out.println(prefix(deep, i) + t);
            }
        }
    }


    private static String prefix(int deep, int i) {
        return StringUtils.repeat('\t', deep) + deep + "." + i + ": ";
    }
}
