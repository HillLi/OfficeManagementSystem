package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.SealApplication;
import org.springframework.stereotype.Service;

// 模板方法模式：用印申请工作流的具体实现
@Service
public class SealWorkflowService extends AbstractBusinessService<SealApplication> {
    // 校验用印事由和材料不能为空
    @Override
    protected void validate(SealApplication app) {
        if (app.getPurpose() == null || app.getPurpose().trim().isEmpty()) {
            throw new BusinessException("用印事由不能为空");
        }
        if (app.getMaterialUrl() == null || app.getMaterialUrl().trim().isEmpty()) {
            throw new BusinessException("用印材料不能为空，严禁在空白纸张上用印");
        }
    }

    // 关联申请人到用印申请
    @Override
    protected SealApplication doSubmit(SealApplication app, Long applicantId) {
        app.setApplicantId(applicantId);
        return app;
    }

    @Override
    protected void postSubmit(SealApplication app) {
        // hook for notifications
    }
}
