package com.ruoyi.system.service.bridge;

import com.ruoyi.system.domain.BrUser;

import java.util.List;

/**
 * 用户信息Service接口
 * <p>
 * 查询渠道分库中的 br_user 表数据，自动切换数据源到对应渠道数据库。
 *
 * @author ruoyi
 */
public interface IBrUserService {

    /**
     * 查询用户列表
     *
     * @param channelKey 渠道KEY（用于路由到对应渠道数据库）
     * @param user       查询条件
     * @return 用户列表
     */
    List<BrUser> selectUserList(String channelKey, BrUser user);
}
