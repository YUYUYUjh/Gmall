package com.yy;

import org.bouncycastle.util.test.Test;

/**
 * @author Yu
 * @create 2021-10-17 12:35
 */
public class TestDemo {

    public static void main(String[] args) {
        String s = "/**/inner/";
        boolean b = s.startsWith("/");
        System.out.println(b);
    }
}
