package com.yt.evaluation_system.review.feign;

import com.yt.evaluation_system.common.entity.Shop;
import com.yt.evaluation_system.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "shop-service", path = "/shop")
public interface ShopClient {

    @GetMapping("/{id}")
    Result<Shop> getShopById(@PathVariable("id") Long id);

    @GetMapping("/list")
    Result<List<Shop>> getShopList();
}