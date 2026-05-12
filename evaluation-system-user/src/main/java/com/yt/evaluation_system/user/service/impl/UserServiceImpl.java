package com.yt.evaluation_system.user.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yt.evaluation_system.common.entity.User;
import com.yt.evaluation_system.common.result.Result;
import com.yt.evaluation_system.common.utils.JwtUtils;
import com.yt.evaluation_system.user.mapper.UserMapper;
import com.yt.evaluation_system.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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

        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.fail("您的账号已被封禁");
        }
        
        // 简单 MD5 验证
        if (!DigestUtil.md5Hex(password).equals(user.getPassword())) {
            return Result.fail("用户名或密码错误");
        }
        
        // 生成 JWT Token（自包含 userId 和 role，无需存入 Redis）
        String token = JwtUtils.generateToken(user.getId(), user.getRole());
        
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
        Long userId = JwtUtils.getUserId(token);
        if (userId == null) {
            return Result.fail("登录已过期");
        }
        User user = this.getById(userId);
        if (user != null) {
            user.setPassword(null); // 隐藏密码
        }
        return Result.ok(user);
    }

    @Override
    public Result<String> becomeMerchant(String token) {
        User user = getUserByToken(token);
        if (user == null) {
            return Result.fail("未登录或登录已过期");
        }
        if (user.getRole() != null && user.getRole() == 1) {
            return Result.fail("您已经是商家了");
        }
        if (user.getMerchantStatus() != null && user.getMerchantStatus() == 1) {
            return Result.fail("您的申请正在审核中，请耐心等待");
        }
        user.setMerchantStatus(1);
        this.updateById(user);
        return Result.ok("商家申请已提交，请等待管理员审核");
    }

    private User getUserByToken(String token) {
        if (cn.hutool.core.util.StrUtil.isBlank(token)) return null;
        Long userId = JwtUtils.getUserId(token);
        if (userId == null) return null;
        return this.getById(userId);
    }

    @Override
    public Result<String> submitVerification(String imageUrl, String token) {
        User user = getUserByToken(token);
        if (user == null) return Result.fail("未登录或登录过期");
        if (user.getVerifyStatus() != null && user.getVerifyStatus() == 2) {
            return Result.fail("您已是认证学生，无需重复提交");
        }
        user.setVerifyStatus(1);
        user.setVerifyImage(imageUrl);
        this.updateById(user);
        return Result.ok("认证资料已提交，请等待管理员审核");
    }

    @Override
    public Result<java.util.List<User>> getPendingVerifications(String token) {
        User admin = getUserByToken(token);
        if (admin == null || admin.getRole() == null || admin.getRole() != 2) {
            return Result.fail("无权限访问");
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getVerifyStatus, 1);
        java.util.List<User> list = this.list(wrapper);
        list.forEach(u -> u.setPassword(null));
        return Result.ok(list);
    }

    @Override
    public Result<String> processVerification(Long userId, Integer status, String token) {
        User admin = getUserByToken(token);
        if (admin == null || admin.getRole() == null || admin.getRole() != 2) {
            return Result.fail("无权限访问");
        }
        if (status != 2 && status != 3) {
            return Result.fail("状态不合法");
        }
        User target = this.getById(userId);
        if (target == null) return Result.fail("用户不存在");
        
        target.setVerifyStatus(status);
        this.updateById(target);
        return Result.ok("审核完成");
    }

    @Override
    public Result<java.util.List<User>> getPendingMerchants(String token) {
        User admin = getUserByToken(token);
        if (admin == null || admin.getRole() == null || admin.getRole() != 2) return Result.fail("无权限");
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getMerchantStatus, 1);
        java.util.List<User> list = this.list(wrapper);
        list.forEach(u -> u.setPassword(null));
        return Result.ok(list);
    }

    @Override
    public Result<String> processMerchant(Long userId, Integer status, String token) {
        User admin = getUserByToken(token);
        if (admin == null || admin.getRole() == null || admin.getRole() != 2) return Result.fail("无权限");
        User target = this.getById(userId);
        if (target == null) return Result.fail("用户不存在");
        
        target.setMerchantStatus(status);
        if (status == 2) {
            target.setRole(1); // 审核通过成为商家
        }
        this.updateById(target);
        return Result.ok("审批完成");
    }

    @Override
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<User>> getUserList(Integer current, Integer size, String token) {
        User admin = getUserByToken(token);
        if (admin == null || admin.getRole() == null || admin.getRole() != 2) return Result.fail("无权限");
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        this.page(page);
        page.getRecords().forEach(u -> u.setPassword(null));
        return Result.ok(page);
    }

    @Override
    public Result<String> updateUserStatus(Long userId, Integer status, String token) {
        User admin = getUserByToken(token);
        if (admin == null || admin.getRole() == null || admin.getRole() != 2) return Result.fail("无权限");
        User target = this.getById(userId);
        if (target == null) return Result.fail("用户不存在");
        if (target.getRole() != null && target.getRole() == 2) return Result.fail("无法操作管理员账号");

        target.setStatus(status);
        this.updateById(target);
        return Result.ok("操作成功");
    }

    @Override
    public Result<java.util.Map<String, Object>> getAdminStats(String token) {
        User admin = getUserByToken(token);
        if (admin == null || admin.getRole() == null || admin.getRole() != 2) return Result.fail("无权限");
        
        long totalUsers = this.count();
        long pendingVerifications = this.count(new LambdaQueryWrapper<User>().eq(User::getVerifyStatus, 1));
        long pendingMerchants = this.count(new LambdaQueryWrapper<User>().eq(User::getMerchantStatus, 1));

        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("totalUsers", totalUsers);
        map.put("pendingVerifications", pendingVerifications);
        map.put("pendingMerchants", pendingMerchants);
        return Result.ok(map);
    }
}

