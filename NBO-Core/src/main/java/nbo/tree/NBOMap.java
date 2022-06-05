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
        return (type == null ? "" : type) + "{" + String.join(",", entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue().toString())
                .toArray(CharSequence[]::new)) + "}";
    }

    @Override
    public String toNBTString() {
        return (type == null ? "" : type) + "{" + String.join(",", entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue().toNBTString())
                .toArray(CharSequence[]::new)) + "}";
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
