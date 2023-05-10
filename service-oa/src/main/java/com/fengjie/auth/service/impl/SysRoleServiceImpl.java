package com.fengjie.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengjie.auth.mapper.SysRoleMapper;

import com.fengjie.auth.service.SysRoleService;
import com.fengjie.auth.service.SysUserRoleService;
import com.fengjie.model.system.SysRole;
import com.fengjie.model.system.SysUserRole;
import com.fengjie.vo.system.AssginRoleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysUserRoleService sysUserRoleService;
    @Override
    public Map<String, Object> findRoleByAdminId(Long userId) {
        List<SysRole> allRolesList = baseMapper.selectList(null);

        LambdaQueryWrapper<SysUserRole> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> existUserRoleList = sysUserRoleService.list(lambdaQueryWrapper);
        List<Long> existRoleIdList = existUserRoleList.stream().map(c->c.getRoleId()).collect(Collectors.toList());

        //对角色进行分类
        List<SysRole> assginRoleList = new ArrayList<>();
        for (SysRole role : allRolesList) {
            //已分配
            if(existRoleIdList.contains(role.getId())) {
                assginRoleList.add(role);
            }
        }

        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("assginRoleList", assginRoleList);
        roleMap.put("allRolesList", allRolesList);
        return roleMap;
    }

    @Transactional
    @Override
    public void doAssign(AssginRoleVo assginRoleVo) {
        //根据userid删除原来的角色数据
        LambdaQueryWrapper<SysUserRole> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysUserRole::getUserId,assginRoleVo.getUserId());
        sysUserRoleService.remove(lambdaQueryWrapper);

        //重新进行分配
        for(Long roleId : assginRoleVo.getRoleIdList()) {
            if(StringUtils.isEmpty(roleId)) continue;
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(assginRoleVo.getUserId());
            userRole.setRoleId(roleId);
            sysUserRoleService.save(userRole);
        }
    }
}
