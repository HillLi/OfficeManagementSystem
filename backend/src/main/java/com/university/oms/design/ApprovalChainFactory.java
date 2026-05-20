package com.university.oms.design;

import org.springframework.stereotype.Component;

/**
 * Factory that builds approval chains per business type (Chain of Responsibility).
 */
@Component
public class ApprovalChainFactory {
    private final DeptHeadHandler deptHead;
    private final OfficeAdminHandler officeAdmin;
    private final SchoolLeaderHandler schoolLeader;
    private final SecurityHandler security;
    private final FinanceHandler finance;
    private final SecretReviewHandler secretReview;

    public ApprovalChainFactory(DeptHeadHandler deptHead, OfficeAdminHandler officeAdmin,
                                SchoolLeaderHandler schoolLeader, SecurityHandler security,
                                FinanceHandler finance, SecretReviewHandler secretReview) {
        this.deptHead = deptHead;
        this.officeAdmin = officeAdmin;
        this.schoolLeader = schoolLeader;
        this.security = security;
        this.finance = finance;
        this.secretReview = secretReview;
    }

    public ApprovalHandler getChain(String bizType) {
        switch (bizType) {
            case "document":
                deptHead.setNext(officeAdmin);
                officeAdmin.setNext(schoolLeader);
                schoolLeader.setNext(null);
                return deptHead;
            case "seal":
                deptHead.setNext(officeAdmin);
                officeAdmin.setNext(null);
                return deptHead;
            case "meeting":
                security.setNext(deptHead);
                deptHead.setNext(null);
                return security;
            case "travel":
                deptHead.setNext(finance);
                finance.setNext(null);
                return deptHead;
            case "report":
                secretReview.setNext(deptHead);
                deptHead.setNext(null);
                return secretReview;
            default:
                return deptHead;
        }
    }
}
