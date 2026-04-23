package com.yt.evaluation_system.user.controller;

import com.yt.evaluation_system.common.entity.User;
import com.yt.evaluation_system.common.result.Result;
import com.yt.evaluation_system.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody java.util.Map<String, String> params) {
        return userService.login(params.get("username"), params.get("password"));
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody java.util.Map<String, String> params) {
        return userService.register(params.get("username"), params.get("password"), params.get("nickname"));
    }

    @GetMapping("/info/{id}")
    public Result<User> info(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null); // 隐藏密码
        }
        return Result.ok(user);
    }

    @GetMapping("/info")
    public Result<User> getMyInfo(javax.servlet.http.HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return userService.info(token);
    }

    @PostMapping("/become-merchant")
    public Result<String> becomeMerchant(javax.servlet.http.HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return userService.becomeMerchant(token);
    }
}

