package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.Document;
import org.springframework.stereotype.Service;

/**
 * Template Method — document submission workflow.
 */
@Service
public class DocumentWorkflowService extends AbstractBusinessService<Document> {
    @Override
    protected void validate(Document doc) {
        if (doc.getTitle() == null || doc.getTitle().trim().isEmpty()) {
            throw new BusinessException("公文标题不能为空");
        }
        if (doc.getContent() == null || doc.getContent().trim().isEmpty()) {
            throw new BusinessException("公文正文不能为空");
        }
    }

    @Override
    protected Document doSubmit(Document doc, Long applicantId) {
        doc.setStatus("pending_dept");
        doc.setApplicantId(applicantId);
        return doc;
    }

    @Override
    protected void postSubmit(Document doc) {
        // hook for notifications, logging, etc.
    }
}
