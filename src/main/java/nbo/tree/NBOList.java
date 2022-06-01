package nbo.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NBOList extends ArrayList<NBOTree> implements NBOTree {

    @Override
    public Object getValue() {
        return "List";
    }

    @Override
    public List<NBOTree> getSubTrees() {
        return stream().flatMap(tree -> tree.getSubTrees().stream()).collect(Collectors.toList());
    }
}
