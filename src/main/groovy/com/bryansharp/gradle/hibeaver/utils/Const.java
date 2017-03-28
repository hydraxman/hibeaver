package com.bryansharp.gradle.hibeaver.utils;

/**
 * Created by bsp on 17/3/6.
 */

public interface Const {
    int MT_FULL = 0;
    int MT_WILDCARD = 1;
    int MT_REGEX = 2;
    int TY_AAR = 11;
    int TY_JAR = 12;
    String KEY_CLASSMATCHTYPE="classMatchType";
    String KEY_MODIFYMETHODS="modifyMethods";
    String KEY_METHODNAME="methodName";
    String KEY_METHODMATCHTYPE="methodMatchType";
    String KEY_METHODDESC="methodDesc";
    String KEY_ADAPTER="adapter";
    String VALUE_WILDCARD="wildcard";
    String VALUE_REGEX="regEx";
    String VALUE_ALL="all";
    String WILDCARD_STAR = "\\*{1,3}";
    String WILDCARD_VLINE = "\\|{1,2}";
    String REGEX_STARTER = "r:";
}
