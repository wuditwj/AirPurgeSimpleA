package com.njwyt.airpurgesimple.util;

public class StringUtil {

    /**
     * 16进制转10进制
     *
     * @param bytes
     * @return
     */
    public static String bytes2hex03(byte[] bytes) {
        final String HEX = "0123456789abcdef";
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(HEX.charAt((b >> 4) & 0x0f));
            sb.append(HEX.charAt(b & 0x0f));
        }

        return sb.toString();
    }

    /**
     * 10进制转16进制
     *
     * @param num
     * @param totalLenght 需要的字符串总长度
     * @return
     */
    public static String toHex(int num, int totalLenght) {
        String str = Integer.toHexString(num);
        str = splicingZero(str, totalLenght);
        return str;
    }

    public static String toHex(long num, int totalLenght) {
        String str = Long.toHexString(num);
        str = splicingZero(str, totalLenght);
        return str;
    }

    /**
     * 字符串前面补零操作
     *
     * @param str         字符串本体
     * @param totalLenght 需要的字符串总长度
     * @return
     */
    public static String splicingZero(String str, int totalLenght) {
        int strLenght = str.length();
        String strReturn = str;
        for (int i = 0; i < totalLenght - strLenght; i++) {
            strReturn = "0" + strReturn;
        }
        return strReturn;
    }

    /**
     * 将16进制字符串转换为byte[]
     *
     * @param str
     * @return
     */
    public static byte[] toBytes(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }
}
