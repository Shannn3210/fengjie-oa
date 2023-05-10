package com.fengjie.auth.controller;


import com.fengjie.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengjie.auth.service.SysUserService;
import com.fengjie.common.utils.MD5;
import com.fengjie.model.system.SysUser;
import com.fengjie.vo.system.SysUserQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author fengjie
 * @since 2023-04-24
 */
@Api(tags = "用户管理接口")
@RestController
@CrossOrigin  //跨域
@RequestMapping("/admin/system/sysUser")
public class SysUserController {
    @Autowired
    private SysUserService sysUserService;
    @ApiOperation(value = "更新状态")
    @GetMapping("updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        sysUserService.updateStatus(id, status);
        return Result.ok();
    }
    @ApiOperation("用户条件分页查询")
    @GetMapping("{page}/{limit}")
    public Result index(@PathVariable Long page,
                        @PathVariable Long limit,
                        SysUserQueryVo sysUserQueryVo){
        Page<SysUser> pageParam = new Page<>(page,limit);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        String username = sysUserQueryVo.getKeyword();
        String createTimeBegin = sysUserQueryVo.getCreateTimeBegin();
        String createTimeEnd = sysUserQueryVo.getCreateTimeEnd();
        //判断条件值不为空
        //like 模糊查询
        if (!StringUtils.isEmpty(username)){
            wrapper.like(SysUser::getUsername,username);
        }
        if (!StringUtils.isEmpty(createTimeBegin)){
            wrapper.ge(SysUser::getCreateTime,createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)){
            wrapper.le(SysUser::getCreateTime,createTimeEnd);
        }
        IPage<SysUser> pageModel = sysUserService.page(pageParam, wrapper);
        return Result.ok(pageModel);
    }
    @ApiOperation(value = "获取用户")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        return Result.ok(user);
    }

    @ApiOperation(value = "保存用户")
    @PostMapping("save")
    public Result save(@RequestBody SysUser user) {
        //使用MD5进行密码加密
        String passwordMD5 = MD5.encrypt(user.getPassword());
        user.setPassword(passwordMD5);
        sysUserService.save(user);
        return Result.ok();
    }

    @ApiOperation(value = "更新用户")
    @PutMapping("update")
    public Result updateById(@RequestBody SysUser user) {
        sysUserService.updateById(user);
        return Result.ok();
    }

    @ApiOperation(value = "删除用户")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.ok();
    }
    @ApiOperation(value = "获取当前用户基本信息")
    @GetMapping("getCurrentUser")
    public Result getCurrentUser() {
        Map<String,Object> map = sysUserService.getCurrentUser();
        return Result.ok(map);
    }
}

