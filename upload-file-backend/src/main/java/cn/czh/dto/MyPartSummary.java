package cn.czh.dto;

import java.util.Date;

public interface MyPartSummary {

    int getPartNumber();

    Date getLastModified();

    String getETag();

    long getSize();

}