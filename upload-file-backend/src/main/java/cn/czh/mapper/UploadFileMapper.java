package cn.czh.mapper;

import cn.czh.entity.UploadFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UploadFileMapper extends BaseMapper<UploadFile> {


    IPage<UploadFile> pageFiles(@Param("page") Page<UploadFile> page,
                                @Param("storageType") String storageType,
                                @Param("fileName") String fileName);

}