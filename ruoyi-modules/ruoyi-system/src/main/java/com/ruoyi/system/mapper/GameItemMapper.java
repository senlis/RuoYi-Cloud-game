package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.GameItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 游戏道具配置Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface GameItemMapper {

    @Results(id = "itemResult", value = {
        @Result(property = "projectId", column = "project_id"),
        @Result(property = "itemId", column = "item_id"),
        @Result(property = "itemName", column = "item_name"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    @Select("SELECT project_id, item_id, item_name, created_at, updated_at"
            + "  FROM t_item_config"
            + " WHERE project_id = #{projectId} AND item_id = #{itemId}")
    GameItem selectByProjectAndId(@Param("projectId") Long projectId, @Param("itemId") Long itemId);

    @ResultMap("itemResult")
    @Select("<script>"
            + "SELECT project_id, item_id, item_name, created_at, updated_at"
            + "  FROM t_item_config"
            + "<where>"
            + "<if test='projectId != null'> AND project_id = #{projectId}</if>"
            + "<if test='itemId != null'> AND item_id = #{itemId}</if>"
            + "<if test='itemName != null and itemName != \"\"'> AND item_name like concat('%', #{itemName}, '%')</if>"
            + "</where>"
            + "ORDER BY item_id ASC"
            + "</script>")
    List<GameItem> selectList(GameItem item);

    @Insert("INSERT INTO t_item_config (project_id, item_id, item_name, created_at) "
            + "VALUES (#{projectId}, #{itemId}, #{itemName}, now())")
    int insert(GameItem item);

    @Insert("<script>"
            + "INSERT INTO t_item_config (project_id, item_id, item_name, created_at) VALUES "
            + "<foreach item='i' collection='list' separator=','>"
            + "(#{i.projectId}, #{i.itemId}, #{i.itemName}, now())"
            + "</foreach>"
            + "</script>")
    int batchInsert(@Param("list") List<GameItem> list);

    @Delete("DELETE FROM t_item_config WHERE project_id = #{projectId} AND item_id = #{itemId}")
    int deleteByProjectAndId(@Param("projectId") Long projectId, @Param("itemId") Long itemId);

    @Delete("DELETE FROM t_item_config WHERE project_id = #{projectId}")
    int deleteByProject(@Param("projectId") Long projectId);

    @Delete("DELETE FROM t_item_config")
    int deleteAll();
}
