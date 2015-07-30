package com.nike.mm.business.plugins.impl

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nike.mm.business.plugins.IMeasureMentorBusiness;
import com.nike.mm.business.plugins.IPluginStore;
import com.nike.mm.core.exception.impl.PluginNotFoundException;

@Service
class PluginStore implements IPluginStore {

	@Autowired
	Set<IMeasureMentorBusiness> measureMentorBusinesses;
	
	@Override
	public IMeasureMentorBusiness findByType(String type)
			throws PluginNotFoundException {
		def plugin = measureMentorBusinesses.find({business -> business.type() == type});
		if(!plugin) {
			throw new PluginNotFoundException("Plugin of type $type was not found." )
		}

		return plugin;
	}
}
