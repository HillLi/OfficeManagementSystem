package com.university.oms.design;

import com.university.oms.dto.DocumentRequest;
import com.university.oms.model.Document;
import com.university.oms.model.User;

import java.time.LocalDate;

public class DocumentFactory {
    public Document create(long id, DocumentRequest request, User applicant) {
        Document document = new Document();
        document.setId(id);
        document.setDocNo("校发〔" + LocalDate.now().getYear() + "〕" + id + "号");
        document.setTitle(request.getTitle());
        document.setDocType(request.getDocType());
        document.setUrgency(request.getUrgency());
        document.setSecrecyLevel(request.getSecrecyLevel());
        document.setKnowledgeScope(request.getKnowledgeScope());
        document.setContent(request.getContent());
        document.setApplicantId(applicant.getId());
        document.setDeptId(applicant.getDeptId());
        document.setStatus("draft");
        return document;
    }
}
