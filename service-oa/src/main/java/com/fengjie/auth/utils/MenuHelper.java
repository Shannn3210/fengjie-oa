package com.fengjie.auth.utils;

import com.fengjie.model.system.SysMenu;

import java.util.ArrayList;
import java.util.List;

public class MenuHelper {
    public static List<SysMenu> buildTree(List<SysMenu> sysMenuList) {
        ArrayList<SysMenu> trees = new ArrayList<>();
        for (SysMenu sysMenu : sysMenuList){
            if (sysMenu.getParentId() == 0){
                trees.add(findChildren(sysMenu,sysMenuList));
            }
        }
        return trees;
    }

    private static SysMenu findChildren(SysMenu sysMenu, List<SysMenu> sysMenuList) {
        sysMenu.setChildren(new ArrayList<SysMenu>());
        for (SysMenu it : sysMenuList) {
            if(sysMenu.getId().longValue() == it.getParentId().longValue()) {
                if (sysMenu.getChildren() == null) {
                    sysMenu.setChildren(new ArrayList<>());
                }
                sysMenu.getChildren().add(findChildren(it,sysMenuList));
            }
        }
        return sysMenu;
    }
}
