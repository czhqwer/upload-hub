package cn.czh.dto.aws;

import cn.czh.dto.MyPartSummary;

import java.util.Date;

public class LocalPartSummary implements MyPartSummary {
    private final int partNumber;
    private final String eTag;
    private final long size;
    private final Date lastModified;

    public LocalPartSummary(int partNumber, String eTag, long size, Date lastModified) {
        this.partNumber = partNumber;
        this.eTag = eTag;
        this.size = size;
        this.lastModified = lastModified;
    }

    @Override
    public int getPartNumber() {
        return partNumber;
    }

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public String getETag() {
        return eTag;
    }

    @Override
    public long getSize() {
        return size;
    }
}