package nbo.tree;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NBOString implements NBOTree {
    private String value;

    public NBOString(String value) {
        this.value = value.startsWith("'") || value.startsWith("\"") ? value.substring(1, value.length() - 1) : value;
    }

    @Override public String toString() {
        return (String) getValue();
    }

    public Object getValue() {
        return "'" + value + "'";
    }

    @Override
    public List<NBOTree> getSubTrees() {
        return new ArrayList<>();
    }
}
