package nbo.tree;

import java.util.List;

public interface NBOTree {

    List<NBOTree> getSubTrees();

    Object getValue();
}
