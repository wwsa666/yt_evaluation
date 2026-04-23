package com.yt.evaluation_system.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yt.evaluation_system.common.entity.Review;
import com.yt.evaluation_system.common.result.Result;

import java.util.List;

public interface ReviewService extends IService<Review> {
    Result<String> saveReview(Review review, String token);
    Result<List<Review>> queryReviewByShop(Long shopId, Integer current, Integer size);
    Result<List<Review>> queryReviewByUser(String token, Integer current, Integer size);
    Result<String> deleteReview(Long id, String token);
}

