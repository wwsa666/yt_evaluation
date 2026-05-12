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
import com.yt.evaluation_system.review.mapper.ReviewLikeMapper;
import com.yt.evaluation_system.review.service.ReviewService;
import com.yt.evaluation_system.common.utils.JwtUtils;
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

    @Autowired
    private ReviewLikeMapper reviewLikeMapper;

    @Override
    public Result<String> saveReview(Review review, String token) {
        if (StringUtils.isBlank(token)) {
            return Result.fail("未登录");
        }
        Long userId = JwtUtils.getUserId(token);
        if (userId == null) {
            return Result.fail("登录已过期");
        }
        
        review.setUserId(userId);
        
        // 获取用户信息校验认证状态
        Result<User> userRes = userClient.getUserInfo(userId);
        if (userRes == null || userRes.getData() == null) {
            return Result.fail("获取用户信息失败");
        }
        User user = userRes.getData();
        if (user.getVerifyStatus() == null || user.getVerifyStatus() != 2) {
            return Result.fail("仅通过认证的学生可发布评价");
        }
        
        // 校验是否已点评过该商铺
        if (review.getParentId() == null) {
            LambdaQueryWrapper<Review> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(Review::getUserId, userId)
                        .eq(Review::getShopId, review.getShopId())
                        .isNull(Review::getParentId);
            if (this.count(checkWrapper) > 0) {
                return Result.fail("您已经评价过该商铺，只能评价一次");
            }

            // 计算平均分
            if (review.getScoreTaste() != null && review.getScoreEnv() != null && review.getScoreService() != null && review.getScoreValue() != null) {
                java.math.BigDecimal total = review.getScoreTaste().add(review.getScoreEnv()).add(review.getScoreService()).add(review.getScoreValue());
                int avgRating = total.divide(new java.math.BigDecimal(4), java.math.RoundingMode.HALF_UP).intValue();
                review.setRating(avgRating);
            }
        }
        
        review.setLikes(0);
        this.save(review);
        // 异步发送消息更新商铺评分
        try {
            rocketMQTemplate.convertAndSend("shop-rating-update-topic", review.getShopId());
        } catch (Exception e) {
            System.err.println("RocketMQ发送失败，降级为直接数据库更新: " + e.getMessage());
            this.baseMapper.updateShopRatingAndComments(review.getShopId());
        }
        
        return Result.ok("评价发布成功");
    }

    @Override
    public Result<String> likeReview(Long id, String token) {
        if (StringUtils.isBlank(token)) return Result.fail("未登录");
        Long userId = JwtUtils.getUserId(token);
        if (userId == null) return Result.fail("登录已过期");

        com.yt.evaluation_system.common.entity.ReviewLike like = new com.yt.evaluation_system.common.entity.ReviewLike();
        like.setReviewId(id);
        like.setUserId(userId);
        
        try {
            reviewLikeMapper.insert(like);
        } catch (Exception e) {
            return Result.fail("您已经点过赞了");
        }

        Review review = this.getById(id);
        if (review != null) {
            review.setLikes(review.getLikes() + 1);
            this.updateById(review);
        }
        return Result.ok("点赞成功");
    }

    @Override
    public Result<String> deleteReview(Long id, String token) {
        if (StringUtils.isBlank(token)) {
            return Result.fail("未登录");
        }
        Long userId = JwtUtils.getUserId(token);
        if (userId == null) {
            return Result.fail("登录已过期");
        }

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
        Long userId = JwtUtils.getUserId(token);
        if (userId == null) {
            return Result.fail("登录已过期");
        }

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

    @Override
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<Review>> getAdminReviewList(Integer current, Integer size, String token) {
        Page<Review> page = new Page<>(current, size);
        this.page(page);
        for (Review review : page.getRecords()) {
            Result<User> userResult = userClient.getUserInfo(review.getUserId());
            if (userResult != null && userResult.getData() != null) {
                review.setUsername(userResult.getData().getUsername());
            }
        }
        return Result.ok(page);
    }

    @Override
    public Result<String> adminDeleteReview(Long id, String token) {
        Review review = this.getById(id);
        if (review == null) return Result.fail("评论不存在");
        
        LambdaQueryWrapper<Review> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(Review::getId, id).or().eq(Review::getParentId, id);
        this.remove(deleteWrapper);

        try {
            rocketMQTemplate.convertAndSend("shop-rating-update-topic", review.getShopId());
        } catch (Exception e) {
            this.baseMapper.updateShopRatingAndComments(review.getShopId());
        }
        return Result.ok("删除成功");
    }

    @Override
    public Result<java.util.Map<String, Object>> getAdminStats(String token) {
        long totalReviews = this.count();
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("totalReviews", totalReviews);
        return Result.ok(map);
    }
}
