package nbo.tree;

import java.util.List;

public interface NBOTree {

    String toString();

    String toNBTString();

    List<NBOTree> getSubTrees();

    Object getValueRaw();
}
