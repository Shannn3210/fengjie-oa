package com.fengjie.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fengjie.model.system.SysRole;
import com.fengjie.vo.system.AssginRoleVo;

import java.util.Map;

public interface SysRoleService extends IService<SysRole> {
    Map<String, Object> findRoleByAdminId(Long userId);

    void doAssign(AssginRoleVo assginRoleVo);

}
