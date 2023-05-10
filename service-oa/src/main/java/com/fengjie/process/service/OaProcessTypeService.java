package com.fengjie.process.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fengjie.model.process.ProcessTemplate;
import com.fengjie.model.process.ProcessType;

import java.util.List;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author fengjie
 * @since 2023-05-02
 */
public interface OaProcessTypeService extends IService<ProcessType> {

    List<ProcessType> findProcessType();
}
