package com.yt.evaluation_system.review.feign;

import com.yt.evaluation_system.common.entity.User;
import com.yt.evaluation_system.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/user")
public interface UserClient {

    @GetMapping("/info/{id}")
    Result<User> getUserInfo(@PathVariable("id") Long id);
}