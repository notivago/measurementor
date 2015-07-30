package com.nike.mm.core.exception.impl

import spock.lang.Specification

class PluginNotFoundExceptionUnitSpec extends Specification {

	private static final String PLUGIN_NOT_FOUND = "Plugin not found"

	def "exception is created with the message" () {
		when:
			throw new PluginNotFoundException(PLUGIN_NOT_FOUND);
		then:
			PluginNotFoundException exception = thrown();
			exception.message == PLUGIN_NOT_FOUND;
	}
	
}
