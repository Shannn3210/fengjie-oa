package com.fengjie.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fengjie.model.system.SysMenu;
import com.fengjie.vo.system.AssginMenuVo;
import com.fengjie.vo.system.RouterVo;

import java.util.List;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author fengjie
 * @since 2023-04-24
 */
public interface SysMenuService extends IService<SysMenu> {
    /**
     * 菜单树形数据
     */
    List<SysMenu> findNodes();

    /**
     * 根据角色获取授权权限数据
     */
    List<SysMenu> findSysMenuByRoleId(Long roleId);

    /**
     * 保存角色权限
     */
    void doAssign(AssginMenuVo assignMenuVo);

    List<String> findUserPermsByUserId(Long userId);

    List<RouterVo> findUserMenuListByUserId(Long userId);
}
