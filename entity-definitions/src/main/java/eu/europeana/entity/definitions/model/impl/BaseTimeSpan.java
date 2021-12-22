package eu.europeana.entity.definitions.model.impl;

import eu.europeana.entity.definitions.model.TimeSpan;

public class BaseTimeSpan extends BaseEntity implements TimeSpan {

    private String[] isNextInSequence;
    private String begin;
    private String end;

    public String[] getIsNextInSequence() {
        return isNextInSequence;
    }

    public void setIsNextInSequence(String[] isNextInSequence) {
        this.isNextInSequence = isNextInSequence;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    @Override
    public String getBegin() {
        return begin;
    }

    @Override
    public String getEnd() {
        return end;
    }

}
