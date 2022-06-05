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
    private String elementType = null;

    @Override
    public String toString() {
        return (type == null ? "" : type) + "[" + (elementType == null ? "" : elementType + ";")
                + String.join(",", stream().map(NBOTree::toString).toArray(CharSequence[]::new)) + "]";
    }

    @Override
    public String toNBTString() {
        return (type == null ? "" : type) + "[" + (elementType == null ? "" : elementType + ";")
                + String.join(",", stream().map(NBOTree::toString).toArray(CharSequence[]::new)) + "]";
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
