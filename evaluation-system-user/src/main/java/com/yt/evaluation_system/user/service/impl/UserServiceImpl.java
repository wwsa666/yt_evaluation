package com.yt.evaluation_system.user.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yt.evaluation_system.common.entity.User;
import com.yt.evaluation_system.common.result.Result;
import com.yt.evaluation_system.user.mapper.UserMapper;
import com.yt.evaluation_system.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result<String> login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = this.getOne(wrapper);
        
        if (user == null) {
            return Result.fail("用户名或密码错误");
        }
        
        // 简单 MD5 验证
        if (!DigestUtil.md5Hex(password).equals(user.getPassword())) {
            return Result.fail("用户名或密码错误");
        }
        
        // 生成Token
        String token = IdUtil.simpleUUID();
        // 存入Redis，设置有效期 7 天
        stringRedisTemplate.opsForValue().set("login:token:" + token, user.getId().toString(), 7, TimeUnit.DAYS);
        
        return Result.ok(token);
    }

    @Override
    public Result<String> register(String username, String password, String nickname) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (this.count(wrapper) > 0) {
            return Result.fail("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(username);
        // MD5加密
        user.setPassword(DigestUtil.md5Hex(password));
        user.setNickname(nickname);
        user.setAvatar(""); // 默认头像为空
        
        this.save(user);
        return Result.ok("注册成功");
    }

    @Override
    public Result<User> info(String token) {
        if (cn.hutool.core.util.StrUtil.isBlank(token)) {
            return Result.fail("未登录");
        }
        String userIdStr = stringRedisTemplate.opsForValue().get("login:token:" + token);
        if (cn.hutool.core.util.StrUtil.isBlank(userIdStr)) {
            return Result.fail("登录已过期");
        }
        User user = this.getById(Long.valueOf(userIdStr));
        if (user != null) {
            user.setPassword(null); // 隐藏密码
        }
        return Result.ok(user);
    }

    @Override
    public Result<String> becomeMerchant(String token) {
        if (cn.hutool.core.util.StrUtil.isBlank(token)) {
            return Result.fail("未登录");
        }
        String userIdStr = stringRedisTemplate.opsForValue().get("login:token:" + token);
        if (cn.hutool.core.util.StrUtil.isBlank(userIdStr)) {
            return Result.fail("登录已过期");
        }
        User user = this.getById(Long.valueOf(userIdStr));
        if (user == null) {
            return Result.fail("用户不存在");
        }
        if (user.getRole() != null && user.getRole() == 1) {
            return Result.fail("您已经是商家了");
        }
        user.setRole(1);
        this.updateById(user);
        return Result.ok("恭喜，您已成功成为商家！");
    }
}

