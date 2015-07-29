package com.nike.mm.core

class CollectionsTools {
	public static Collection enforceAsCollection( value ) {
		if(!value) {
			return [];
		}
		return isCollectionOrArray( value ) ? value : [value];
	}
	
	private static boolean isCollectionOrArray(final Object value) {
		[Collection, Object[]].any { it.isAssignableFrom(value.getClass()) }
	}
}
