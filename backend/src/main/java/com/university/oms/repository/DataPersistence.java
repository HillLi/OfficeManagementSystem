package com.university.oms.repository;

import com.university.oms.model.*;

public interface DataPersistence {
    void saveUser(User user);
    void deleteUser(Long id);
    void saveDepartment(Department department);
    void deleteDepartment(Long id);
    void saveDictionaryType(DictionaryType type);
    void saveDictionaryItem(DictionaryItem item);
    void saveDocument(Document document);
    default void saveDocumentDistribution(DocumentDistribution distribution) { }
    void saveSealApplication(SealApplication application);
    void saveSeal(Seal seal);
    default void saveSealTransfer(SealTransfer transfer) { }
    void saveMeeting(Meeting meeting);
    void saveTravel(Travel travel);
    void saveReport(Report report);
    void saveApproval(ApprovalRecord record);
    void saveAttachment(Attachment attachment);
    void saveAuditLog(AuditLog auditLog);
    void saveNotification(Notification notification);
    void saveMailMessage(MailMessage mailMessage);
    void saveMailRecipient(MailRecipient mailRecipient);
    default void saveAnnouncement(Announcement announcement) { }
    void saveFlowInstance(FlowInstance flowInstance);
    void saveFlowTask(FlowTask flowTask);
}
