package com.yt.evaluation_system.shop.controller;

import com.yt.evaluation_system.common.entity.Shop;
import com.yt.evaluation_system.common.entity.ShopStatsDaily;
import com.yt.evaluation_system.common.entity.ShopType;
import com.yt.evaluation_system.common.result.Result;
import com.yt.evaluation_system.shop.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    // ======================== 用户端接口 ========================

    @GetMapping("/type/list")
    public Result<List<ShopType>> queryTypeList() {
        return shopService.queryTypeList();
    }

    @GetMapping("/list")
    public Result<List<Shop>> queryShopList(
            @RequestParam(value = "typeId", required = false) Long typeId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return shopService.queryShopByType(typeId, name, current, size);
    }

    @GetMapping("/{id}")
    public Result<Shop> queryShopById(@PathVariable("id") Long id) {
        return shopService.queryShopById(id);
    }

    // ======================== 商家端接口 ========================

    @PostMapping("/create")
    public Result<String> createShop(@RequestBody Shop shop, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return shopService.createShop(shop, token);
    }

    @PutMapping("/update")
    public Result<String> updateShop(@RequestBody Shop shop, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return shopService.updateShop(shop, token);
    }

    @GetMapping("/my-shops")
    public Result<List<Shop>> myShops(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return shopService.myShops(token);
    }

    @GetMapping("/stats/{shopId}")
    public Result<List<ShopStatsDaily>> getShopStats(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "30") Integer days,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return shopService.getShopStats(shopId, days, token);
    }

    @PutMapping("/toggle-status/{shopId}")
    public Result<String> toggleShopStatus(@PathVariable Long shopId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return shopService.toggleShopStatus(shopId, token);
    }
}
