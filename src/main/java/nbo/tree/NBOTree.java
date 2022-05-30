package nbo.tree;

import java.util.List;

public interface NBOTree {

    Object getValue();
    List<NBOTree> getSubTrees();
    default String pretty(String indent) {
        return getValue().toString();
    }
}
