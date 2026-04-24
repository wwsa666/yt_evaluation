package com.yt.evaluation_system.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yt.evaluation_system.common.entity.Review;
import com.yt.evaluation_system.common.entity.Shop;
import com.yt.evaluation_system.common.entity.User;
import com.yt.evaluation_system.common.result.Result;
import com.yt.evaluation_system.review.feign.ShopClient;
import com.yt.evaluation_system.review.feign.UserClient;
import com.yt.evaluation_system.review.mapper.ReviewMapper;
import com.yt.evaluation_system.review.service.ReviewService;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private UserClient userClient;

    @Autowired
    private ShopClient shopClient;

    @Override
    public Result<String> saveReview(Review review, String token) {
        if (StringUtils.isBlank(token)) {
            return Result.fail("未登录");
        }
        String userIdStr = stringRedisTemplate.opsForValue().get("login:token:" + token);
        if (StringUtils.isBlank(userIdStr)) {
            return Result.fail("登录已过期");
        }
        
        Long userId = Long.valueOf(userIdStr);
        review.setUserId(userId);
        
        // 防刷分：如果是主评论（打分），检查该用户是否已对该商铺打过分
        if (review.getParentId() == null) {
            LambdaQueryWrapper<Review> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(Review::getUserId, userId)
                        .eq(Review::getShopId, review.getShopId())
                        .isNull(Review::getParentId);
            if (this.count(checkWrapper) > 0) {
                return Result.fail("您已经评价过该商铺了，只能打分一次");
            }
        }
        
        this.save(review);
        // 异步发送消息更新商铺评分和评论数
        try {
            rocketMQTemplate.convertAndSend("shop-rating-update-topic", review.getShopId());
        } catch (Exception e) {
            System.err.println("RocketMQ发送失败，降级为直接数据库更新: " + e.getMessage());
            this.baseMapper.updateShopRatingAndComments(review.getShopId());
        }
        
        return Result.ok("评价成功");
    }

    @Override
    public Result<String> deleteReview(Long id, String token) {
        if (StringUtils.isBlank(token)) {
            return Result.fail("未登录");
        }
        String userIdStr = stringRedisTemplate.opsForValue().get("login:token:" + token);
        if (StringUtils.isBlank(userIdStr)) {
            return Result.fail("登录已过期");
        }
        Long userId = Long.valueOf(userIdStr);

        Review review = this.getById(id);
        if (review == null) {
            return Result.fail("评论不存在");
        }
        if (!review.getUserId().equals(userId)) {
            return Result.fail("无权删除他人的评论");
        }

        // 删除主评论及所有子评论（盖楼回复）
        LambdaQueryWrapper<Review> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(Review::getId, id).or().eq(Review::getParentId, id);
        this.remove(deleteWrapper);

        // 异步通知更新评分和评论数
        try {
            rocketMQTemplate.convertAndSend("shop-rating-update-topic", review.getShopId());
        } catch (Exception e) {
            System.err.println("RocketMQ发送失败，降级为直接数据库更新: " + e.getMessage());
            this.baseMapper.updateShopRatingAndComments(review.getShopId());
        }
        return Result.ok("删除成功");
    }

@Override
    public Result<List<Review>> queryReviewByShop(Long shopId, Integer current, Integer size) {
        Page<Review> page = new Page<>(current, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getShopId, shopId).orderByDesc(Review::getCreateTime);
        Page<Review> resultPage = this.page(page, wrapper);

        for (Review review : resultPage.getRecords()) {
            Result<User> userResult = userClient.getUserInfo(review.getUserId());
            if (userResult != null && userResult.getData() != null) {
                User user = userResult.getData();
                review.setNickname(user.getNickname());
                review.setUsername(user.getUsername());
                review.setAvatar(user.getAvatar());
            }
        }

        return Result.ok(resultPage.getRecords(), resultPage.getTotal());
    }

@Override
    public Result<List<Review>> queryReviewByUser(String token, Integer current, Integer size) {
        if (StringUtils.isBlank(token)) {
            return Result.fail("未登录");
        }
        String userIdStr = stringRedisTemplate.opsForValue().get("login:token:" + token);
        if (StringUtils.isBlank(userIdStr)) {
            return Result.fail("登录已过期");
        }
        Long userId = Long.valueOf(userIdStr);

        Page<Review> page = new Page<>(current, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getUserId, userId).orderByDesc(Review::getCreateTime);
        Page<Review> resultPage = this.page(page, wrapper);

        for (Review review : resultPage.getRecords()) {
            Result<Shop> shopResult = shopClient.getShopById(review.getShopId());
            if (shopResult != null && shopResult.getData() != null) {
                review.setShopName(shopResult.getData().getName());
            }
        }

        return Result.ok(resultPage.getRecords(), resultPage.getTotal());
    }
}

