package dev.kensa.util;

import java.time.Duration;

public final class DurationFormatter {

    public static String format(final Duration duration) {
        StringBuilder sb = new StringBuilder();

        Duration d = duration;
        long days = toDaysPart(d);
        d = d.minusDays(days);
        int hours = toHoursPart(d);
        d = d.minusHours(hours);
        int minutes = toMinutesPart(d);
        d = d.minusMinutes(minutes);
        int seconds = toSecondsPart(d);
        d = d.minusSeconds(seconds);
        int milliseconds = toMillisPart(d);

        append(sb, days, "Day");
        append(sb, hours, "Hr");
        append(sb, minutes, "Min");
        append(sb, seconds, "Sec");
        append(sb, milliseconds, "Ms", "");

        return sb.toString();
    }

    private static int toMillisPart(Duration duration) {
        return (int) (duration.toNanos() / 1000_000);
    }

    private static int toSecondsPart(Duration duration) {
        return (int) (duration.getSeconds() % 60);
    }

    private static int toMinutesPart(Duration duration) {
        return (int) (duration.toMinutes() % 60);
    }

    private static int toHoursPart(Duration duration) {
        return (int) (duration.toHours() % 24);
    }

    private static long toDaysPart(Duration duration) {
        return duration.getSeconds() / 86400;
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
