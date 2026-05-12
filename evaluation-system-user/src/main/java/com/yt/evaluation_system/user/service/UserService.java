package com.yt.evaluation_system.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yt.evaluation_system.common.entity.User;
import com.yt.evaluation_system.common.result.Result;

public interface UserService extends IService<User> {
    Result<String> login(String username, String password);
    Result<String> register(String username, String password, String nickname);
    Result<User> info(String token);
    Result<String> becomeMerchant(String token);
    Result<String> submitVerification(String imageUrl, String token);
    Result<java.util.List<User>> getPendingVerifications(String token);
    Result<String> processVerification(Long userId, Integer status, String token);

    // Phase 3 additions
    Result<java.util.List<User>> getPendingMerchants(String token);
    Result<String> processMerchant(Long userId, Integer status, String token);
    Result<com.baomidou.mybatisplus.core.metadata.IPage<User>> getUserList(Integer current, Integer size, String token);
    Result<String> updateUserStatus(Long userId, Integer status, String token);
    Result<java.util.Map<String, Object>> getAdminStats(String token);
}

