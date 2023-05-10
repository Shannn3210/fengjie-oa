package com.fengjie.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fengjie.model.process.ProcessTemplate;
import com.fengjie.model.process.ProcessType;
import com.fengjie.process.mapper.OaProcessTypeMapper;
import com.fengjie.process.service.OaProcessTemplateService;
import com.fengjie.process.service.OaProcessTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author fengjie
 * @since 2023-05-02
 */
@Service
public class OaProcessTypeServiceImpl extends ServiceImpl<OaProcessTypeMapper, ProcessType> implements OaProcessTypeService {

    @Autowired
    private OaProcessTemplateService oaProcessTemplateService;
    @Override
    public List<ProcessType> findProcessType() {
        List<ProcessType> processTypes = baseMapper.selectList(null);
        for (ProcessType processType : processTypes){
            Long typeId = processType.getId();
            LambdaQueryWrapper<ProcessTemplate> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(ProcessTemplate::getProcessTypeId,typeId);
            List<ProcessTemplate> processTemplateList = oaProcessTemplateService.list(lambdaQueryWrapper);
            for (ProcessTemplate processTemplate : processTemplateList){
                System.out.println(processTemplate);
            }
            processType.setProcessTemplateList(processTemplateList);
        }
        return processTypes;
    }
}
