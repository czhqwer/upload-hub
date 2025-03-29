package cn.czh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_config")
public class UserConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private Integer isMainUser; // 0 or 1
    private String createTime;
}