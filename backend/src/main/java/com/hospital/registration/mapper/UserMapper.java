package com.hospital.registration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.registration.model.entity.UserDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

    @Select("SELECT * FROM t_user WHERE id = #{id}")
    UserDO findById(Long id);

    @Select("SELECT * FROM t_user WHERE username = #{username} AND deletion_time = 0 AND del_flag = 0")
    UserDO findByUsername(String username);

    @Select("SELECT * FROM t_user WHERE (username = #{account} OR phone = #{account} OR email = #{account}) AND deletion_time = 0 AND del_flag = 0")
    UserDO findByAccount(String account);

    @Select("SELECT * FROM t_user WHERE phone = #{phone} AND deletion_time = 0 AND del_flag = 0")
    UserDO findByPhone(String phone);

    @Select("SELECT * FROM t_user WHERE email = #{email} AND deletion_time = 0 AND del_flag = 0")
    UserDO findByEmail(String email);

    @Select("SELECT COUNT(*) FROM t_user WHERE username = #{username} AND deletion_time = 0 AND del_flag = 0")
    boolean hasUsername(String username);

    @Select("SELECT COUNT(*) FROM t_user WHERE phone = #{phone} AND deletion_time = 0 AND del_flag = 0")
    boolean existsByPhone(String phone);

    @Select("SELECT COUNT(*) FROM t_user WHERE email = #{email} AND deletion_time = 0 AND del_flag = 0")
    boolean existsByEmail(String email);

    @Insert("INSERT INTO t_user (username, password, phone, email, user_type, deletion_time, del_flag, create_time, update_time) " +
            "VALUES (#{username}, #{password}, #{phone}, #{email}, #{role}, #{deletionTime}, #{delFlag}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserDO userDO);

    @Select("SELECT * FROM t_user WHERE deletion_time = 0 AND del_flag = 0")
    List<UserDO> findAll();
}
