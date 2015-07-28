package com.nike.mm.core.exception.impl

import com.nike.mm.core.exception.AbstractMmException;

class MissingOwnerException extends AbstractMmException {
	
	MissingOwnerException(String message) {
		super(message);
	}
}
