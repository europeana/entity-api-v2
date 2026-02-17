package eu.europeana.entity.solr.model.vocabulary;

import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityFields;

public interface SuggestionFields {

	public static final String FILTER_EUROPEANA = "europeana";
	public static final String TERM             = "term";
	public static final String PAYLOAD          = "payload";

	//TODO: update to correct values from specs
	public static final String TIME_SPAN_START = "begin";
	public static final String TIME_SPAN_END = "end";
	public static final String IN_SCHEME = "inScheme";
	
	public static final String ID = CommonLdConstants.id; // "id";
	
	public static final String DATE_OF_BIRTH = WebEntityFields.dateOfBirth; // "lifespanStart";
	public static final String DATE_OF_DEATH = WebEntityFields.dateOfDeath; // "lifespanEnd";
	public static final String LATITUDE = WebEntityFields.lat; // "latitude";
	public static final String LONGITUDE = WebEntityFields._long; // "longitude";
	public static final String PREF_LABEL = WebEntityFields.prefLabel; // "prefLabel";
	public static final String ALT_LABEL = WebEntityFields.altLabel; // "altLabel";
	public static final String HIDDEN_LABEL = WebEntityFields.hiddenLabel; // "hiddenLabel";
	public static final String TYPE = CommonLdConstants.type; // "type";
	public static final String PROFESSION_OR_OCCUPATION = WebEntityFields.professionOrOccupation;
	public static final String IS_SHOWN_BY = WebEntityFields.isShownBy; // "isShownBy";

	public static final String IS_PART_OF = WebEntityFields.isPartOf;
}
