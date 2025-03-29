package cn.czh.service.impl;

import cn.czh.dto.NotifyMessage;
import cn.czh.entity.UserConfig;
import cn.czh.mapper.UserConfigMapper;
import cn.czh.service.IAuthService;
import cn.czh.service.ISseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements IAuthService {

    @Resource
    private UserConfigMapper userConfigMapper;
    @Resource
    private ISseService sseService;

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
        user.setPassword(password != null && password.trim().isEmpty() ? "" : password);
        if (user.getId() == null) {
            userConfigMapper.insert(user);
        } else {
            userConfigMapper.updateById(user);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("password", password);
        NotifyMessage message = new NotifyMessage("setPassword", data);

        sseService.notifySystemEvent(message);
    }

    @Override
    public boolean isMainUser(String remoteAddress) {
        return "127.0.0.1".equals(remoteAddress) || "0:0:0:0:0:0:0:1".equals(remoteAddress);
    }
}
