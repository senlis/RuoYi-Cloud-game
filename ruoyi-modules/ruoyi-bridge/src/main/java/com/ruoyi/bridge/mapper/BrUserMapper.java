package com.ruoyi.bridge.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.bridge.domain.BrUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 渠道用户Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface BrUserMapper {

    /**
     * 查询用户
     *
     * @param userId 用户ID
     * @return 渠道用户
     */
    public BrUser selectByUserId(Long userId);

    /**
     * 根据平台标识查询用户
     *
     * @param identityId 平台唯一标识
     * @return 渠道用户
     */
    public BrUser selectByIdentityId(String identityId);

    /**
     * 根据渠道标识+渠道用户ID+服务器ID查询用户
     *
     * @param channelKey     渠道标识
     * @param channelUserId  渠道用户ID
     * @param serverId       服务器ID
     * @return 渠道用户
     */
    public BrUser selectByUserKey(@Param("channelKey") String channelKey,
                                  @Param("channelUserId") String channelUserId,
                                  @Param("serverId") Integer serverId);

    /**
     * 查询用户列表
     *
     * @param user 查询条件
     * @return 用户集合
     */
    public List<BrUser> selectList(BrUser user);

    /**
     * 新增用户
     *
     * @param user 渠道用户
     * @return 结果
     */
    public int insert(BrUser user);

    /**
     * 修改用户
     *
     * @param user 渠道用户
     * @return 结果
     */
    public int update(BrUser user);

    /**
     * 批量删除用户
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    public int deleteByIds(Long[] userIds);
}
