package com.ruoyi.system.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.domain.GameResourceRequest;
import com.ruoyi.system.mapper.GameResourceRequestMapper;
import com.ruoyi.system.service.IGameResourceRequestService;
import com.ruoyi.system.service.IServerPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 内部资源申请Service实现
 *
 * @author ruoyi
 */
@Service
public class GameResourceRequestServiceImpl implements IGameResourceRequestService {

    @Autowired
    private GameResourceRequestMapper mapper;

    @Autowired
    private IServerPushService serverPushService;

    @Override
    public List<GameResourceRequest> selectList(GameResourceRequest req) {
        return mapper.selectList(req);
    }

    @Override
    public GameResourceRequest selectById(Long requestId) {
        return mapper.selectById(requestId);
    }

    @Override
    public int insert(GameResourceRequest req) {
        req.setStatus(0); // 草稿
        return mapper.insert(req);
    }

    @Override
    public int update(GameResourceRequest req) {
        GameResourceRequest exist = mapper.selectById(req.getRequestId());
        if (exist == null) throw new ServiceException("申请单不存在");
        if (exist.getStatus() != 0 && exist.getStatus() != 4)
            throw new ServiceException("仅草稿或驳回状态的申请可以编辑");
        req.setStatus(0);
        return mapper.update(req);
    }

    @Override
    public int deleteById(Long requestId) {
        GameResourceRequest exist = mapper.selectById(requestId);
        if (exist == null) throw new ServiceException("申请单不存在");
        if (exist.getStatus() != 0 && exist.getStatus() != 4)
            throw new ServiceException("仅草稿或驳回状态的申请可以删除");
        return mapper.deleteById(requestId);
    }

    @Override
    public int submitForAudit(Long requestId) {
        GameResourceRequest exist = mapper.selectById(requestId);
        if (exist == null) throw new ServiceException("申请单不存在");
        if (exist.getStatus() != 0)
            throw new ServiceException("仅草稿状态的申请可以提交");
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approve(Long requestId) {
        GameResourceRequest exist = mapper.selectById(requestId);
        if (exist == null) throw new ServiceException("申请单不存在");
        if (exist.getStatus() != 0)
            throw new ServiceException("仅待审批状态的申请可以通过");

        List<Integer> failedIds = pushToServers(exist);
        GameResourceRequest update = new GameResourceRequest();
        update.setRequestId(requestId);
        update.setStatus(failedIds.isEmpty() ? 2 : 3);
        update.setFailedServerIds(failedIds.isEmpty() ? null
                : failedIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        return mapper.update(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reject(Long requestId, String remark) {
        GameResourceRequest exist = mapper.selectById(requestId);
        if (exist == null) throw new ServiceException("申请单不存在");
        if (exist.getStatus() != 0)
            throw new ServiceException("仅待审批状态的申请可以驳回");
        GameResourceRequest update = new GameResourceRequest();
        update.setRequestId(requestId);
        update.setStatus(4);
        update.setAuditRemark(remark);
        return mapper.update(update);
    }

    @Override
    public int revoke(Long requestId) {
        GameResourceRequest exist = mapper.selectById(requestId);
        if (exist == null) throw new ServiceException("申请单不存在");
        if (exist.getStatus() != 0)
            throw new ServiceException("仅待审批状态的申请可以撤回");
        GameResourceRequest update = new GameResourceRequest();
        update.setRequestId(requestId);
        update.setStatus(5);
        return mapper.update(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int retry(Long requestId) {
        GameResourceRequest exist = mapper.selectById(requestId);
        if (exist == null) throw new ServiceException("申请单不存在");
        if (exist.getFailedServerIds() == null || exist.getFailedServerIds().isEmpty())
            throw new ServiceException("没有需要重试的失败服务器");

        GameResourceRequest retryReq = new GameResourceRequest();
        retryReq.setRequestId(requestId);
        retryReq.setProjectId(exist.getProjectId());
        retryReq.setServerIds(exist.getFailedServerIds());
        retryReq.setPlayerIds(exist.getPlayerIds());
        retryReq.setResources(exist.getResources());

        List<Integer> stillFailed = pushToServers(retryReq);
        GameResourceRequest update = new GameResourceRequest();
        update.setRequestId(requestId);
        update.setStatus(stillFailed.isEmpty() ? 2 : 3);
        update.setFailedServerIds(stillFailed.isEmpty() ? null
                : stillFailed.stream().map(String::valueOf).collect(Collectors.joining(",")));
        return mapper.update(update);
    }

    private List<Integer> pushToServers(GameResourceRequest req) {
        if (req.getServerIds() == null || req.getServerIds().isEmpty()) return Collections.emptyList();
        List<Integer> serverIds = Arrays.stream(req.getServerIds().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).map(Integer::parseInt)
                .collect(Collectors.toList());
        if (serverIds.isEmpty()) return Collections.emptyList();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("requestId", String.valueOf(req.getRequestId()));
        params.put("playerIds", req.getPlayerIds() != null ? req.getPlayerIds() : "");
        params.put("resources", req.getResources() != null ? req.getResources() : "");

        Map<Integer, Boolean> results = serverPushService.pushToServers(
                req.getProjectId(), serverIds, "/resource", params);

        return results.entrySet().stream()
                .filter(e -> !e.getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
