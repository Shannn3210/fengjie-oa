package com.fengjie.process.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengjie.common.result.Result;
import com.fengjie.model.process.ProcessType;
import com.fengjie.process.service.OaProcessTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 审批类型 前端控制器
 * </p>
 *
 * @author fengjie
 * @since 2023-05-02
 */
@Api(value = "审批类型", tags = "审批类型")
@RestController
@RequestMapping(value = "/admin/process/processType")
public class OaProcessTypeController {
    @Autowired
    private OaProcessTypeService oaProcessTypeService;

    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public Result index(@PathVariable Long page,
                        @PathVariable Long limit){
        Page<ProcessType> pageParam = new Page<>(page,limit);
        IPage<ProcessType> pageModel = oaProcessTypeService.page(pageParam);
        return Result.ok(pageModel);
    }
    @ApiOperation(value = "获取")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        ProcessType processType = oaProcessTypeService.getById(id);
        return Result.ok(processType);
    }
    @ApiOperation(value = "新增")
    @PostMapping("save")
    public Result save(@RequestBody ProcessType processType) {
        oaProcessTypeService.save(processType);
        return Result.ok();
    }
    @ApiOperation(value = "修改")
    @PutMapping("update")
    public Result updateById(@RequestBody ProcessType processType) {
        oaProcessTypeService.updateById(processType);
        return Result.ok();
    }
    @ApiOperation(value = "删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        oaProcessTypeService.removeById(id);
        return Result.ok();
    }

    @ApiOperation(value = "获取全部审批分类")
    @GetMapping("findAll")
    public Result findAll() {
        return Result.ok(oaProcessTypeService.list());
    }

}

