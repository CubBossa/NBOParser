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

    @Override
    public String toString() {
        return "&" + reference;
    }

    @Override
    public String toNBTString() {
        return "'reference: " + reference + "'";
    }

    @Override
    public String getValueRaw() {
        return reference;
    }

    @Override
    public List<NBOTree> getSubTrees() {
        return new ArrayList<>();
    }
}
