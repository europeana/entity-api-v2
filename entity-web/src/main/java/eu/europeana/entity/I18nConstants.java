package eu.europeana.entity;

public interface I18nConstants {

	// 4xx
	static final String CANT_FIND_BY_SAME_AS_URI   = "error.entity_same_as_not_found";
	static final String UNSUPPORTED_ENTITY_TYPE    = "error.entity_unsupported_type";
	static final String UNSUPPORTED_ALGORITHM_TYPE = "error.algorithm_unsupported_type";

	//500
	static final String SERVER_ERROR_CANT_RETRIEVE_URI     = "error.entity_server_cannot_retrieve_uri";
	static final String SERVER_ERROR_CANT_SERIALIZE_OBJECT = "error.entity_server_cannot_serialize_object";
}
