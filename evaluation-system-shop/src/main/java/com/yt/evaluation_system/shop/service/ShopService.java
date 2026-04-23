package com.yt.evaluation_system.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yt.evaluation_system.common.entity.Shop;
import com.yt.evaluation_system.common.entity.ShopStatsDaily;
import com.yt.evaluation_system.common.entity.ShopType;
import com.yt.evaluation_system.common.result.Result;

import java.util.List;

public interface ShopService extends IService<Shop> {
    Result<List<Shop>> queryShopByType(Long typeId, String name, Integer current, Integer size);
    Result<Shop> queryShopById(Long id);
    Result<List<ShopType>> queryTypeList();
    void updateShopRatingAndComments(Long shopId);

    // 商家端接口
    Result<String> createShop(Shop shop, String token);
    Result<String> updateShop(Shop shop, String token);
    Result<List<Shop>> myShops(String token);
    Result<List<ShopStatsDaily>> getShopStats(Long shopId, Integer days, String token);
    Result<String> toggleShopStatus(Long shopId, String token);
}
