package com.fengjie.process.controller.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengjie.auth.service.SysUserService;
import com.fengjie.common.result.Result;
import com.fengjie.model.process.Process;
import com.fengjie.model.process.ProcessTemplate;
import com.fengjie.model.process.ProcessType;
import com.fengjie.process.service.OaProcessService;
import com.fengjie.process.service.OaProcessTemplateService;
import com.fengjie.process.service.OaProcessTypeService;
import com.fengjie.vo.process.ApprovalVo;
import com.fengjie.vo.process.ProcessFormVo;
import com.fengjie.vo.process.ProcessVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "审批流管理")
@RestController
@RequestMapping(value="/admin/process")
@CrossOrigin  //跨域
public class ProcessController {
    @Autowired
    private OaProcessTypeService oaProcessTypeService;
    @Autowired
    private OaProcessTemplateService oaProcessTemplateService;
    @Autowired
    private OaProcessService oaProcessService;
    @Autowired
    private SysUserService sysUserService;
    @ApiOperation(value = "启动流程")
    @PostMapping("/startUp")
    public Result start(@RequestBody ProcessFormVo processFormVo) {
        oaProcessService.startUp(processFormVo);
        return Result.ok();
    }
    @ApiOperation(value = "获取审批模板")
    @GetMapping("getProcessTemplate/{processTemplateId}")
    public Result get(@PathVariable Long processTemplateId) {
        ProcessTemplate processTemplate = oaProcessTemplateService.getById(processTemplateId);
        return Result.ok(processTemplate);
    }
    @ApiOperation(value = "获取全部审批分类及模板")
    @GetMapping("findProcessType")
    public Result findProcessType() {
        List<ProcessType> list = oaProcessTypeService.findProcessType();
        return Result.ok(list);
    }
    @ApiOperation(value = "待处理")
    @GetMapping("/findPending/{page}/{limit}")
    public Result findPending(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit) {
        Page<Process> pageParam = new Page<>(page, limit);
        return Result.ok(oaProcessService.findPending(pageParam));
    }
    @ApiOperation(value = "获取审批详情")
    @GetMapping("show/{id}")
    public Result show(@PathVariable Long id) {
        Map<String,Object> map = oaProcessService.show(id);
        return Result.ok(map);
    }
    @ApiOperation(value = "审批")
    @PostMapping("approve")
    public Result approve(@RequestBody ApprovalVo approvalVo) {
        oaProcessService.approve(approvalVo);
        return Result.ok();
    }
    @ApiOperation(value = "已处理")
    @GetMapping("/findProcessed/{page}/{limit}")
    public Result findProcessed(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit) {
        Page<Process> pageParam = new Page<>(page,limit);
        return Result.ok(oaProcessService.findProcessed(pageParam));
    }
    @ApiOperation(value = "已发起")
    @GetMapping("/findStarted/{page}/{limit}")
    public Result findStarted(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit) {
        Page<ProcessVo> pageParam = new Page<>(page, limit);
        return Result.ok(oaProcessService.findStarted(pageParam));
    }

}
