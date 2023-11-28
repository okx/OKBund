package com.okcoin.dapp.bundler.rest.aspect;

import java.lang.annotation.*;


@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MultiChainRestLog {

}
