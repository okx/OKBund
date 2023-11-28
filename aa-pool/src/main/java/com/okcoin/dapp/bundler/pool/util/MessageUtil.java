package com.okcoin.dapp.bundler.pool.util;

import com.okcoin.dapp.bundler.infra.chain.CodecUtil;
import com.okcoin.dapp.bundler.infra.chain.FieldUtil;

/**
 * @author Yukino.Xin on 2023/10/30 21:03
 */
public class MessageUtil {

    public static String format(String message, Object... args) {
        if (message == null || args == null || args.length == 0) {
            return message;
        }

        StringBuilder builder = new StringBuilder();
        int argIndex = 0;
        for (int i = 0; i < message.length(); i++) {
            if (i < message.length() - 1 && message.charAt(i) == '{' && message.charAt(i + 1) == '}') {
                if (argIndex < args.length) {
                    Object arg = args[argIndex++];
                    if (arg instanceof String && FieldUtil.isValidAddress((String) arg)) {
                        builder.append(CodecUtil.toChecksumAddress((String) arg));
                    } else {
                        builder.append(arg);
                    }
                } else {
                    builder.append("{}");
                }
                i++;
            } else {
                builder.append(message.charAt(i));
            }
        }

        return builder.toString();
    }
}
