package com.university.oms.repository;

import com.university.oms.model.*;

public interface DataPersistence {
    void saveDocument(Document document);
    void saveSealApplication(SealApplication application);
    void saveSeal(Seal seal);
    void saveMeeting(Meeting meeting);
    void saveTravel(Travel travel);
    void saveReport(Report report);
    void saveApproval(ApprovalRecord record);
}
