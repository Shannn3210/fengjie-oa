package com.fengjie.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fengjie.model.system.SysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author fengjie
 * @since 2023-04-24
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> findMenuListByUserId(@Param("userId") Long userId);

}
