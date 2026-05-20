package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.SealApplication;
import org.springframework.stereotype.Service;

/**
 * Template Method — seal application workflow.
 */
@Service
public class SealWorkflowService extends AbstractBusinessService<SealApplication> {
    @Override
    protected void validate(SealApplication app) {
        if (app.getPurpose() == null || app.getPurpose().trim().isEmpty()) {
            throw new BusinessException("用印事由不能为空");
        }
        if (app.getMaterialUrl() == null || app.getMaterialUrl().trim().isEmpty()) {
            throw new BusinessException("用印材料不能为空，严禁在空白纸张上用印");
        }
    }

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
