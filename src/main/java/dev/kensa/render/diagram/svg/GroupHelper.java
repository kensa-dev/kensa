package dev.kensa.render.diagram.svg;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

public class GroupHelper {
    private static final Pattern PATTERN = Pattern.compile(">\\((.*)\\).*");
    private final Stack<String> groups = new Stack<>();

    public List<String> markupGroup(String currentLine) {
        List<String> lines = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(currentLine);

        if (matcher.find()) {
            String groupName = matcher.group(1);
            if (groups.isEmpty()) {
                lines.addAll(startGroup(groupName));
            } else if (!groups.peek().equals(groupName)) {
                lines.addAll(isMovingToPreviousGroup(groupName) ? endGroup() : startGroup(groupName));
            }

            lines.add(currentLine.replaceFirst(">\\(+" + groupName + "\\)\\s?", ">"));
        } else {
            lines.addAll(cleanUpOpenGroups());
            lines.add(currentLine);
        }
        return lines;
    }

    public List<String> cleanUpOpenGroups() {
        List<String> endGroups = new ArrayList<>();

        while (!groups.empty()) {
            endGroups.addAll(endGroup());
        }
        return endGroups;
    }

    private boolean isMovingToPreviousGroup(String groupName) {
        int len = groups.size();
        return len >= 2 && groups.elementAt(len - 2).equals(groupName);
    }

    private List<String> startGroup(String groupName) {
        groups.push(groupName);
        return singletonList(format("group %s", groupName));
    }

    private List<String> endGroup() {
        groups.pop();
        return singletonList("end");
    }
}
