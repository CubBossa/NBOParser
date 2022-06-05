package nbo.tree;

import java.util.ArrayList;
import java.util.List;

public class NBONull implements NBOTree {

	@Override
	public String toString() {
		return "null";
	}

	@Override
	public String toNBTString() {
		return toString();
	}

	@Override
	public Object getValueRaw() {
		return null;
	}

	@Override
	public List<NBOTree> getSubTrees() {
		return new ArrayList<>();
	}
}