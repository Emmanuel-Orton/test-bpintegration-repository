package com.bearingpoint.beyond.test-bpintegration.configuration;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.contrib.json.classic.JsonLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MaskSensitiveDataPatternLayout extends JsonLayout {

    private Pattern compiledPattern;
    private List<String> maskPatterns = new ArrayList<>();

    public void addMaskPattern(String maskPattern) {
        maskPatterns.add(maskPattern);
        compiledPattern = Pattern.compile(maskPatterns.stream().collect(Collectors.joining("|")), Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        String message = super.doLayout(event);

        return maskMessage(message);
    }

    protected String maskMessage(String message) {
        if (compiledPattern == null) {
            return message;
        }
        StringBuilder sb = new StringBuilder(message);
        Matcher matcher = compiledPattern.matcher(sb);

        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (matcher.group(i) != null) {
                    IntStream.range(matcher.start(i), matcher.end(i)).forEach(group -> sb.setCharAt(group, '*'));
                }
            }
        }
        return sb.toString();
    }

}