package com.fengjie.auth.activity;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class ProcessTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;
    @Test
    public void SingleSuspendProcessInstance() {
        String processInstanceId = "8bdff984-ab53-11ed-9b17-f8e43b734677";
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        //获取到当前流程定义是否为暂停状态   suspended方法为true代表为暂停   false就是运行的
        boolean suspended = processInstance.isSuspended();
        if (suspended) {
            runtimeService.activateProcessInstanceById(processInstanceId);
            System.out.println("流程实例:" + processInstanceId + "激活");
        } else {
            runtimeService.suspendProcessInstanceById(processInstanceId);
            System.out.println("流程实例:" + processInstanceId + "挂起");
        }
    }
    /**
     * 查询流程定义
     */
    @Test
    public void findProcessDefinitionList(){
        ProcessDefinition qingjia = repositoryService
                                    .createProcessDefinitionQuery()
                                    .processDefinitionKey("qingjia")
                                    .singleResult();
        boolean suspended = qingjia.isSuspended();
        if (suspended){
            repositoryService.activateProcessDefinitionById(qingjia.getId(),true,null);
            System.out.println("流程定义:" + qingjia.getId() + "激活");
        }else {
            repositoryService.activateProcessDefinitionById(qingjia.getId(),true,null);
            System.out.println("流程定义:" + qingjia.getId() + "挂起");
        }
    }
    /**
     * 启动流程实例，添加businessKey
     */
    @Test
    public void startUpProcessAddBusinessKey(){
        String businessKey = "1";
        // 启动流程实例，指定业务标识businessKey，也就是请假申请单id
        ProcessInstance processInstance = runtimeService.
                startProcessInstanceByKey("qingjia",businessKey);
        // 输出
        System.out.println("业务id:"+processInstance.getBusinessKey());
    }
    /**
     * 查询已处理历史任务
     */
    @Test
    public void findProcessedTaskList() {
        //张三已处理过的历史任务
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskAssignee("zhangsan").finished().list();
        for (HistoricTaskInstance historicTaskInstance : list) {
            System.out.println("流程实例id：" + historicTaskInstance.getProcessInstanceId());
            System.out.println("任务id：" + historicTaskInstance.getId());
            System.out.println("任务负责人：" + historicTaskInstance.getAssignee());
            System.out.println("任务名称：" + historicTaskInstance.getName());
        }
    }

    /**
     * 完成任务
     */
    @Test
    public void completTask(){
        Task task = taskService.createTaskQuery()
                .taskAssignee("zhangsan")  //要查询的负责人
                .singleResult();//返回一条

        //完成任务,参数：任务id
        taskService.complete(task.getId());
    }

    @Test
    public void findPendingTaskList(){
        String assignee = "zhangsan";
        List<Task> list = taskService.createTaskQuery().taskAssignee(assignee).list();
        for (Task task : list){
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

    @Test
    public void startProcess(){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("qingjia");
        //输出实例的相关信息
        System.out.println("流程定义id：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例id：" + processInstance.getId());
        System.out.println("当前活动Id：" + processInstance.getActivityId());
    }
    @Test
    //流程部署
    public void deployProcess(){
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("process/qingjia.bpmn20.xml")
                .addClasspathResource("process/qingjia.png")
                .name("请假申请流程")
                .deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());
    }
}
