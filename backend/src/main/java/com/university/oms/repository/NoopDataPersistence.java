package com.university.oms.repository;

import com.university.oms.model.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "oms.repository", havingValue = "memory", matchIfMissing = true)
public class NoopDataPersistence implements DataPersistence {
    public void saveDocument(Document document) { }
    public void saveSealApplication(SealApplication application) { }
    public void saveSeal(Seal seal) { }
    public void saveMeeting(Meeting meeting) { }
    public void saveTravel(Travel travel) { }
    public void saveReport(Report report) { }
    public void saveApproval(ApprovalRecord record) { }
}
