package nbo;

import nbo.tree.NBOTree;

import java.util.HashMap;
import java.util.Map;

public class NBOInterpreter {

	public Map<String, Object> interpret(NBOTree tree) throws NBOParseException {
		checkReferences(tree);
		return new HashMap<>();
	}

	public void checkReferences(NBOTree tree) throws NBOParseException {

	}
}
