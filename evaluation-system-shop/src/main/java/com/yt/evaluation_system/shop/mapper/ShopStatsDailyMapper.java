package com.yt.evaluation_system.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yt.evaluation_system.common.entity.ShopStatsDaily;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ShopStatsDailyMapper extends BaseMapper<ShopStatsDaily> {

    /**
     * UPSERT: 如果当天已有记录就更新，否则插入
     */
    @Insert("INSERT INTO shop_stats_daily (shop_id, stat_date, new_reviews, avg_rating, total_reviews) " +
            "VALUES (#{shopId}, CURDATE(), " +
            "  (SELECT COUNT(*) FROM reviews WHERE shop_id = #{shopId} AND parent_id IS NULL AND DATE(create_time) = CURDATE()), " +
            "  IFNULL((SELECT ROUND(AVG(rating), 1) FROM reviews WHERE shop_id = #{shopId} AND parent_id IS NULL AND rating > 0), 0), " +
            "  (SELECT COUNT(*) FROM reviews WHERE shop_id = #{shopId} AND parent_id IS NULL) " +
            ") ON DUPLICATE KEY UPDATE " +
            "  new_reviews = VALUES(new_reviews), " +
            "  avg_rating = VALUES(avg_rating), " +
            "  total_reviews = VALUES(total_reviews)")
    void upsertTodayStats(@Param("shopId") Long shopId);
}
