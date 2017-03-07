package com.bryansharp.gradle.hibeaver.utils

import org.apache.commons.io.IOUtils

import java.lang.reflect.Array

public class Log {

    static void setQuiet(boolean quiet) {
        //此处如果不加@会导致循环调用
        Log.@quiet = quiet
    }

    static void setShowHelp(boolean showHelp) {
        //此处如果不加@会导致循环调用
        Log.@showHelp = showHelp
    }
    def static boolean quiet = false
    def static boolean showHelp = false
    def static boolean learnMode = false

    def static logHelp() {
        if (quiet) return;
        if (!showHelp) return;
        try {
            println("");
            def stream = Log.class.getClassLoader().getResourceAsStream('helpContent.groovy')
            def helpContent = new String(IOUtils.toByteArray(stream), 'UTF-8');
            println helpContent;
        } catch (Exception e) {
        }
    }

    def static learn(Object msg) {
        if (quiet) return
        if (learnMode) {
            println "learn:${msg}"
        }
    }

    def static info(Object msg) {
        if (quiet) return
        try {
            println "${msg}"
        } catch (Exception e) {
        }
    }

    def static logEach(Object... msg) {
        if (quiet) return
        msg.each {
            Object m ->
                try {
                    if (m != null) {
                        if (m.class.isArray()) {
                            print "["
                            def length = Array.getLength(m);
                            if (length > 0) {
                                for (int i = 0; i < length; i++) {
                                    def get = Array.get(m, i);
                                    if (get != null) {
                                        print "${get}\t"
                                    } else {
                                        print "null\t"
                                    }
                                }
                            }
                            print "]\t"
                        } else {
                            print "${m}\t"
                        }
                    } else {
                        print "null\t"
                    }
                } catch (Exception e) {
                }
        }
        println ""

    }

    def static String getOpName(int opCode) {
        return getOpMap().get(opCode);
    }
    static HashMap<Integer, String> opCodeMap = new HashMap<>();

    public static HashMap<Integer, String> accCodeMap = new HashMap<>();

