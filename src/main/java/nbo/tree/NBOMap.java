package nbo.tree;

import java.util.*;
import java.util.stream.Collectors;

public class NBOMap extends LinkedHashMap<String, NBOTree> implements NBOTree {

    @Override
    public Object getValue() {
        return "Map";
    }

    @Override
    public List<NBOTree> getSubTrees() {
        return new ArrayList<>(values());
    }

    @Override
    public String pretty(String indent) {
        boolean breakLines = size() > 1;
        StringBuilder s = new StringBuilder("{");
        if (breakLines) {
            s.append("\n").append(indent);
        }
        s.append(entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue().pretty(indent).replace("\n", "\n" + indent))
                .collect(Collectors.joining(",\n" + indent)));
        if (breakLines) {
            s.append("\n");
        }
        s.append("}");
        return s.toString();
    }
}
