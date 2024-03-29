package eu.europeana.entity.solr.model.vocabulary;

import eu.europeana.entity.definitions.model.vocabulary.WebEntityFields;

public interface SuggestionFields {

	public static final String FILTER_EUROPEANA = "europeana";
	
	public static final String TERM = "term";
	public static final String PAYLOAD = "payload";

	//test 2
	
	//TODO: update to correct values from specs
	public static final String TIME_SPAN_START = "begin";
	public static final String TIME_SPAN_END = "end";
	public static final String IN_SCHEME = "inScheme";
	
	public static final String ID = WebEntityFields.ID; // "id";
	
	public static final String DATE_OF_BIRTH = WebEntityFields.DATE_OF_BIRTH; // "lifespanStart";
	public static final String DATE_OF_DEATH = WebEntityFields.DATE_OF_DEATH; // "lifespanEnd";
	public static final String LATITUDE = WebEntityFields.LATITUDE; // "latitude";
	public static final String LONGITUDE = WebEntityFields.LONGITUDE; // "longitude";
	public static final String PREF_LABEL = WebEntityFields.PREF_LABEL; // "prefLabel";
	public static final String ALT_LABEL = WebEntityFields.ALT_LABEL; // "altLabel";
	public static final String HIDDEN_LABEL = WebEntityFields.HIDDEN_LABEL; // "hiddenLabel";
	public static final String TYPE = WebEntityFields.TYPE; // "type";
	public static final String PROFESSION_OR_OCCUPATION = WebEntityFields.PROFESSION_OR_OCCUPATION;
	public static final String IS_SHOWN_BY = WebEntityFields.IS_SHOWN_BY; // "isShownBy";

	public static final String IS_PART_OF = WebEntityFields.IS_PART_OF;
}
