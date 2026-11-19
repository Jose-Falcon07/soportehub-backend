package com.soportehub.backend.common.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class Dates {
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Panama"));

    private Dates() {}

    public static String format(Instant instant) {
        return instant == null ? "" : FORMAT.format(instant);
    }
}
