package cn.czh.dto.aws;

import cn.czh.dto.MyPartSummary;

import java.util.Date;

public class OssPartSummaryWrapper implements MyPartSummary {
    private final com.aliyun.oss.model.PartSummary ossPartSummary;

    public OssPartSummaryWrapper(com.aliyun.oss.model.PartSummary ossPartSummary) {
        this.ossPartSummary = ossPartSummary;
    }

    @Override
    public int getPartNumber() {
        return ossPartSummary.getPartNumber();
    }

    @Override
    public Date getLastModified() {
        return ossPartSummary.getLastModified();
    }

    @Override
    public String getETag() {
        return ossPartSummary.getETag();
    }

    @Override
    public long getSize() {
        return ossPartSummary.getSize();
    }
}