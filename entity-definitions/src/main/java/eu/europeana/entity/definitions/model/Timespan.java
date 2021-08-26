package eu.europeana.entity.definitions.model;

public interface Timespan extends Entity {

    void setIsNextInSequence(String[] isNextInSequence);

    String[] getIsNextInSequence();

    public String getBegin();

    public void setBegin(String begin);

    public String getEnd();

    public void setEnd(String end);

    public String[] getIsPartOf();

    public void setIsPartOf(String[] isPartOf);

}
