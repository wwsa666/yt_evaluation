package com.yt.evaluation_system.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yt.evaluation_system.common.entity.User;
import com.yt.evaluation_system.common.result.Result;

public interface UserService extends IService<User> {
    Result<String> login(String username, String password);
    Result<String> register(String username, String password, String nickname);
    Result<User> info(String token);
    Result<String> becomeMerchant(String token);
}

