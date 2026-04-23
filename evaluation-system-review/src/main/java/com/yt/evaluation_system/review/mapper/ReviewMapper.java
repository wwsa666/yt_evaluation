package com.yt.evaluation_system.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yt.evaluation_system.common.entity.Review;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
    @Select("SELECT r.*, u.nickname, u.username, u.avatar FROM reviews r LEFT JOIN users u ON r.user_id = u.id WHERE r.shop_id = #{shopId} ORDER BY r.create_time ASC")
    Page<Review> queryReviewWithUserByShopId(Page<Review> page, @Param("shopId") Long shopId);

    @Select("SELECT r.*, s.name AS shop_name FROM reviews r LEFT JOIN shops s ON r.shop_id = s.id WHERE r.user_id = #{userId} ORDER BY r.create_time DESC")
    Page<Review> queryReviewWithShopByUserId(Page<Review> page, @Param("userId") Long userId);

    @Update("UPDATE shops s " +
            "SET s.comments_count = (SELECT COUNT(*) FROM reviews r WHERE r.shop_id = s.id AND r.parent_id IS NULL), " +
            "s.avg_rating = IFNULL((SELECT ROUND(AVG(r.rating), 1) FROM reviews r WHERE r.shop_id = s.id AND r.parent_id IS NULL AND r.rating > 0), 0) " +
            "WHERE s.id = #{shopId}")
    void updateShopRatingAndComments(@Param("shopId") Long shopId);
}

