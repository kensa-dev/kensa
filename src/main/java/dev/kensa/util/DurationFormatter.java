package dev.kensa.util;

import java.time.Duration;

public final class DurationFormatter {

    public static String format(Duration duration) {
        StringBuilder sb = new StringBuilder();

        append(sb, duration.toDaysPart(), "Day");
        append(sb, duration.toHoursPart(), "Hr");
        append(sb, duration.toMinutesPart(), "Min");
        append(sb, duration.toSecondsPart(), "Sec");
        append(sb, duration.toMillisPart(), "Ms", "");

        return sb.toString();
    }

    private static void append(StringBuilder sb, long value, String singlular) {
        append(sb, value, singlular, "s");
    }

    private static void append(StringBuilder sb, long value, String singlular, String pluralSuffix) {
        if(value > 0) {
            if(sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(value).append(" ").append(singlular).append(value == 1 ? "" : pluralSuffix);
        }
    }
}
