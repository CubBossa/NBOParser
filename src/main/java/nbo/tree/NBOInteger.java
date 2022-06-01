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
public class NBOInteger implements NBOTree {
    private int value;

    public NBOInteger(String input) {
        if(input.startsWith("'") || input.startsWith("\"")) {
            input = input.substring(1, input.length() - 1);
        }
        input = input.toLowerCase(Locale.ROOT).replace("f", "");
        value = Integer.parseInt(input);
    }

    @Override public String toString() {
        return getValue().toString();
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Object getValueRaw() {
        return value;
    }

    @Override
    public List<NBOTree> getSubTrees() {
        return new ArrayList<>();
    }
}
