package cn.czh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("shared_file")
public class SharedFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileIdentifier;
    private String createTime;
}