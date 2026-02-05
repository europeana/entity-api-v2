package eu.europeana.entity.config;

/**
 * I18 constants for the messages related to Entity API v2
 */
public class I18nConstants {

	private I18nConstants() {
		// to hide implicit one
	}

	// 4xx
	public static final String UNSUPPORTED_ENTITY_TYPE    = "error.entity_unsupported_type";
	public static final String UNSUPPORTED_ALGORITHM_TYPE = "error.algorithm_unsupported_type";

	//500
	public static final String SERVER_ERROR_CANT_RETRIEVE_URI     = "error.entity_server_cannot_retrieve_uri";
	public static final String SERVER_ERROR_CANT_SERIALIZE_OBJECT = "error.entity_server_cannot_serialize_object";
}
