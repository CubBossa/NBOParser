package nbo.tree;

import java.util.*;
import java.util.stream.Collectors;

public class NBOMap extends LinkedHashMap<String, NBOTree> implements NBOTree {

    @Override public String toString() {
        return super.toString();
    }

    @Override
    public Object getValue() {
        return "Map";
    }

    @Override
    public Object getValueRaw() {
        return "Map";
    }

    @Override
    public List<NBOTree> getSubTrees() {
        return new ArrayList<>(values());
    }
}
