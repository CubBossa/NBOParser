package nbo.tree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NBOObject extends NBOMap {

    private String type;

    @Override
    public String pretty(String indent) {
        return type + super.pretty(indent);
    }
}
