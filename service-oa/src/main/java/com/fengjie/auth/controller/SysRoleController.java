package com.fengjie.auth.controller;

import com.fengjie.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengjie.auth.service.SysRoleService;
import com.fengjie.model.system.SysRole;
import com.fengjie.vo.system.AssginRoleVo;
import com.fengjie.vo.system.SysRoleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "角色管理接口")
@RestController
@RequestMapping("/admin/system/sysRole")
public class SysRoleController {
    @Autowired
    private SysRoleService sysRoleService;
    //统一返回数据结果
    @ApiOperation("查询所有的角色")
    @GetMapping("findAll")
    public Result findAll(){
        //调用Service方法
        List<SysRole> list = sysRoleService.list();
        return Result.ok(list);
    }
    //条件分页查询
    //page 当前页  limit 每页显示记录数
    //SysRoleQueryVo 条件对象
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("条件分页查询")
    @GetMapping("{page}/{limit}")
    public Result pageQueryRole(@PathVariable Long page,
                                @PathVariable Long limit,
                                SysRoleQueryVo sysRoleQueryVo){
        //调用service的方法实现
        //创建Page对象，传递分页相关参数
        //page 当前页  limit 每页显示记录数
        Page<SysRole> pageParam = new Page<>(page,limit);
        //封装条件，判断条件是否为空，不为空进行封装
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        String roleName = sysRoleQueryVo.getRoleName();
        if(!StringUtils.isEmpty(roleName)) {
            //封装 like模糊查询
            wrapper.like(SysRole::getRoleName,roleName);
        }
        //调用方法实现
        IPage<SysRole> pageModel = sysRoleService.page(pageParam, wrapper);
        return Result.ok(pageModel);
    }
    @PreAuthorize("hasAuthority('bnt.sysRole.add')")
    @ApiOperation("新增角色")
    @PostMapping("save")
    public Result save(@RequestBody SysRole sysRole){
        boolean is_success = sysRoleService.save(sysRole);
        if ((is_success)){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("根据id查询")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id){
        SysRole sysRole = sysRoleService.getById(id);
        return Result.ok(sysRole);
    }
    @PreAuthorize("hasAuthority('bnt.sysRole.update')")
    @ApiOperation("修改角色")
    @PutMapping("update")
    public Result update(@RequestBody SysRole sysRole){
        boolean is_success = sysRoleService.updateById(sysRole);
        if ((is_success)){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("根据id删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        boolean is_success = sysRoleService.removeById(id);
        if ((is_success)){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("批量删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> list){
        boolean is_success = sysRoleService.removeByIds(list);
        if ((is_success)){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }
    @ApiOperation("获取所有角色")
    @GetMapping("/toAssign/{userId}")
    public Result toAssign(@PathVariable Long userId){
        Map<String, Object> roleMap = sysRoleService.findRoleByAdminId(userId);
        return Result.ok(roleMap);
    }
    @ApiOperation(value = "为用户分配角色")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestBody AssginRoleVo assginRoleVo) {
        sysRoleService.doAssign(assginRoleVo);
        return Result.ok();
    }
}
