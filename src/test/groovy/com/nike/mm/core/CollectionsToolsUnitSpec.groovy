package com.nike.mm.core

import spock.lang.Specification

class CollectionsToolsUnitSpec extends Specification {

	private static final String MESSAGE = "Message"
	
	def "Null value result in empty collection"() {
		expect:
			CollectionsTools.enforceAsCollection(null) == [];
	}
	
	def "A collection is returned as is"() {
		expect:
			CollectionsTools.enforceAsCollection([1, 2]) == [1, 2];
	}
	
	def "A single value is returned wrapped"() {
		expect:
			CollectionsTools.enforceAsCollection(1) == [1];
	}
}
