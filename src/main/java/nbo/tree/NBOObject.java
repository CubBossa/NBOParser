package nbo.tree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
public class NBOObject extends NBOMap {

    private String type;

    @Override
    public String toString() {
        return type + "{" + super.toString() + "}";
    }

}
