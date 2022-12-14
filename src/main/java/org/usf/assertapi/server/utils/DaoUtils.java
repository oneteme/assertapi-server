package org.usf.assertapi.server.utils;

import java.sql.Timestamp;
import java.time.Instant;

public class DaoUtils {

    public static String inArgs(int n) {
        return "(" + (n == 1 ? "?" : "?" + ",?".repeat(n-1)) + ")";
    }

    public static Timestamp ofEpochMilli(long v) {
        return Timestamp.from(Instant.ofEpochMilli(v));
    }
}
