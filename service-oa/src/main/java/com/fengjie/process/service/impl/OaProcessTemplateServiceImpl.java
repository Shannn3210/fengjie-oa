package com.fengjie.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengjie.auth.service.SysUserService;
import com.fengjie.model.process.ProcessTemplate;
import com.fengjie.model.process.ProcessType;
import com.fengjie.process.mapper.OaProcessTemplateMapper;
import com.fengjie.process.service.OaProcessService;
import com.fengjie.process.service.OaProcessTemplateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengjie.process.service.OaProcessTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 审批模板 服务实现类
 * </p>
 *
 * @author fengjie
 * @since 2023-05-02
 */
@Service
public class OaProcessTemplateServiceImpl extends ServiceImpl<OaProcessTemplateMapper, ProcessTemplate> implements OaProcessTemplateService {

    @Autowired
    private OaProcessService oaProcessService;

    @Autowired
    private OaProcessTypeService oaProcessTypeService;

    @Override
    public IPage<ProcessTemplate> selectPageProcessTemplate(Page<ProcessTemplate> pageParam) {
        Page<ProcessTemplate> processTemplatePage = baseMapper.selectPage(pageParam,null);
        List<ProcessTemplate> processTemplateList = processTemplatePage.getRecords();
        for(ProcessTemplate processTemplate : processTemplateList) {
            LambdaQueryWrapper<ProcessType> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(ProcessType::getId,processTemplate.getProcessTypeId());
            ProcessType processType = oaProcessTypeService.getOne(lambdaQueryWrapper);
            if(null == processType) continue;
            processTemplate.setProcessTypeName(processType.getName());
        }
        return processTemplatePage;
    }

    @Override
    public void publish(Long id) {
        //修改发布状态
        ProcessTemplate processTemplate = baseMapper.selectById(id);
        processTemplate.setStatus(1);
        baseMapper.updateById(processTemplate);
        //流程定义部署
        if (!StringUtils.isEmpty(processTemplate.getProcessDefinitionPath())){
            oaProcessService.deployByZip(processTemplate.getProcessDefinitionPath());
        }
    }

}
