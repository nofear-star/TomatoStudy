package com.tomato.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tomato.entity.UserPrivacy;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户隐私设置Mapper
 */
public interface UserPrivacyMapper extends BaseMapper<UserPrivacy> {
    
    @Select("SELECT * FROM userprivacy WHERE user_id = #{userId} LIMIT 1")
    UserPrivacy findByUserId(@Param("userId") Long userId);
}

