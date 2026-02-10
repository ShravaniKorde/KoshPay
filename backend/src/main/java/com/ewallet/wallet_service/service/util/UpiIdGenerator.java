package com.ewallet.wallet_service.service.util;

import java.util.Locale;

public final class UpiIdGenerator {

    private static final String PLATFORM = "kospay";

    private UpiIdGenerator() {}

    public static String generateBase(String name) {
        return name
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z]", "");
    }

    public static String build(String base, int suffix) {
        return suffix == 0
                ? base + "@" + PLATFORM
                : base + suffix + "@" + PLATFORM;
    }
}
