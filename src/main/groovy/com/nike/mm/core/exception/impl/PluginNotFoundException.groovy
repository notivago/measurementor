package com.nike.mm.core.exception.impl

import com.nike.mm.core.exception.AbstractMmException

class PluginNotFoundException extends AbstractMmException {
	
	PluginNotFoundException(String message) {
		super(message);
	}
}