    public static Map<Integer, String> getAccCodeMap() {
        if (accCodeMap.size() == 0) {
            HashMap<String, Integer> map = new HashMap<>();
            map.put("ACC_PUBLIC", 1);
            map.put("ACC_PRIVATE", 2);
            map.put("ACC_PROTECTED", 4);
            map.put("ACC_STATIC", 8);
            map.put("ACC_FINAL", 16);
            map.put("ACC_SUPER", 32);
            map.put("ACC_SYNCHRONIZED", 32);
            map.put("ACC_VOLATILE", 64);
            map.put("ACC_BRIDGE", 64);
            map.put("ACC_VARARGS", 128);
            map.put("ACC_TRANSIENT", 128);
            map.put("ACC_NATIVE", 256);
            map.put("ACC_INTERFACE", 512);
            map.put("ACC_ABSTRACT", 1024);
            map.put("ACC_STRICT", 2048);
            map.put("ACC_SYNTHETIC", 4096);
            map.put("ACC_ANNOTATION", 8192);
            map.put("ACC_ENUM", 16384);
            map.put("ACC_DEPRECATED", 131072);
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                accCodeMap.put(entry.getValue(), entry.getKey());
            }
        }
        return accCodeMap;
    }

    public static String accCode2String(int access) {
        def builder = new StringBuilder();
        def map = getAccCodeMap();
        map.entrySet().each {
            entry ->
                if ((entry.getKey().intValue() & access) > 0) {
                    //此处如果使用|作为分隔符会导致编译报错 因此改用斜杠
                    builder.append('\\' + entry.getValue() + '/ ');
                }
        }
        return builder.toString();
    }

    public static Map<Integer, String> getOpMap() {
        if (opCodeMap.size() == 0) {
            HashMap<String, Integer> map = new HashMap<>();
//            map.put("T_BOOLEAN", 4);
//            map.put("T_CHAR", 5);
//            map.put("T_FLOAT", 6);
//            map.put("T_DOUBLE", 7);
//            map.put("T_BYTE", 8);
//            map.put("T_SHORT", 9);
//            map.put("T_INT", 10);
//            map.put("T_LONG", 11);
//            map.put("F_NEW", -1);
//            map.put("F_FULL", 0);
//            map.put("F_APPEND", 1);
//            map.put("F_CHOP", 2);
//            map.put("F_SAME", 3);
//            map.put("F_SAME1", 4);
//            map.put("TOP", 0);
//            map.put("INTEGER", 1);
//            map.put("FLOAT", 2);
//            map.put("DOUBLE", 3);
//            map.put("LONG", 4);
//            map.put("NULL", 5);
//            map.put("UNINITIALIZED_THIS", 6);
            map.put("NOP", 0);
            map.put("ACONST_NULL", 1);
            map.put("ICONST_M1", 2);
            map.put("ICONST_0", 3);
            map.put("ICONST_1", 4);
            map.put("ICONST_2", 5);
            map.put("ICONST_3", 6);
            map.put("ICONST_4", 7);
            map.put("ICONST_5", 8);
            map.put("LCONST_0", 9);
            map.put("LCONST_1", 10);
            map.put("FCONST_0", 11);
            map.put("FCONST_1", 12);
            map.put("FCONST_2", 13);
            map.put("DCONST_0", 14);
            map.put("DCONST_1", 15);
            map.put("BIPUSH", 16);
            map.put("SIPUSH", 17);
            map.put("LDC", 18);
            map.put("ILOAD", 21);
            map.put("LLOAD", 22);
            map.put("FLOAD", 23);
            map.put("DLOAD", 24);
            map.put("ALOAD", 25);
            map.put("IALOAD", 46);
            map.put("LALOAD", 47);
            map.put("FALOAD", 48);
            map.put("DALOAD", 49);
            map.put("AALOAD", 50);
            map.put("BALOAD", 51);
            map.put("CALOAD", 52);
            map.put("SALOAD", 53);
            map.put("ISTORE", 54);
            map.put("LSTORE", 55);
            map.put("FSTORE", 56);
            map.put("DSTORE", 57);
            map.put("ASTORE", 58);
            map.put("IASTORE", 79);
            map.put("LASTORE", 80);
            map.put("FASTORE", 81);
            map.put("DASTORE", 82);
            map.put("AASTORE", 83);
            map.put("BASTORE", 84);
            map.put("CASTORE", 85);
            map.put("SASTORE", 86);
            map.put("POP", 87);
            map.put("POP2", 88);
            map.put("DUP", 89);
            map.put("DUP_X1", 90);
            map.put("DUP_X2", 91);
            map.put("DUP2", 92);
            map.put("DUP2_X1", 93);
            map.put("DUP2_X2", 94);
            map.put("SWAP", 95);
            map.put("IADD", 96);
            map.put("LADD", 97);
            map.put("FADD", 98);
            map.put("DADD", 99);
            map.put("ISUB", 100);
            map.put("LSUB", 101);
            map.put("FSUB", 102);
            map.put("DSUB", 103);
            map.put("IMUL", 104);
            map.put("LMUL", 105);
            map.put("FMUL", 106);
            map.put("DMUL", 107);
            map.put("IDIV", 108);
            map.put("LDIV", 109);
            map.put("FDIV", 110);
            map.put("DDIV", 111);
            map.put("IREM", 112);
            map.put("LREM", 113);
            map.put("FREM", 114);
            map.put("DREM", 115);
            map.put("INEG", 116);
            map.put("LNEG", 117);
            map.put("FNEG", 118);
            map.put("DNEG", 119);
            map.put("ISHL", 120);
            map.put("LSHL", 121);
            map.put("ISHR", 122);
            map.put("LSHR", 123);
            map.put("IUSHR", 124);
            map.put("LUSHR", 125);
            map.put("IAND", 126);
            map.put("LAND", 127);
            map.put("IOR", 128);
            map.put("LOR", 129);
            map.put("IXOR", 130);
            map.put("LXOR", 131);
            map.put("IINC", 132);
            map.put("I2L", 133);
            map.put("I2F", 134);
            map.put("I2D", 135);
            map.put("L2I", 136);
            map.put("L2F", 137);
            map.put("L2D", 138);
            map.put("F2I", 139);
            map.put("F2L", 140);
            map.put("F2D", 141);
            map.put("D2I", 142);
            map.put("D2L", 143);
            map.put("D2F", 144);
            map.put("I2B", 145);
            map.put("I2C", 146);
            map.put("I2S", 147);
            map.put("LCMP", 148);
            map.put("FCMPL", 149);
            map.put("FCMPG", 150);
            map.put("DCMPL", 151);
            map.put("DCMPG", 152);
            map.put("IFEQ", 153);
            map.put("IFNE", 154);
            map.put("IFLT", 155);
            map.put("IFGE", 156);
            map.put("IFGT", 157);
            map.put("IFLE", 158);
            map.put("IF_ICMPEQ", 159);
            map.put("IF_ICMPNE", 160);
            map.put("IF_ICMPLT", 161);
            map.put("IF_ICMPGE", 162);
            map.put("IF_ICMPGT", 163);
            map.put("IF_ICMPLE", 164);
            map.put("IF_ACMPEQ", 165);
            map.put("IF_ACMPNE", 166);
            map.put("GOTO", 167);
            map.put("JSR", 168);
            map.put("RET", 169);
            map.put("TABLESWITCH", 170);
            map.put("LOOKUPSWITCH", 171);
            map.put("IRETURN", 172);
            map.put("LRETURN", 173);
            map.put("FRETURN", 174);
            map.put("DRETURN", 175);
            map.put("ARETURN", 176);
            map.put("RETURN", 177);
            map.put("GETSTATIC", 178);
            map.put("PUTSTATIC", 179);
            map.put("GETFIELD", 180);
            map.put("PUTFIELD", 181);
            map.put("INVOKEVIRTUAL", 182);
            map.put("INVOKESPECIAL", 183);
            map.put("INVOKESTATIC", 184);
            map.put("INVOKEINTERFACE", 185);
            map.put("INVOKEDYNAMIC", 186);
            map.put("NEW", 187);
            map.put("NEWARRAY", 188);
            map.put("ANEWARRAY", 189);
            map.put("ARRAYLENGTH", 190);
            map.put("ATHROW", 191);
            map.put("CHECKCAST", 192);
            map.put("INSTANCEOF", 193);
            map.put("MONITORENTER", 194);
            map.put("MONITOREXIT", 195);
            map.put("MULTIANEWARRAY", 197);
            map.put("IFNULL", 198);
            map.put("IFNONNULL", 199);
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                opCodeMap.put(entry.getValue(), entry.getKey());
            }
        }
        return opCodeMap;
    }
}