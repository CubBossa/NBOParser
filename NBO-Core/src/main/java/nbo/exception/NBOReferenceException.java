package nbo.exception;

import nbo.tree.NBOReference;

public class NBOReferenceException extends NBOException {

	public NBOReferenceException(NBOReference reference) {
		super("The variable " + reference + " is not defined in the given scope. Check that all required imports are made and files are included.");
	}

}
