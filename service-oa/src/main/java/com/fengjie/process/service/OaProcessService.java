package com.fengjie.process.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fengjie.model.process.Process;
import com.fengjie.vo.process.ApprovalVo;
import com.fengjie.vo.process.ProcessFormVo;
import com.fengjie.vo.process.ProcessQueryVo;
import com.fengjie.vo.process.ProcessVo;

import java.util.Map;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author fengjie
 * @since 2023-05-03
 */
public interface OaProcessService extends IService<Process> {
    //审批管理列表
    IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo);
    //部署流程定义
    void deployByZip(String deployPath);

    void startUp(ProcessFormVo processFormVo);
    //查询待处理列表
    IPage<ProcessVo> findPending(Page<Process> pageParam);

    Map<String, Object> show(Long id);

    void approve(ApprovalVo approvalVo);

    IPage<ProcessVo> findProcessed(Page<Process> pageParam);

    IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam);

}
