package com.hospital.registration.mapper;

import com.hospital.registration.model.entity.NoticeDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NoticeMapper {

    @Select("""
            <script>
            SELECT *
            FROM t_notice
            WHERE del_flag = 0
            <if test="enabled != null">
              AND enabled = #{enabled}
            </if>
            ORDER BY pinned DESC, publish_date DESC, id DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<NoticeDO> findPage(@Param("offset") int offset,
                            @Param("size") int size,
                            @Param("enabled") Boolean enabled);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM t_notice
            WHERE del_flag = 0
            <if test="enabled != null">
              AND enabled = #{enabled}
            </if>
            </script>
            """)
    long countPage(@Param("enabled") Boolean enabled);

    @Select("SELECT * FROM t_notice WHERE id = #{id} AND del_flag = 0 LIMIT 1")
    NoticeDO findById(@Param("id") Long id);

    @Insert("""
            INSERT INTO t_notice
            (tag, tag_type, title, summary, publish_date, pinned, path, enabled, del_flag, create_time, update_time)
            VALUES
            (#{tag}, #{tagType}, #{title}, #{summary}, #{publishDate}, #{pinned}, #{path}, #{enabled}, 0, NOW(), NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(NoticeDO noticeDO);

    @Update("""
            UPDATE t_notice
            SET tag = #{tag},
                tag_type = #{tagType},
                title = #{title},
                summary = #{summary},
                publish_date = #{publishDate},
                pinned = #{pinned},
                path = #{path},
                enabled = #{enabled},
                update_time = NOW()
            WHERE id = #{id}
              AND del_flag = 0
            """)
    int update(NoticeDO noticeDO);

    @Update("""
            UPDATE t_notice
            SET del_flag = 1,
                update_time = NOW()
            WHERE id = #{id}
              AND del_flag = 0
            """)
    int softDeleteById(@Param("id") Long id);
}
