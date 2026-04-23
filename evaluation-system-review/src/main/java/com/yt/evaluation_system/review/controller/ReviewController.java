package com.yt.evaluation_system.review.controller;

import com.yt.evaluation_system.common.entity.Review;
import com.yt.evaluation_system.common.result.Result;
import com.yt.evaluation_system.review.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/save")
    public Result<String> saveReview(@RequestBody Review review, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return reviewService.saveReview(review, token);
    }

    @GetMapping("/shop/{shopId}")
    public Result<List<Review>> queryReviewByShop(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return reviewService.queryReviewByShop(shopId, current, size);
    }

    @GetMapping("/user")
    public Result<List<Review>> queryReviewByUser(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        String token = request.getHeader("Authorization");
        return reviewService.queryReviewByUser(token, current, size);
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> deleteReview(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return reviewService.deleteReview(id, token);
    }
}

