package cn.czh.service.impl;

import cn.czh.entity.UserConfig;
import cn.czh.mapper.UserConfigMapper;
import cn.czh.service.IAuthService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AuthServiceImpl implements IAuthService {

    @Resource
    private UserConfigMapper userConfigMapper;

    @Override
    public String getMainUserPassword() {
        UserConfig user = userConfigMapper.selectOne(
                new LambdaQueryWrapper<UserConfig>()
                        .eq(UserConfig::getIsMainUser, 1)
        );
        if (user == null) {
            return "";
        }
        return user.getPassword() == null || user.getPassword().trim().isEmpty() ? "" : user.getPassword();
    }

    @Override
    public void setMainUserPassword(String password) {
        UserConfig user = userConfigMapper.selectOne(
                new LambdaQueryWrapper<UserConfig>()
                        .eq(UserConfig::getIsMainUser, 1)
        );

        if (user == null) {
            user = new UserConfig();
            user.setUsername("admin");
            user.setIsMainUser(1);
            user.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        }
        user.setPassword(password != null && password.trim().isEmpty() ? null : password);
        if (user.getId() == null) {
            userConfigMapper.insert(user);
        } else {
            userConfigMapper.updateById(user);
        }
    }

    @Override
    public boolean isMainUser(String remoteAddress) {
        return "127.0.0.1".equals(remoteAddress) || "0:0:0:0:0:0:0:1".equals(remoteAddress);
    }
}
