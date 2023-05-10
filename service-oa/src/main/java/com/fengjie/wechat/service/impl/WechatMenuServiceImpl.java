package com.fengjie.wechat.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fengjie.model.wechat.Menu;
import com.fengjie.vo.wechat.MenuVo;
import com.fengjie.wechat.mapper.WechatMenuMapper;
import com.fengjie.wechat.service.WechatMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单 服务实现类
 * </p>
 *
 * @author fengjie
 * @since 2023-05-07
 */
@Service
public class WechatMenuServiceImpl extends ServiceImpl<WechatMenuMapper, Menu> implements WechatMenuService {
    @Autowired
    private WxMpService wxMpService;
    @Override
    public List<MenuVo> findMenuInfo() {
        List<MenuVo> list = new ArrayList<>();
        //查询所有菜单list集合
        List<Menu> menuList = baseMapper.selectList(null);
        //查询所有一级菜单 parent_id=0，返回一级菜单list集合
        List<Menu> oneMenuList = menuList.stream()
                                .filter(menu -> menu.getParentId().longValue() == 0)
                                .collect(Collectors.toList());
        //遍历一级菜单集合，得到每一个一级菜单
        for (Menu oneMenu : oneMenuList){
            //一级菜单menu转换menuVo
            MenuVo oneMenuVo = new MenuVo();
            BeanUtils.copyProperties(oneMenu,oneMenuVo);
            //获取每个一级菜单里的二级菜单，id和parent_id比较
            List<Menu> twoMenuList = menuList.stream()
                                .filter(menu -> menu.getParentId().longValue() == oneMenu.getId())
                                .collect(Collectors.toList());
            //把一级菜单里面所有的二级菜单获取到
            List<MenuVo> children = new ArrayList<>();
            for (Menu twoMenu : twoMenuList){
                MenuVo twoMenuVo = new MenuVo();
                BeanUtils.copyProperties(twoMenu,twoMenuVo);
                children.add(twoMenuVo);
            }
            //封装到一级菜单里的children集合
            oneMenuVo.setChildren(children);
            //将一级菜单及其children放到list集合中
            list.add(oneMenuVo);
        }
        return list;
    }

    @Override
    public void syncMenu() {
        List<MenuVo> menuVoList = this.findMenuInfo();
        //菜单
        JSONArray buttonList = new JSONArray();
        for(MenuVo oneMenuVo : menuVoList) {
            JSONObject one = new JSONObject();
            one.put("name", oneMenuVo.getName());
            if(CollectionUtils.isEmpty(oneMenuVo.getChildren())) {
                one.put("type", oneMenuVo.getType());
                one.put("url", "http://fengjie9090.5gzvip.91tunnel.com/#"+oneMenuVo.getUrl());
            } else {
                JSONArray subButton = new JSONArray();
                for(MenuVo twoMenuVo : oneMenuVo.getChildren()) {
                    JSONObject view = new JSONObject();
                    view.put("type", twoMenuVo.getType());
                    if(twoMenuVo.getType().equals("view")) {
                        view.put("name", twoMenuVo.getName());
                        //H5页面地址
                        view.put("url", "http://fengjie9090.5gzvip.91tunnel.com#"+twoMenuVo.getUrl());
                    } else {
                        view.put("name", twoMenuVo.getName());
                        view.put("key", twoMenuVo.getMeunKey());
                    }
                    subButton.add(view);
                }
                one.put("sub_button", subButton);
            }
            buttonList.add(one);
        }
        //菜单
        JSONObject button = new JSONObject();
        button.put("button", buttonList);
        try {
            wxMpService.getMenuService().menuCreate(button.toJSONString());
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeMenu() {
        try {
            wxMpService.getMenuService().menuDelete();
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
    }
}
