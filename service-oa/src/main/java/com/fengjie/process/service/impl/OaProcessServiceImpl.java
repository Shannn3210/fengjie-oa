package com.fengjie.process.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengjie.auth.service.SysUserService;
import com.fengjie.model.process.Process;
import com.fengjie.model.process.ProcessRecord;
import com.fengjie.model.process.ProcessTemplate;
import com.fengjie.model.system.SysUser;
import com.fengjie.process.mapper.OaProcessMapper;
import com.fengjie.process.service.MessageService;
import com.fengjie.process.service.OaProcessRecordService;
import com.fengjie.process.service.OaProcessService;
import com.fengjie.process.service.OaProcessTemplateService;
import com.fengjie.security.custom.LoginUserInfoHelper;
import com.fengjie.vo.process.ApprovalVo;
import com.fengjie.vo.process.ProcessFormVo;
import com.fengjie.vo.process.ProcessQueryVo;
import com.fengjie.vo.process.ProcessVo;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ConnectionUtils;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author fengjie
 * @since 2023-05-03
 */
@Service
public class OaProcessServiceImpl extends ServiceImpl<OaProcessMapper, Process> implements OaProcessService {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private OaProcessTemplateService oaProcessTemplateService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private OaProcessRecordService oaProcessRecordService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private MessageService messageService;
    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo) {
        IPage<ProcessVo> pageModel = baseMapper.selectPage(pageParam, processQueryVo);
        return pageModel;
    }

    @Override
    public void deployByZip(String deployPath) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(deployPath);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        Deployment deployment = repositoryService.createDeployment()
                                .addZipInputStream(zipInputStream).name("").deploy();
        System.out.println(deployment.getName());
        System.out.println(deployment.getId());
    }


    @Override
    public void startUp(ProcessFormVo processFormVo) {
        Long userId = LoginUserInfoHelper.getUserId();
        SysUser sysUser = sysUserService.getById(userId);
        Long processTemplateId = processFormVo.getProcessTemplateId();
        ProcessTemplate processTemplate = oaProcessTemplateService.getById(processTemplateId);
        Process process = new Process();
        BeanUtils.copyProperties(processFormVo,process);
        process.setStatus(1);
        String workNo = System.currentTimeMillis() + "";
        process.setProcessCode(workNo);
        process.setUserId(LoginUserInfoHelper.getUserId());
        process.setFormValues(processFormVo.getFormValues());
        process.setTitle(sysUser.getName() + "发起" + processTemplate.getName() + "申请");
        baseMapper.insert(process);
        //启动流程实例
        //流程定义key
        String processDefinitionKey = processTemplate.getProcessDefinitionKey();
        //业务key
        String businessKey = String.valueOf(process.getId());
        //将表单数据放入流程实例中
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formData = jsonObject.getJSONObject("formData");
        Map<String, Object> map = new HashMap<>();
        //循环转换
        for (Map.Entry<String, Object> entry : formData.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        //流程参数
        Map<String, Object> variables = new HashMap<>();
        variables.put("data", map);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
        //计算下一个审批人，可能有多个（并行审批）
        List<Task> taskList = this.getCurrentTaskList(processInstance.getId());
        List<String> nameList = new ArrayList<>();
        for (Task task : taskList){
            String assignee = task.getAssignee();
            SysUser user = sysUserService.getUserByUserName(assignee);
            String userName = user.getName();
            nameList.add(userName);
            //推送消息给下一个审批人
            messageService.pushPendingMessage(process.getId(), sysUser.getId(), task.getId());
        }
        process.setProcessInstanceId(processInstance.getId());
        process.setDescription("等待" + StringUtils.join(nameList.toArray(), ",") + "审批");
        baseMapper.updateById(process);
        //记录操作行为
        oaProcessRecordService.record(process.getId(), 1, "发起申请");
    }

    @Override
    public IPage<ProcessVo> findPending(Page<Process> pageParam) {
        TaskQuery taskQuery = taskService.createTaskQuery().taskAssignee(LoginUserInfoHelper.getUsername()).orderByTaskCreateTime().desc();
        int begin = (int) ((pageParam.getCurrent() - 1) * pageParam.getSize());
        int size = (int) pageParam.getSize();
        List<Task> list = taskQuery.listPage(begin, size);
        long totalCount = taskQuery.count();

        List<ProcessVo> processList = new ArrayList<>();
        // 根据流程的业务ID查询实体并关联
        for (Task item : list) {
            String processInstanceId = item.getProcessInstanceId();
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            if (processInstance == null) {
                continue;
            }
            // 业务key
            String businessKey = processInstance.getBusinessKey();
            if (businessKey == null) {
                continue;
            }
            long processId = Long.parseLong(businessKey);
            Process process = baseMapper.selectById(processId);
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process, processVo);
            processVo.setTaskId(item.getId());
            processList.add(processVo);
        }
        IPage<ProcessVo> page = new Page<ProcessVo>(pageParam.getCurrent(), pageParam.getSize(), totalCount);
        page.setRecords(processList);
        return page;
    }

    @Override
    public Map<String, Object> show(Long id) {
        //根据流程id获取流程信息process
        Process process = baseMapper.selectById(id);
        //根据流程id获取流程记录信息
        LambdaQueryWrapper<ProcessRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ProcessRecord::getProcessId,id);
        List<ProcessRecord> processRecordList = oaProcessRecordService.list(lambdaQueryWrapper);
        //根据模板id查询模板信息
        ProcessTemplate processTemplate = oaProcessTemplateService.getById(process.getProcessTemplateId());
        //判断当前用户是否可以审批
        //可以看到信息不一定能审批，不能重复审批
        boolean isApprove = false;
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        for (Task task : taskList){
        //判断任务审批人是否是当前用户
            String username = LoginUserInfoHelper.getUsername();
            String taskAssignee = task.getAssignee();
            if (taskAssignee.equals(username)){
                isApprove = true;
            }
        }
        //查询数据封装到map中返回
        Map<String, Object> map = new HashMap<>();
        map.put("process", process);
        map.put("processRecordList", processRecordList);
        map.put("processTemplate", processTemplate);
        map.put("isApprove", isApprove);
        return map;
    }

    @Override
    public void approve(ApprovalVo approvalVo) {
        String taskId = approvalVo.getTaskId();
        Map<String, Object> variables = taskService.getVariables(taskId);
        for (Map.Entry<String,Object> entry : variables.entrySet()){
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
        //判断审批状态值
        //状态值等于1时，通过
        if (approvalVo.getStatus() == 1){
            //额外设置
            Map<String, Object> variable = new HashMap<String,Object>();
            //完成审批
            taskService.complete(taskId,variable);
        }else {
            //状态值等于-1时，驳回
            this.endTask(taskId);
        }
        String description = approvalVo.getStatus().intValue() == 1 ? "通过" : "驳回";
        oaProcessRecordService.record(approvalVo.getProcessId(),approvalVo.getStatus(),description);
        //查询下一个审批人
        Process process = baseMapper.selectById(approvalVo.getProcessId());
        //查询任务
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if (!CollectionUtils.isEmpty(taskList)){
            List<String> assigneeList = new ArrayList<>();
            for (Task task : taskList){
                SysUser sysUser = sysUserService.getUserByUserName(task.getAssignee());
                String username = sysUser.getName();
                assigneeList.add(username);
                //推送消息给下一个审批人
                messageService.pushPendingMessage(process.getId(), sysUser.getId(), task.getId());
            }
            process.setDescription("等待" + StringUtils.join(assigneeList.toArray(), ",") + "审批");
            process.setStatus(1);
        }else {
            if(approvalVo.getStatus().intValue() == 1) {
                process.setDescription("审批完成（同意）");
                process.setStatus(2);
            } else {
                process.setDescription("审批完成（拒绝）");
                process.setStatus(-1);
            }
        }
        //推送消息给申请人
        messageService.pushProcessedMessage(process.getId(), process.getUserId(), approvalVo.getStatus());
        baseMapper.updateById(process);
    }

    @Override
    public IPage<ProcessVo> findProcessed(Page<Process> pageParam) {
        // 根据当前人的ID查询
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery().taskAssignee(LoginUserInfoHelper.getUsername()).finished().orderByTaskCreateTime().desc();
        List<HistoricTaskInstance> list = query.listPage((int) ((pageParam.getCurrent() - 1) * pageParam.getSize()), (int) pageParam.getSize());
        long totalCount = query.count();

        List<ProcessVo> processList = new ArrayList<>();
        for (HistoricTaskInstance item : list) {
            String processInstanceId = item.getProcessInstanceId();
            Process process = this.getOne(new LambdaQueryWrapper<Process>().eq(Process::getProcessInstanceId, processInstanceId));
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process, processVo);
            processVo.setTaskId("0");
            processList.add(processVo);
        }
        IPage<ProcessVo> page = new Page<ProcessVo>(pageParam.getCurrent(), pageParam.getSize(), totalCount);
        page.setRecords(processList);
        return page;
    }

    @Override
    public IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam) {
        ProcessQueryVo processQueryVo = new ProcessQueryVo();
        processQueryVo.setUserId(LoginUserInfoHelper.getUserId());
        IPage<ProcessVo> page = baseMapper.selectPage(pageParam, processQueryVo);
        for (ProcessVo item : page.getRecords()) {
            item.setTaskId("0");
        }
        return page;
    }

    private void endTask(String taskId) {
        //  当前任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        List endEventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        // 并行任务可能为null
        if(CollectionUtils.isEmpty(endEventList)) {
            return;
        }
        FlowNode endFlowNode = (FlowNode) endEventList.get(0);
        FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());

        //  临时保存当前活动的原始方向
        List originalSequenceFlowList = new ArrayList<>();
        originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());
        //  清理活动方向
        currentFlowNode.getOutgoingFlows().clear();

        //  建立新方向
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId("newSequenceFlowId");
        newSequenceFlow.setSourceFlowElement(currentFlowNode);
        newSequenceFlow.setTargetFlowElement(endFlowNode);
        List newSequenceFlowList = new ArrayList<>();
        newSequenceFlowList.add(newSequenceFlow);
        //  当前节点指向新的方向
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);

        //  完成当前任务
        taskService.complete(task.getId());
    }

    public List<Task> getCurrentTaskList(String id) {
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(id).list();
        return taskList;
    }
}
