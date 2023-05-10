package com.fengjie.process.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.fengjie.model.process.ProcessRecord;

/**
 * <p>
 * 审批记录 服务类
 * </p>
 *
 * @author fengjie
 * @since 2023-05-04
 */
public interface OaProcessRecordService extends IService<ProcessRecord> {
    void record(Long processId, Integer status, String description);
}
