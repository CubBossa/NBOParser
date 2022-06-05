package nbo.tree;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NBOString implements NBOTree {

    private String value;
    private char quote;

    public NBOString(String value) {
        this.value = value.startsWith("'") || value.startsWith("\"") ? value.substring(1, value.length() - 1) : value;
        this.quote = '\'';
    }

    public NBOString(String value, char quote) {
        this.value = value.startsWith("'") || value.startsWith("\"") ? value.substring(1, value.length() - 1) : value;
        this.quote = quote;
    }

    @Override
    public String toString() {
        return (String) getValue();
    }

    @Override
    public Object getValue() {
        return quote + value + quote;
    }

    public String getValueRaw() {
        return value;
    }

    @Override
    public List<NBOTree> getSubTrees() {
        return new ArrayList<>();
    }
}
