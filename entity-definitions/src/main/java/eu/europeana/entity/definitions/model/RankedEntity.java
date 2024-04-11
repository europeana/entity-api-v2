package eu.europeana.entity.definitions.model;

import java.util.Date;

public interface RankedEntity {

    // TODO: move timestamp to another interface
    void setTimestamp(Date timestamp);

    Date getTimestamp();

    void setWikipediaClicks(int wikipediaClicks);

    int getWikipediaClicks();

}
