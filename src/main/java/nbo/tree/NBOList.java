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

    @Override
    public String pretty(String indent) {
        return size() > 4 ?
                "[\n" + indent + stream().map(tree -> tree.pretty(indent).replace("\n", "\n" + indent)).collect(Collectors.joining(",\n" + indent)) + "\n]" :
                "[" + stream().map(tree -> tree.pretty(indent).replace("\n", "\n" + indent)).collect(Collectors.joining(", ")) + "]";
    }
}
