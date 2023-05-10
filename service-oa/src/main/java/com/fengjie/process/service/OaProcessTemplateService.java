package com.fengjie.process.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fengjie.model.process.ProcessTemplate;
import com.fengjie.vo.process.ProcessFormVo;

/**
 * <p>
 * 审批模板 服务类
 * </p>
 *
 * @author fengjie
 * @since 2023-05-02
 */
public interface OaProcessTemplateService extends IService<ProcessTemplate> {

    IPage<ProcessTemplate> selectPageProcessTemplate(Page<ProcessTemplate> pageParam);

    void publish(Long id);
}
