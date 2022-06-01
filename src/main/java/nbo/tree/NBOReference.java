package nbo.tree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class NBOReference implements NBOTree {
    private String reference;

    @Override public String toString() {
        return "&" + getValue();
    }

    @Override
    public Object getValue() {
        return reference;
    }

    @Override
    public List<NBOTree> getSubTrees() {
        return new ArrayList<>();
    }
}
