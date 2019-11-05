package com.excelian.demo.simulator.Util;

public class RandomUtil {

    private static String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "0123456789"
            + "abcdefghijklmnopqrstuvxyz";

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; ++i) {
            int index = (int) (alphaNumericString.length() * Math.random());
            char c = alphaNumericString.charAt(index);
            sb.append(c);
        }
        return sb.toString();
    }

    public static int generateRandomInteger(int low, int high) {
        return (int) (((high - low) + 1) * Math.random()) + low;
    }

    public static double generateRandomDouble(double low, double high) {
        return ((high - low) + 1.0) * Math.random() + low;
    }
}
