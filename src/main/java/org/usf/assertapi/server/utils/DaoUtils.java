package org.usf.assertapi.server.utils;

import static java.sql.Timestamp.from;

import java.sql.Timestamp;
import java.time.Instant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DaoUtils {

    public static String inArgs(int n) {
    	if(n < 1) {
    		throw new IllegalArgumentException("n connot be < 1");
    	}
        return "(" + (n == 1 ? "?" : "?" + ",?".repeat(n-1)) + ")";
    }

    public static Timestamp ofEpochMilli(long epochMilli) {
        return from(Instant.ofEpochMilli(epochMilli));
    }
}
