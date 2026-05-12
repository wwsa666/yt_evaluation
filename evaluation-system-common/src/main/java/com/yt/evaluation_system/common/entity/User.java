package com.yt.evaluation_system.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer role; // 0=普通用户, 1=商家, 2=系统管理员

    private Integer verifyStatus; // 0=未认证, 1=待审核, 2=已认证, 3=已驳回

    private String verifyImage;

    private Integer merchantStatus; // 0=未申请, 1=审核中, 2=已通过, 3=已驳回

    private Integer status; // 1=正常, 0=封禁
}

