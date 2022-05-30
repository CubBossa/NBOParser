package nbo.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class NBOMap extends HashMap<String, NBOTree> implements NBOTree {

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
        if(size() == 0) {
            return "{}";
        } else if(size() == 1) {
            return "{" + entrySet().stream().findAny().map(Entry::getValue).orElse(new NBOMap()).pretty(indent).replace("\n", "\n" + indent) + "}";
        }
        return "{\n" + indent + entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue().pretty(indent).replace("\n", "\n" + indent))
                .collect(Collectors.joining(",\n" + indent)) + "\n}";
    }
}
