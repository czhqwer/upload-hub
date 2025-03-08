package cn.czh.dto;

import cn.czh.entity.UploadFile;
import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
@Accessors(chain = true)
public class TaskRecordDTO extends UploadFile {

    /**
     * 已上传完的分片
     */
    private List<MyPartSummary> exitPartList;

    public static TaskRecordDTO convertFromEntity (UploadFile task) {
        TaskRecordDTO dto = new TaskRecordDTO();
        BeanUtil.copyProperties(task, dto);
        return dto;
    }
}
