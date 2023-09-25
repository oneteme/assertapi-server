package org.usf.assertapi.server.utils;

import lombok.Getter;

@Getter
public final class StringBuilder {
    private final java.lang.StringBuilder sb;

    public StringBuilder() {
        sb = new java.lang.StringBuilder();
    }

    public StringBuilder(String value) {
        sb = new java.lang.StringBuilder().append(value);
    }

    public StringBuilder appendIf(String value, boolean test) {

        return test ? append(value) : this;
    }

    public StringBuilder append(String value) {
        sb.append(value);
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
