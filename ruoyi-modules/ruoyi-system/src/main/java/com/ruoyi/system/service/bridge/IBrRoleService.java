package com.ruoyi.system.service.bridge;

import com.ruoyi.system.domain.BrRole;

import java.util.List;

/**
 * 角色信息Service接口
 * <p>
 * 查询渠道分库中的 br_role 表数据，自动切换数据源到对应渠道数据库。
 *
 * @author ruoyi
 */
public interface IBrRoleService {

    /**
     * 查询角色信息列表
     *
     * @param channelKey 渠道KEY（用于路由到对应渠道数据库）
     * @param role       查询条件
     * @return 角色信息列表
     */
    List<BrRole> selectRoleList(String channelKey, BrRole role);
}
