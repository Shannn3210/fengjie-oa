package com.fengjie.wechat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fengjie.model.wechat.Menu;
import com.fengjie.vo.wechat.MenuVo;

import java.util.List;

/**
 * <p>
 * 菜单 服务类
 * </p>
 *
 * @author fengjie
 * @since 2023-05-07
 */
public interface WechatMenuService extends IService<Menu> {

    List<MenuVo> findMenuInfo();

    void syncMenu();

    void removeMenu();
}
