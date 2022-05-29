package nbo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NBOInterpreter {

	public Map<String, Object> interpret(NBOParser.NBOTree tree) throws NBOParseException {
		checkReferences(tree);
		return new HashMap<>();
	}

	public void checkReferences(NBOParser.NBOTree tree) throws NBOParseException {
		Map<String, String> withTable = new HashMap<>(); //TODO
		List<String> references = new ArrayList<>();

		for (NBOParser.NBOTree t : tree.lined()) {
			if (t instanceof NBOParser.NBODeclarationTree declaration) {
				references.add(declaration.getDeclaration());
				declaration.setType(withTable.getOrDefault(declaration.getType(), declaration.getType()));
			} else if (t instanceof NBOParser.NBOReference ref) {
				if (!references.contains(ref.getReference())) {
					throw new NBOParseException("No reference '" + ref.getReference() + "' found in this file.");
				}
			}
		}
	}
}
