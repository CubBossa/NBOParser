package nbo.tree;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Getter
@Setter
public class NBOMap extends LinkedHashMap<String, NBOTree> implements NBOTree, NBOTyped {

    /**
     * If null, LinkedHashMap is used.
     */
    private String type = null;

    @Override
    public String toString() {
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
