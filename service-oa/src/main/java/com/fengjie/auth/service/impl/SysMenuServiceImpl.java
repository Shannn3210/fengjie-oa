package com.fengjie.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengjie.auth.mapper.SysMenuMapper;
import com.fengjie.auth.service.SysMenuService;
import com.fengjie.auth.service.SysRoleMenuService;
import com.fengjie.auth.utils.MenuHelper;
import com.fengjie.common.config.exception.FengjieException;
import com.fengjie.model.system.SysMenu;
import com.fengjie.model.system.SysRoleMenu;
import com.fengjie.vo.system.AssginMenuVo;
import com.fengjie.vo.system.MetaVo;
import com.fengjie.vo.system.RouterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author fengjie
 * @since 2023-04-24
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {
    @Autowired
    SysRoleMenuService sysRoleMenuService;
    @Override
    public List<SysMenu> findNodes() {
        //全部权限列表
        List<SysMenu> sysMenuList = baseMapper.selectList(null);
        if (CollectionUtils.isEmpty(sysMenuList)) return null;

        //构建树形数据
        List<SysMenu> resultList = MenuHelper.buildTree(sysMenuList);
        return resultList;
    }

    @Override
    public List<SysMenu> findSysMenuByRoleId(Long roleId) {
        //全部权限列表
        LambdaQueryWrapper<SysMenu> sysMenuWrapper = new LambdaQueryWrapper<>();
        sysMenuWrapper.eq(SysMenu::getStatus,1);
        List<SysMenu> allSysMenuList = baseMapper.selectList(sysMenuWrapper);
        //根据角色id获取角色权限
        LambdaQueryWrapper<SysRoleMenu> sysRoleMenuWrapper = new LambdaQueryWrapper<>();
        sysRoleMenuWrapper.eq(SysRoleMenu::getRoleId,roleId);
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuService.list(sysRoleMenuWrapper);
        //转换给角色id与角色权限对应Map对象
        List<Long> menuIdList = sysRoleMenuList.stream().map(c -> c.getMenuId()).collect(Collectors.toList());
        allSysMenuList.forEach(permission -> {
            if (menuIdList.contains(permission.getId())) {
                permission.setSelect(true);
            } else {
                permission.setSelect(false);
            }
        });
        List<SysMenu> sysMenuList = MenuHelper.buildTree(allSysMenuList);
        return sysMenuList;
    }

    @Transactional
    @Override
    public void doAssign(AssginMenuVo assignMenuVo) {
        LambdaQueryWrapper<SysRoleMenu> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysRoleMenu::getRoleId, assignMenuVo.getRoleId());
        sysRoleMenuService.remove(lambdaQueryWrapper);
        List<Long> menuIdList = assignMenuVo.getMenuIdList();
        for (Long menuId : menuIdList){
            if (StringUtils.isEmpty(menuId)){
                continue;
            }else {
                SysRoleMenu sysRoleMenu = new SysRoleMenu();
                sysRoleMenu.setRoleId(assignMenuVo.getRoleId());
                sysRoleMenu.setMenuId(menuId);
                sysRoleMenuService.save(sysRoleMenu);
            }
        }
    }

    @Override
    public List<RouterVo> findUserMenuListByUserId(Long userId) {
        List<SysMenu> sysMenuList = null;
        if (userId.longValue() == 1){
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus,1);
            wrapper.orderByAsc(SysMenu::getSortValue);
            sysMenuList = baseMapper.selectList(wrapper);
        }else {
            sysMenuList = baseMapper.findMenuListByUserId(userId);
        }
        List<SysMenu> sysMenuTreeList = MenuHelper.buildTree(sysMenuList);
        List<RouterVo> routerList = this.buildRouter(sysMenuTreeList);
        return routerList;
    }

    private List<RouterVo> buildRouter(List<SysMenu> menus) {
        List<RouterVo> routers = new ArrayList<>();
        for (SysMenu menu : menus){
            RouterVo router = new RouterVo();
            router.setHidden(false);
            router.setAlwaysShow(false);
            router.setPath(getRouterPath(menu));
            router.setComponent(menu.getComponent());
            router.setMeta(new MetaVo(menu.getName(), menu.getIcon()));
            //下一层数据部分
            List<SysMenu> children = menu.getChildren();
            if (menu.getType().intValue() == 1){
                List<SysMenu> hiddenMenuList = children.stream().
                        filter(item -> !StringUtils
                                        .isEmpty(item.getComponent()))
                                        .collect(Collectors.toList());
                for (SysMenu hiddenMenu : hiddenMenuList) {
                    RouterVo hiddenRouter = new RouterVo();
                    hiddenRouter.setHidden(true);
                    hiddenRouter.setAlwaysShow(false);
                    hiddenRouter.setPath(getRouterPath(hiddenMenu));
                    hiddenRouter.setComponent(hiddenMenu.getComponent());
                    hiddenRouter.setMeta(new MetaVo(hiddenMenu.getName(), hiddenMenu.getIcon()));
                    routers.add(hiddenRouter);
                }
            }else {
                if (!CollectionUtils.isEmpty(children)) {
                    if(children.size() > 0) {
                        router.setAlwaysShow(true);
                    }
                    router.setChildren(buildRouter(children));
                }
            }
            routers.add(router);
        }
        return routers;
    }

    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();
        if(menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }
    @Override
    public List<String> findUserPermsByUserId(Long userId) {
        List<SysMenu> sysMenuList = null;
        if (userId.longValue() == 1){
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus,1);
            sysMenuList = baseMapper.selectList(wrapper);
        }else {
            sysMenuList = baseMapper.findMenuListByUserId(userId);
        }
        List<String> permsList = sysMenuList.stream()
                            .filter(item -> item.getType() == 2)
                            .map(item -> item.getPerms()).collect(Collectors.toList());
        return permsList;
    }


    @Override
    public boolean removeById(Serializable id) {

        LambdaQueryWrapper<SysMenu> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysMenu::getParentId,id);
        Integer count = baseMapper.selectCount(lambdaQueryWrapper);
        if (count > 0) {
            throw new FengjieException(201,"菜单不能删除");
        }
        baseMapper.deleteById(id);
        return false;
    }
}
