package com.yt.evaluation_system.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yt.evaluation_system.common.entity.Review;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    @Update("UPDATE shops s " +
            "SET s.comments_count = (SELECT COUNT(*) FROM reviews r WHERE r.shop_id = s.id AND r.parent_id IS NULL), " +
            "s.avg_rating = IFNULL((SELECT ROUND(AVG(r.rating), 1) FROM reviews r WHERE r.shop_id = s.id AND r.parent_id IS NULL AND r.rating > 0), 0) " +
            "WHERE s.id = #{shopId}")
    void updateShopRatingAndComments(@Param("shopId") Long shopId);
}

