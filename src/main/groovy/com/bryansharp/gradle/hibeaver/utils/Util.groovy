package com.bryansharp.gradle.hibeaver.utils

import java.util.regex.Pattern

/**
 * Created by bryansharp(bsp0911932@163.com) on 2016/5/10.
 *
 * @author bryansharp
 * Project: FirstGradle
 * introduction:
 */
public class Util {
    public static boolean regMatch(String pattern, String target) {
        return Pattern.matches(pattern, target);
    }
    /**
     * wildcard匹配模式仅支持星号匹配，*代表任意长度字符
     * TODO 以下写法不严谨 需要考虑startWith 与 endWith
     * @param pattern
     * @param target
     * @return
     */
    public static boolean wildcardMatch(String pattern, String target) {
        String[] split = pattern.split("\\*[1-3]");
        for (int i = 0; i < split.length; i++) {
            String part = split[i]
            if (part == null || part.trim().length() < 1) {
                continue;
            }
            def index = target.indexOf(part)
            if (index < 0) {
                return false;
            }
            def newStart = index + part.length()
            if (newStart < target.length()) {
                target = target.substring(newStart);
            } else {
                target = "";
            }
        }
        return true;
    }

    public static int typeString2Int(String type) {
        if (type == null || "full".equals(type)) {
            return Const.MT_FULL;
        } else if ("regEx".equals(type)) {
            return Const.MT_REGEX;
        } else if ("wildcard".equals(type)) {
            return Const.MT_WILDCARD;
        } else {
            return Const.MT_FULL;
        }
    }

    public static boolean isPatternMatch(String pattern, String type, String target) {
        int intType = typeString2Int(type);
        switch (intType) {
            case Const.MT_FULL:
                if (target.equals(pattern)) {
                    return true;
                }
                break;
            case Const.MT_REGEX:
                if (regMatch(pattern, target)) {
                    return true;
                }
                break;
            case Const.MT_WILDCARD:
                if (wildcardMatch(pattern, target)) {
                    return true;
                }
                break;
        }
        return false;
    }
}