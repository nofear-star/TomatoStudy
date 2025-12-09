package com.tomato.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tomato.entity.UserCurrency;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户货币Mapper
 */
public interface UserCurrencyMapper extends BaseMapper<UserCurrency> {
    
    @Select("SELECT * FROM usercurrency WHERE user_id = #{userId} LIMIT 1")
    UserCurrency findByUserId(@Param("userId") Long userId);
}

