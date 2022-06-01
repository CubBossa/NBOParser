package nbo.tree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
@AllArgsConstructor
public class NBOFloat implements NBOTree {
    private float value;

    public NBOFloat(String input) {
        if(input.startsWith("'") || input.startsWith("\"")) {
            input = input.substring(1, input.length() - 1);
        }
        input = input.toLowerCase(Locale.ROOT).replace("f", "");
        value = Float.parseFloat(input);
    }

    @Override public String toString() {
        return getValue().toString();
    }

    public Object getValue() {
        return value;
    }

    @Override
    public List<NBOTree> getSubTrees() {
        return new ArrayList<>();
    }
}
