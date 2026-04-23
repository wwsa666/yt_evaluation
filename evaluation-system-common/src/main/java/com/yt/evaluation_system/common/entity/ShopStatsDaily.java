package com.yt.evaluation_system.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("shop_stats_daily")
public class ShopStatsDaily implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;

    private LocalDate statDate;

    private Integer newReviews;

    private BigDecimal avgRating;

    private Integer totalReviews;

    private LocalDateTime createTime;
}
