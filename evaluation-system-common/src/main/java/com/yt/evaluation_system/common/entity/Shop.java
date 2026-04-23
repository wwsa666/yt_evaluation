package com.yt.evaluation_system.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("shops")
public class Shop implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long typeId;

    private String name;

    private String address;

    private String image;

    private BigDecimal avgRating;

    private Integer commentsCount;

    private Long ownerId;

    private String description;

    private Integer status; // 0=下架, 1=正常营业

    private LocalDateTime createTime;
}

