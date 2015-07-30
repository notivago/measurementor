package com.nike.mm.business.plugins

import com.nike.mm.core.exception.impl.PluginNotFoundException;

interface IPluginStore {
	IMeasureMentorBusiness findByType(final String type) throws PluginNotFoundException;
}
