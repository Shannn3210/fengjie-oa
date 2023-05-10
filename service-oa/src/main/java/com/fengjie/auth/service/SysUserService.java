package com.fengjie.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fengjie.model.system.SysUser;

import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author fengjie
 * @since 2023-04-24
 */
public interface SysUserService extends IService<SysUser> {

    void updateStatus(Long id, Integer status);

    SysUser getUserByUserName(String username);

    Map<String, Object> getCurrentUser();
}
