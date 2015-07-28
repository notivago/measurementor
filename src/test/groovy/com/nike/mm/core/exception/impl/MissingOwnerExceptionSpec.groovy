package com.nike.mm.core.exception.impl

import spock.lang.Specification

class MissingOwnerExceptionSpec extends Specification {

	private static final String MESSAGE = "Message"
	
	def "Exception is thrown with message set"() {
		when:
			throw new MissingOwnerException(MESSAGE);
		then:
			MissingOwnerException exception = thrown();
			exception.message == MESSAGE;
	}
}
