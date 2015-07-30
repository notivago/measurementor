package com.nike.mm.business.plugins.impl

import java.util.Set;

import spock.lang.Specification;

import com.nike.mm.business.plugins.IMeasureMentorBusiness;
import com.nike.mm.business.plugins.IPluginStore;
import com.nike.mm.core.exception.impl.PluginNotFoundException;

class PluginStoreUnitSpec extends Specification {

	private static final String EXISTING_PLUGIN = "existingPlugin"

	private static final String NON_EXISTING_PLUGIN = "nonExistingPlugin"

	IMeasureMentorBusiness expectedPlugin = Mock();
	
	PluginStore store = new PluginStore();
	
	def setup() {
		store.measureMentorBusinesses = [expectedPlugin];
		expectedPlugin.type() >> EXISTING_PLUGIN;
	}
	
	def "find a plugin by its given type"() {
		when:
			def plugin = store.findByType(EXISTING_PLUGIN);
		then:
			plugin == expectedPlugin; 
	}
	
	def "find a plugin throws exception for unknown type"() {
		when:
			store.findByType(NON_EXISTING_PLUGIN);
		then:
			PluginNotFoundException exception = thrown();
	}
}
