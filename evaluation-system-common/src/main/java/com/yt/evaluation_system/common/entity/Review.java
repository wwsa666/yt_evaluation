package com.yt.evaluation_system.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("reviews")
public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long shopId;

    private Integer rating; // Overall rating (calculated or single)

    private Integer likes;

    private java.math.BigDecimal scoreTaste;

    private java.math.BigDecimal scoreEnv;

    private java.math.BigDecimal scoreService;

    private java.math.BigDecimal scoreValue;

    private String content;

    private LocalDateTime createTime;

    private Long parentId;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String nickname;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String username;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String avatar;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String shopName;
}

