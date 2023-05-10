package com.fengjie.process.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fengjie.auth.service.SysUserService;
import com.fengjie.model.process.Process;
import com.fengjie.model.process.ProcessTemplate;
import com.fengjie.model.system.SysUser;
import com.fengjie.process.service.MessageService;
import com.fengjie.process.service.OaProcessService;
import com.fengjie.process.service.OaProcessTemplateService;
import com.fengjie.security.custom.LoginUserInfoHelper;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {
    @Resource
    private WxMpService wxMpService;

    @Resource
    private OaProcessService oaprocessService;

    @Resource
    private OaProcessTemplateService oaprocessTemplateService;

    @Resource
    private SysUserService sysUserService;
    @Override
    public void pushPendingMessage(Long processId, Long userId, String taskId) {
        Process process = oaprocessService.getById(processId);
        ProcessTemplate processTemplate = oaprocessTemplateService.getById(process.getProcessTemplateId());
        SysUser sysUser = sysUserService.getById(userId);
        SysUser submitSysUser = sysUserService.getById(process.getUserId());
        String openid = sysUser.getOpenId();
        //方便测试，给默认值（开发者本人的openId）
        if(StringUtils.isEmpty(openid)) {
            openid = "oe3sr60Jibq9E927gwS7YB8v5WSE";
        }
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(openid)//要推送的用户openid
                .templateId("J0Ef4QFhxCBtjBHNe4P0vdw1mZvELjxvqHaRcmbeCww")//模板id
                .url("http://fengjie9090.5gzvip.91tunnel.com/#/show/"+processId+"/"+taskId)//点击模板消息要访问的网址
                .build();
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuffer content = new StringBuffer();
        for (Map.Entry entry : formShowData.entrySet()) {
            content.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
        }
        templateMessage.addData(new WxMpTemplateData("first", submitSysUser.getName()+"提交了"+processTemplate.getName()+"审批申请，请注意查看。", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        templateMessage.addData(new WxMpTemplateData("content", content.toString(), "#272727"));
        String msg = null;
        try {
            msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        log.info("推送消息返回：{}", msg);
    }

    @Override
    public void pushProcessedMessage(Long processId, Long userId, Integer status) {
        Process process = oaprocessService.getById(processId);
        ProcessTemplate processTemplate = oaprocessTemplateService.getById(process.getProcessTemplateId());
        SysUser sysUser = sysUserService.getById(userId);
        SysUser currentSysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        String openid = sysUser.getOpenId();
        if(StringUtils.isEmpty(openid)) {
            openid = "oe3sr60Jibq9E927gwS7YB8v5WSE";
        }
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(openid)//要推送的用户openid
                .templateId("ETwU18Amb6az3DsCBIcptdrK1M01qFY-Dyg8l6SS6D0")//模板id
                .url("http://oa.atguigu.cn/#/show/"+processId+"/0")//点击模板消息要访问的网址
                .build();
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuffer content = new StringBuffer();
        for (Map.Entry entry : formShowData.entrySet()) {
            content.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
        }
        templateMessage.addData(new WxMpTemplateData("first", "你发起的"+processTemplate.getName()+"审批申请已经被处理了，请注意查看。", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword3", currentSysUser.getName(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword4", status == 1 ? "审批通过" : "审批拒绝", status == 1 ? "#009966" : "#FF0033"));
        templateMessage.addData(new WxMpTemplateData("content", content.toString(), "#272727"));
        String msg = null;
        try {
            msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        log.info("推送消息返回：{}", msg);
    }
}
