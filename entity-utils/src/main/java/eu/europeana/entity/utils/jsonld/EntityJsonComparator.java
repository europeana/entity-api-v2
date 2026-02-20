package eu.europeana.entity.utils.jsonld;

import java.util.Comparator;
import java.util.HashMap;
import org.apache.stanbol.commons.jsonld.JsonLdCommon;

import static eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants.id;
import static eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants.type;
import static eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants.total;
import static eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants.partOf;
import static eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants.prev;
import static eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants.next;
import static eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants.items;
import static eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants.*;


/**
 * This class is specific to annotations must be moved to (europeana) annotation
 * repository A comparator for JSON-LD maps to ensure the order of certain key
 * elements like '#', '@', 'a' in JSON-LD output.
 *
 * @author Jan Roerden
 */

public class EntityJsonComparator implements Comparator<Object> {

	static final HashMap<String, Integer> propOrderMap = new HashMap<String, Integer>();
	static {
		propOrderMap.put(JsonLdCommon.CONTEXT, 10);
		//AGENTS
		propOrderMap.put(id, 20);
		propOrderMap.put(type, 30);
		propOrderMap.put(total, 40);
		propOrderMap.put(depiction, 50);
		propOrderMap.put(isShownBy, 51);
		propOrderMap.put(prefLabel, 60);
		propOrderMap.put(altLabel, 70);
		propOrderMap.put(hiddenLabel, 80);
		propOrderMap.put(name, 90);
		propOrderMap.put(begin, 100);
		propOrderMap.put(dateOfBirth, 110);
		propOrderMap.put(dateOfEstablishment, 120);
		propOrderMap.put(end, 130);
		propOrderMap.put(dateOfDeath, 140);
		propOrderMap.put(dateOfTermination, 150);
		propOrderMap.put(date, 160);
		propOrderMap.put(placeOfBirth, 170);
		propOrderMap.put(placeOfDeath, 180);
		propOrderMap.put(gender, 190);
		propOrderMap.put(professionOrOccupation, 200);
		propOrderMap.put(biographicalInformation, 210);
//		propOrderMap.put(IS_SHOWN_BY, 215); //2200
		//Place
		propOrderMap.put(lat, 220);
		propOrderMap.put(_long, 230);
		propOrderMap.put(alt, 240);
//		
		//Agent
		propOrderMap.put(note, 250);
		
		//Concept
		propOrderMap.put(notation, 260);
		propOrderMap.put(broader, 270);
		propOrderMap.put(narrower, 280);
		propOrderMap.put(related, 290);
		propOrderMap.put(broadMatch, 300);
		propOrderMap.put(narrowMatch, 310);
		propOrderMap.put(relatedMatch, 320);
		propOrderMap.put(closeMatch, 330);
		propOrderMap.put(exactMatch, 340);
		
		//Organizations
		propOrderMap.put(acronym, 65);
		propOrderMap.put(description, 360);
		propOrderMap.put(FOAF_LOGO, 370);
		propOrderMap.put(europeanaRole, 380);
//		propOrderMap.put(ORGANIZATION_DOMAIN, 390);
//		propOrderMap.put(GEOGRAPHIC_LEVEL, 400);
		propOrderMap.put(country, 410);
		propOrderMap.put(FOAF_HOMEPAGE, 420);
		propOrderMap.put(FOAF_PHONE, 430);
		propOrderMap.put(FOAF_MBOX, 440);
		//Organization - relations
		propOrderMap.put(hasAddress, 510);
		propOrderMap.put(aggregatedVia, 520);
		
		//Agent - relations
		propOrderMap.put(hasPart, 460);
		propOrderMap.put(isPartOf, 470);
		propOrderMap.put(hasMet, 480);
		propOrderMap.put(isRelatedTo, 490);
		propOrderMap.put(wasPresentAt, 500);
		
		//generic/other relations
		propOrderMap.put(identifier, 620);
		propOrderMap.put(isNextInSequence, 630);
		propOrderMap.put(inScheme, 640);
		propOrderMap.put(sameAs, 650);
		//address
		propOrderMap.put(streetAddress, 1010);
		propOrderMap.put(postalCode, 1020);
		propOrderMap.put(postOfficeBox, 1030);
		propOrderMap.put(locality, 1040);
		propOrderMap.put(region, 1050);
		propOrderMap.put(countryName, 1060);
				
		//Page
		propOrderMap.put(partOf, 2050);
		propOrderMap.put(prev, 2090);
		propOrderMap.put(next, 2100);
		propOrderMap.put(FACETS, 2110);
		propOrderMap.put(items, 2120);
		
		//aggregation
        propOrderMap.put(created, 3000);
        propOrderMap.put(modified, 3010);
        propOrderMap.put(pageRank, 3020);
        propOrderMap.put(recordCount, 3030);
        propOrderMap.put(score, 3040);

	}

	@Override
	public int compare(Object arg0, Object arg1) {
		Integer leftOrder = propOrderMap.get(arg0);
		Integer rightOrder = propOrderMap.get(arg1);
		if (leftOrder == null)
			leftOrder = Math.abs(arg0.hashCode());
		if (rightOrder == null)
			rightOrder = Math.abs(arg1.hashCode());

		return Integer.compare(leftOrder, rightOrder);
	}

}
