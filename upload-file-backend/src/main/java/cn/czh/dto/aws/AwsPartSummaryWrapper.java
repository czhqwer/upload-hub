package cn.czh.dto.aws;

import cn.czh.dto.MyPartSummary;

import java.util.Date;

/**
 * AWS S3 - PartSummary
 */
public class AwsPartSummaryWrapper implements MyPartSummary {
    private final com.amazonaws.services.s3.model.PartSummary awsPartSummary;

    public AwsPartSummaryWrapper(com.amazonaws.services.s3.model.PartSummary awsPartSummary) {
        this.awsPartSummary = awsPartSummary;
    }

    @Override
    public int getPartNumber() {
        return awsPartSummary.getPartNumber();
    }

    @Override
    public Date getLastModified() {
        return awsPartSummary.getLastModified();
    }

    @Override
    public String getETag() {
        return awsPartSummary.getETag();
    }

    @Override
    public long getSize() {
        return awsPartSummary.getSize();
    }
}