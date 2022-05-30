package nbo.tree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class NBOString implements NBOTree {
    private String value;

    @Override
    public List<NBOTree> getSubTrees() {
        return new ArrayList<>();
    }
}
