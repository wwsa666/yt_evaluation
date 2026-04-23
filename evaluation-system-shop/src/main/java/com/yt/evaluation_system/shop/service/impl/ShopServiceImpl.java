package com.yt.evaluation_system.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yt.evaluation_system.common.entity.Shop;
import com.yt.evaluation_system.common.entity.ShopStatsDaily;
import com.yt.evaluation_system.common.entity.ShopType;
import com.yt.evaluation_system.common.result.Result;
import com.yt.evaluation_system.shop.mapper.ShopMapper;
import com.yt.evaluation_system.shop.mapper.ShopStatsDailyMapper;
import com.yt.evaluation_system.shop.mapper.ShopTypeMapper;
import com.yt.evaluation_system.shop.service.ShopService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Autowired
    private ShopTypeMapper shopTypeMapper;

    @Autowired
    private ShopStatsDailyMapper shopStatsDailyMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // ======================== 用户端接口 ========================

    @Override
    public Result<List<Shop>> queryShopByType(Long typeId, String name, Integer current, Integer size) {
        Page<Shop> page = new Page<>(current, size);
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        // 只展示正常营业的店铺
        wrapper.eq(Shop::getStatus, 1);
        if (typeId != null && typeId > 0) {
            wrapper.eq(Shop::getTypeId, typeId);
        }
        if (StringUtils.isNotBlank(name)) {
            wrapper.like(Shop::getName, name);
        }
        wrapper.orderByDesc(Shop::getId);
        Page<Shop> resultPage = this.page(page, wrapper);
        return Result.ok(resultPage.getRecords(), resultPage.getTotal());
    }

    @Override
    public Result<Shop> queryShopById(Long id) {
        Shop shop = this.getById(id);
        if (shop == null) {
            return Result.fail("商铺不存在");
        }
        return Result.ok(shop);
    }

    @Override
    public Result<List<ShopType>> queryTypeList() {
        LambdaQueryWrapper<ShopType> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ShopType::getSort);
        return Result.ok(shopTypeMapper.selectList(wrapper));
    }

    @Override
    public void updateShopRatingAndComments(Long shopId) {
        this.baseMapper.updateShopRatingAndComments(shopId);
        // 同时更新当日统计快照
        try {
            shopStatsDailyMapper.upsertTodayStats(shopId);
        } catch (Exception e) {
            System.err.println("更新每日统计快照失败: " + e.getMessage());
        }
    }

    // ======================== 商家端接口 ========================

    /**
     * 从 Token 解析出用户 ID，失败返回 null
     */
    private Long resolveUserId(String token) {
        if (StringUtils.isBlank(token)) return null;
        String userIdStr = stringRedisTemplate.opsForValue().get("login:token:" + token);
        if (StringUtils.isBlank(userIdStr)) return null;
        return Long.valueOf(userIdStr);
    }

    @Override
    public Result<String> createShop(Shop shop, String token) {
        Long userId = resolveUserId(token);
        if (userId == null) return Result.fail("未登录");

        shop.setOwnerId(userId);
        shop.setAvgRating(new java.math.BigDecimal("0.0"));
        shop.setCommentsCount(0);
        shop.setStatus(1); // 默认正常营业
        this.save(shop);
        return Result.ok("店铺创建成功");
    }

    @Override
    public Result<String> updateShop(Shop shop, String token) {
        Long userId = resolveUserId(token);
        if (userId == null) return Result.fail("未登录");

        Shop existing = this.getById(shop.getId());
        if (existing == null) return Result.fail("店铺不存在");
        if (!existing.getOwnerId().equals(userId)) return Result.fail("无权修改他人店铺");

        // 只允许修改基础信息
        existing.setName(shop.getName());
        existing.setTypeId(shop.getTypeId());
        existing.setAddress(shop.getAddress());
        existing.setImage(shop.getImage());
        existing.setDescription(shop.getDescription());
        this.updateById(existing);
        return Result.ok("修改成功");
    }

    @Override
    public Result<List<Shop>> myShops(String token) {
        Long userId = resolveUserId(token);
        if (userId == null) return Result.fail("未登录");

        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getOwnerId, userId).orderByDesc(Shop::getId);
        return Result.ok(this.list(wrapper));
    }

    @Override
    public Result<List<ShopStatsDaily>> getShopStats(Long shopId, Integer days, String token) {
        Long userId = resolveUserId(token);
        if (userId == null) return Result.fail("未登录");

        // 校验该店铺是否属于当前用户
        Shop shop = this.getById(shopId);
        if (shop == null || !shop.getOwnerId().equals(userId)) {
            return Result.fail("无权查看");
        }

        LambdaQueryWrapper<ShopStatsDaily> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopStatsDaily::getShopId, shopId)
               .ge(ShopStatsDaily::getStatDate, java.time.LocalDate.now().minusDays(days))
               .orderByAsc(ShopStatsDaily::getStatDate);
        return Result.ok(shopStatsDailyMapper.selectList(wrapper));
    }

    @Override
    public Result<String> toggleShopStatus(Long shopId, String token) {
        Long userId = resolveUserId(token);
        if (userId == null) return Result.fail("未登录");

        Shop shop = this.getById(shopId);
        if (shop == null || !shop.getOwnerId().equals(userId)) {
            return Result.fail("无权操作");
        }

        shop.setStatus(shop.getStatus() == 1 ? 0 : 1);
        this.updateById(shop);
        return Result.ok(shop.getStatus() == 1 ? "已上架" : "已下架");
    }
}
