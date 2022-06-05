package nbo;

import nbo.exception.NBOReferenceException;
import nbo.tree.NBOReference;
import nbo.tree.NBOTree;

public class NBOInterpreter {

	public void interpret(NBOTree tree, NBOSerializationContext context) throws NBOReferenceException {
		checkReferences(tree, context);
	}

	public void checkReferences(NBOTree tree, NBOSerializationContext context) throws NBOReferenceException {
		for (NBOReference reference : tree.getSubTrees().stream().filter(t -> t instanceof NBOReference).map(t -> (NBOReference) t).toList()) {
			if (context.getReferenceObjects().keySet().stream().noneMatch(string -> reference.getValueRaw().equals(string))) {
				throw new NBOReferenceException(reference);
			}
		}
	}
}
