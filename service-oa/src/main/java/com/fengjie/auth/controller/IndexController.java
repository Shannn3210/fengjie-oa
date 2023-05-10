package com.fengjie.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fengjie.auth.service.SysMenuService;
import com.fengjie.auth.service.SysUserService;
import com.fengjie.common.config.exception.FengjieException;
import com.fengjie.common.jwt.JwtHelper;
import com.fengjie.common.result.Result;
import com.fengjie.common.utils.MD5;
import com.fengjie.model.system.SysUser;
import com.fengjie.vo.system.LoginVo;
import com.fengjie.vo.system.RouterVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "后台登陆管理")
@RestController
@RequestMapping("/admin/system/index")
public class IndexController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysMenuService sysMenuService;
    /**
     * 登录
     * @return
     */
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo){
        //{"code":20000,"data":{"token":"admin-token"}}
//        Map<String, Object> map = new HashMap<>();
//        map.put("token","admin-token");
//        return Result.ok(map);
        String username = loginVo.getUsername();
        LambdaQueryWrapper<SysUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysUser::getUsername,username);
        SysUser sysUser = sysUserService.getOne(lambdaQueryWrapper);
        //判断用户是否存在
        if(null == sysUser) {
            throw new FengjieException(201,"用户不存在");
        }
        //判断密码
        String password_db = sysUser.getPassword();
        String password_input = MD5.encrypt(loginVo.getPassword());
        if(!password_input.equals(password_db)) {
            throw new FengjieException(201,"密码错误");
        }
        //判断是否禁用
        if(sysUser.getStatus() == 0) {
            throw new FengjieException(201,"用户被禁用");
        }
        //生成token
        String token = JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());
        HashMap<String, Object> map = new HashMap<>();
        map.put("token",token);
        return Result.ok(map);
    }
    /**
     * 获取用户信息
     * @return
     */
    @GetMapping("info")
    public Result info(HttpServletRequest request){
        /**
         * {"code":20000,"data":{"roles":["admin"],
         * "introduction":"I am a super administrator",
         * "avatar":"https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif",
         * "name":"Super Admin"}}
         */
        String token = request.getHeader("token");
        Long userId = JwtHelper.getUserId(token);
        SysUser sysUser = sysUserService.getById(userId);
        List<RouterVo> routerList =sysMenuService.findUserMenuListByUserId(userId);
        List<String> permsList =sysMenuService.findUserPermsByUserId(userId);

        Map<String, Object> map = new HashMap<>();
        map.put("roles","[admin]");
        map.put("name",sysUser.getName());
        map.put("avatar","https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
        map.put("routers",routerList);
        map.put("buttons",permsList);
        return Result.ok(map);
    }
    /**
     * 退出
     * @return
     */
    @PostMapping("logout")
    public Result logout(){
        return Result.ok();
    }

}
