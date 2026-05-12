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

    @PostMapping("/verify/submit")
    public Result<String> submitVerification(@RequestParam("imageUrl") String imageUrl, javax.servlet.http.HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return userService.submitVerification(imageUrl, token);
    }

    @GetMapping("/admin/verify/list")
    public Result<java.util.List<User>> getPendingVerifications(javax.servlet.http.HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return userService.getPendingVerifications(token);
    }

    @PostMapping("/admin/verify/process")
    public Result<String> processVerification(
            @RequestParam("userId") Long userId,
            @RequestParam("status") Integer status,
            javax.servlet.http.HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return userService.processVerification(userId, status, token);
    }

    // Phase 3 additions
    @GetMapping("/admin/merchant/list")
    public Result<java.util.List<User>> getPendingMerchants(javax.servlet.http.HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return userService.getPendingMerchants(token);
    }

    @PostMapping("/admin/merchant/process")
    public Result<String> processMerchant(
            @RequestParam("userId") Long userId,
            @RequestParam("status") Integer status,
            javax.servlet.http.HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return userService.processMerchant(userId, status, token);
    }

    @GetMapping("/admin/list")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            javax.servlet.http.HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return userService.getUserList(current, size, token);
    }

    @PostMapping("/admin/status")
    public Result<String> updateUserStatus(
            @RequestParam("userId") Long userId,
            @RequestParam("status") Integer status,
            javax.servlet.http.HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return userService.updateUserStatus(userId, status, token);
    }

    @GetMapping("/admin/stats")
    public Result<java.util.Map<String, Object>> getAdminStats(javax.servlet.http.HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return userService.getAdminStats(token);
    }
}

