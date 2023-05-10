package com.fengjie.process.service.impl;

import com.fengjie.auth.service.SysUserService;
import com.fengjie.model.process.ProcessRecord;
import com.fengjie.model.system.SysUser;
import com.fengjie.process.mapper.OaProcessRecordMapper;
import com.fengjie.process.service.OaProcessRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengjie.security.custom.LoginUserInfoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 审批记录 服务实现类
 * </p>
 *
 * @author fengjie
 * @since 2023-05-04
 */
@Service
public class OaProcessRecordServiceImpl extends ServiceImpl<OaProcessRecordMapper, ProcessRecord> implements OaProcessRecordService {
    @Autowired
    private SysUserService sysUserService;
    @Override
    public void record(Long processId, Integer status, String description) {
        Long userId = LoginUserInfoHelper.getUserId();
        SysUser sysUser = sysUserService.getById(userId);
        ProcessRecord processRecord = new ProcessRecord();
        processRecord.setProcessId(processId);
        processRecord.setStatus(status);
        processRecord.setDescription(description);
        processRecord.setOperateUserId(userId);
        processRecord.setOperateUser(sysUser.getName());
        baseMapper.insert(processRecord);
    }
}
