package nbo.tree;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class NBOList extends ArrayList<NBOTree> implements NBOTree, NBOTyped {

    /**
     * If null, ArrayList is used.
     */
    private String type = null;

    @Override
    public Object getValue() {
        return "List";
    }

    @Override
    public Object getValueRaw() {
        return "List";
    }

    @Override
    public List<NBOTree> getSubTrees() {
        return stream().flatMap(tree -> tree.getSubTrees().stream()).collect(Collectors.toList());
    }
}
