#!/usr/bin/env python3
"""Comprehensive Functional Test for OMS - Tests all 82 endpoints across 8 roles"""

import json, sys, urllib.request, urllib.error

BASE = "http://localhost:8080/api"
results = []

def req(method, path, token=None, body=None):
    url = BASE + path
    data = json.dumps(body).encode("utf-8") if body else None
    r = urllib.request.Request(url, data=data, method=method)
    r.add_header("Content-Type", "application/json; charset=utf-8")
    if token:
        r.add_header("Authorization", f"Bearer {token}")
    try:
        with urllib.request.urlopen(r) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        try:
            return json.loads(e.read().decode("utf-8"))
        except:
            return {"success": False, "message": f"HTTP {e.code}"}
    except Exception as e:
        return {"success": False, "message": str(e)}

def login(username):
    r = req("POST", "/auth/login", body={"username": username, "password": "123456"})
    if r.get("success"):
        return r["data"]["token"]
    return None

def extid(r):
    d = r.get("data")
    if d and isinstance(d, dict):
        return d.get("id")
    return None

def log(status, name, detail=""):
    results.append((status, name, detail))

def ok(r, name):
    if r.get("success"):
        log("PASS", name)
        return True
    log("FAIL", name, r.get("message", "unknown"))
    return False

def fail(r, name):
    if not r.get("success"):
        log("PASS", name, "correctly rejected")
        return True
    log("FAIL", name, "should have been rejected")
    return False

print("=" * 50)
print("  OMS Comprehensive Functional Test")
print("=" * 50)

# ========== 1. AUTH ==========
print("\n=== 1. Authentication ===")
users = {}
for u in ["admin", "user", "head", "leader", "office", "finance", "security", "keeper"]:
    t = login(u)
    users[u] = t
    log("PASS" if t else "FAIL", f"Login [{u}]")

fail(req("POST", "/auth/login", body={"username": "admin", "password": "wrong"}), "Login [wrong pwd]")
fail(req("POST", "/auth/login", body={"username": "nobody", "password": "123456"}), "Login [bad user]")
fail(req("GET", "/documents", "invalidtoken"), "GET /documents [bad token]")

ok(req("POST", "/auth/logout", users["admin"]), "POST /auth/logout")
users["admin"] = login("admin")

ok(req("GET", "/auth/user-options", users["user"]), "GET /auth/user-options")
ok(req("GET", "/auth/dept-options", users["user"]), "GET /auth/dept-options")
ok(req("GET", "/auth/users", users["admin"]), "GET /auth/users [admin]")
fail(req("GET", "/auth/users", users["user"]), "GET /auth/users [user blocked]")

# ========== 2. DICTIONARY ==========
print("\n=== 2. Dictionary Management ===")
ok(req("GET", "/dictionaries", users["user"]), "GET /dictionaries [catalog]")
ok(req("GET", "/dictionaries/version", users["user"]), "GET /dictionaries/version")
ok(req("GET", "/admin/dictionaries/types", users["admin"]), "GET /admin/dictionaries/types")

r = req("POST", "/admin/dictionaries/types", users["admin"], {"dictType": "ft_test_1", "dictName": "FT Type", "enabled": True})
ok(r, "POST /admin/dictionaries/types [create]")

ok(req("PUT", "/admin/dictionaries/types/ft_test_1", users["admin"],
       {"dictType": "ft_test_1", "dictName": "FT Type Updated", "enabled": True}),
   "PUT /admin/dictionaries/types/{dictType}")

ok(req("GET", "/admin/dictionaries/types/ft_test_1/items", users["admin"]),
   "GET /admin/dictionaries/types/{dictType}/items")

ok(req("POST", "/admin/dictionaries/types/ft_test_1/items", users["admin"],
       {"dictCode": "ft1", "dictLabel": "FT Item", "sortOrder": 1, "enabled": True}),
   "POST /admin/dict items [create]")

ok(req("PUT", "/admin/dictionaries/types/ft_test_1/items/ft1", users["admin"],
       {"dictCode": "ft1", "dictLabel": "FT Item Updated", "sortOrder": 2, "enabled": False}),
   "PUT /admin/dict items/{code}")

fail(req("POST", "/admin/dictionaries/types", users["user"], {"dictType": "hack", "dictName": "hack"}),
     "Dict create [user blocked]")

# ========== 3. USER MANAGEMENT ==========
print("\n=== 3. User Management ===")
ok(req("GET", "/admin/users", users["admin"]), "GET /admin/users")

r = req("POST", "/admin/users", users["admin"],
        {"username": "ftuser1", "password": "123456", "realName": "FT User", "deptId": 4, "email": "ft@test.com", "roleKeys": "office_user"})
ok(r, "POST /admin/users [create]")
uid = extid(r)

if uid:
    ok(req("GET", f"/admin/users/{uid}", users["admin"]), "GET /admin/users/{id}")
    ok(req("PUT", f"/admin/users/{uid}", users["admin"], {"realName": "FT User Updated", "email": "ft2@test.com"}),
       "PUT /admin/users/{id}")
    ok(req("DELETE", f"/admin/users/{uid}", users["admin"]), "DELETE /admin/users/{id}")

ok(req("GET", "/admin/depts", users["admin"]), "GET /admin/depts")

r = req("POST", "/admin/depts", users["admin"], {"deptName": "FT Dept", "parentId": 0})
ok(r, "POST /admin/depts [create]")
did = extid(r)

if did:
    ok(req("PUT", f"/admin/depts/{did}", users["admin"], {"deptName": "FT Dept Updated"}),
       "PUT /admin/depts/{id}")
    ok(req("DELETE", f"/admin/depts/{did}", users["admin"]), "DELETE /admin/depts/{id}")

ok(req("GET", "/admin/roles", users["admin"]), "GET /admin/roles")
fail(req("GET", "/admin/users", users["user"]), "RBAC: user GET /admin/users")

# ========== 4. ORG TREE ==========
print("\n=== 4. Organization Tree ===")
ok(req("GET", "/org/tree", users["admin"]), "GET /org/tree [admin]")
ok(req("GET", "/org/tree", users["user"]), "GET /org/tree [user]")

# ========== 5. DOCUMENT ==========
print("\n=== 5. Document Lifecycle ===")
r = req("POST", "/documents", users["user"],
        {"title": "FT Doc", "docType": "通知", "secrecyLevel": "公开", "content": "test", "applicantId": 2})
ok(r, "POST /documents [create]")
doc_id = extid(r)

ok(req("GET", "/documents", users["user"]), "GET /documents [list]")

if doc_id:
    ok(req("POST", f"/documents/{doc_id}/ai-review", users["user"]), "POST /documents/{id}/ai-review")

ok(req("POST", "/documents/ai-draft", users["user"],
       {"docType": "通知", "topic": "FT draft", "keyPoints": "p1,p2"}),
   "POST /documents/ai-draft")

if doc_id:
    ok(req("POST", f"/documents/{doc_id}/submit", users["user"]), "POST /documents/{id}/submit")
    ok(req("POST", f"/approvals/document/{doc_id}", users["head"],
           {"action": "approve", "opinion": "dept ok"}), "Approve doc [head]")
    ok(req("POST", f"/approvals/document/{doc_id}", users["office"],
           {"action": "approve", "opinion": "office ok"}), "Approve doc [office]")
    ok(req("POST", f"/approvals/document/{doc_id}", users["leader"],
           {"action": "approve", "opinion": "signed"}), "Approve doc [leader]")

    r = req("POST", f"/documents/{doc_id}/distributions", users["office"],
            {"receiverId": 2, "receiverDeptId": 4})
    ok(r, "POST /documents/{id}/distributions")
    dist_id = extid(r)

    ok(req("GET", f"/documents/{doc_id}/distributions", users["office"]),
       "GET /documents/{id}/distributions")

    if dist_id:
        ok(req("POST", f"/documents/{doc_id}/distributions/{dist_id}/remind", users["office"]),
           "POST distributions/{id}/remind")
        ok(req("POST", f"/documents/{doc_id}/distributions/{dist_id}/receipt", users["user"]),
           "POST distributions/{id}/receipt")

    ok(req("POST", f"/documents/{doc_id}/archive", users["office"]), "POST /documents/{id}/archive")

# Document rejection
r = req("POST", "/documents", users["user"],
        {"title": "FT Reject", "docType": "通知", "secrecyLevel": "公开", "content": "reject test", "applicantId": 2})
rej_id = extid(r)
if rej_id:
    req("POST", f"/documents/{rej_id}/submit", users["user"])
    ok(req("POST", f"/approvals/document/{rej_id}", users["head"],
           {"action": "reject", "opinion": "revise please"}), "Reject doc [head]")

# ========== 6. SEAL ==========
print("\n=== 6. Seal Management ===")
ok(req("GET", "/seals", users["user"]), "GET /seals")
ok(req("GET", "/seals/applications", users["user"]), "GET /seals/applications [list]")

r = req("POST", "/seals/applications", users["user"],
        {"sealId": 2, "applicantId": 2, "purpose": "FT seal", "copies": 1, "takeOut": False, "matterLevel": "常规事项"})
ok(r, "POST /seals/applications [create]")
seal_id = extid(r)

if seal_id:
    ok(req("POST", f"/seals/applications/{seal_id}/submit", users["user"]), "POST /seals/{id}/submit")
    ok(req("POST", f"/approvals/seal/{seal_id}", users["head"], {"action": "approve", "opinion": "ok"}),
       "Approve seal [head]")
    ok(req("POST", f"/approvals/seal/{seal_id}", users["office"], {"action": "approve", "opinion": "ok"}),
       "Approve seal [office]")
    ok(req("POST", f"/seals/applications/{seal_id}/used?keeperId=8", users["keeper"]),
       "POST /seals/{id}/used")
    ok(req("POST", f"/seals/applications/{seal_id}/returned?keeperId=8", users["keeper"]),
       "POST /seals/{id}/returned")

# School seal major
r = req("POST", "/seals/applications", users["user"],
        {"sealId": 1, "applicantId": 2, "purpose": "FT school seal", "copies": 1, "takeOut": False, "matterLevel": "重大事项"})
ok(r, "POST /seals/applications [school major]")
sseal_id = extid(r)

if sseal_id:
    req("POST", f"/seals/applications/{sseal_id}/submit", users["user"])
    ok(req("POST", f"/approvals/seal/{sseal_id}", users["office"], {"action": "approve", "opinion": "ok"}),
       "Approve school seal [office]")
    ok(req("POST", f"/approvals/seal/{sseal_id}", users["leader"], {"action": "approve", "opinion": "ok"}),
       "Approve school seal [leader]")

ok(req("GET", "/seals/transfers", users["keeper"]), "GET /seals/transfers")
ok(req("POST", "/seals/transfers", users["keeper"],
       {"sealId": 1, "receiverId": 8, "supervisorId": 5, "materialUrl": "/transfer.pdf", "remark": "FT"}),
   "POST /seals/transfers [create]")

# ========== 7. MEETING ==========
print("\n=== 7. Meeting Management ===")
ok(req("GET", "/meetings/rooms", users["user"]), "GET /meetings/rooms")
ok(req("POST", "/meetings/recommend", users["user"],
       {"expectedCount": 50, "startTime": "2026-11-01T10:00:00", "endTime": "2026-11-01T12:00:00"}),
   "POST /meetings/recommend")

r = req("POST", "/meetings", users["user"],
        {"title": "FT Meeting", "roomId": 1, "startTime": "2026-11-01T10:00:00", "endTime": "2026-11-01T12:00:00",
         "organizerId": 2, "venueType": "室内", "meetingType": "国内管理会议", "budget": 300,
         "participants": [2, 3], "recorderId": 3})
ok(r, "POST /meetings [create]")
meet_id = extid(r)

ok(req("GET", "/meetings", users["user"]), "GET /meetings [list]")

if meet_id:
    ok(req("GET", f"/meetings/{meet_id}/participants", users["user"]), "GET /meetings/{id}/participants")

ok(req("GET", "/meetings/participated", users["user"]), "GET /meetings/participated")

if meet_id:
    ok(req("POST", f"/approvals/meeting/{meet_id}", users["head"], {"action": "approve", "opinion": "ok"}),
       "Approve meeting [head]")
    ok(req("POST", f"/meetings/{meet_id}/minutes", users["head"], {"minutes": "FT minutes", "signInCount": 2}),
       "POST /meetings/{id}/minutes")
    ok(req("POST", f"/meetings/{meet_id}/confirm-minutes", users["user"]),
       "POST /meetings/{id}/confirm-minutes")
    ok(req("POST", f"/meetings/{meet_id}/publish", users["user"]), "POST /meetings/{id}/publish")
    ok(req("POST", f"/meetings/{meet_id}/archive", users["user"]), "POST /meetings/{id}/archive")
    ok(req("POST", f"/meetings/{meet_id}/remind-participant/3", users["user"]),
       "POST /meetings/{id}/remind-participant/{uid}")

# Large activity
r = req("POST", "/meetings", users["user"],
        {"title": "FT Large", "roomId": 4, "startTime": "2026-12-01T10:00:00", "endTime": "2026-12-01T12:00:00",
         "organizerId": 2, "venueType": "室内", "meetingType": "国内管理会议", "budget": 500,
         "participants": [2,3,4,5,6,7,8,1], "recorderId": 3, "expectedCount": 600,
         "riskReportUrl": "/r.pdf", "securityPlanUrl": "/s.pdf", "emergencyPlanUrl": "/e.pdf"})
ok(r, "POST /meetings [large activity]")
lmeet_id = extid(r)

if lmeet_id:
    ok(req("POST", f"/approvals/meeting/{lmeet_id}", users["head"], {"action": "approve", "opinion": "ok"}),
       "Approve large [head]")
    ok(req("POST", f"/approvals/meeting/{lmeet_id}", users["security"], {"action": "approve", "opinion": "security ok"}),
       "Approve large [security]")

# ========== 8. TRAVEL ==========
print("\n=== 8. Travel Management ===")
r = req("POST", "/travels", users["user"],
        {"applicantId": 2, "destination": "Shanghai", "startDate": "2026-11-01", "endDate": "2026-11-03",
         "reason": "FT travel", "staffLevel": "三类", "travelType": "教学科研业务", "transport": "高铁二等座", "budget": 2600})
ok(r, "POST /travels [create]")
trav_id = extid(r)

ok(req("GET", "/travels", users["user"]), "GET /travels [list]")

if trav_id:
    ok(req("POST", f"/approvals/travel/{trav_id}", users["head"], {"action": "approve", "opinion": "ok"}),
       "Approve travel [head]")
    ok(req("POST", f"/approvals/travel/{trav_id}", users["finance"], {"action": "approve", "opinion": "budget ok"}),
       "Approve travel [finance]")
    ok(req("POST", f"/travels/{trav_id}/reimburse", users["user"],
           {"actualExpense": 1200, "receiptUrl": "/ticket.pdf", "overLimitReason": ""}),
       "POST /travels/{id}/reimburse")
    ok(req("POST", f"/approvals/travel/{trav_id}", users["finance"], {"action": "approve", "opinion": "recheck ok"}),
       "Finance recheck travel")

# ========== 9. REPORT ==========
print("\n=== 9. Report Management ===")
r = req("POST", "/reports", users["user"],
        {"title": "FT Report", "type": "请示", "secrecyLevel": "公开", "content": "test", "applicantId": 2})
ok(r, "POST /reports [create]")
rept_id = extid(r)

ok(req("GET", "/reports", users["user"]), "GET /reports [list]")

if rept_id:
    ok(req("POST", f"/reports/{rept_id}/reply", users["leader"], {"reply": "FT reply"}),
       "POST /reports/{id}/reply")

# ========== 10. APPROVALS ==========
print("\n=== 10. Approval Queue ===")
ok(req("GET", "/approvals", users["head"]), "GET /approvals [head]")
ok(req("GET", "/approvals?bizType=document", users["head"]), "GET /approvals?bizType=document")
ok(req("GET", "/approvals", users["finance"]), "GET /approvals [finance]")
ok(req("GET", "/approvals", users["security"]), "GET /approvals [security]")
ok(req("GET", "/approvals", users["office"]), "GET /approvals [office]")

# ========== 11. MAIL ==========
print("\n=== 11. Mail System ===")
r = req("POST", "/mails", users["user"], {"subject": "FT Mail", "content": "test mail", "toUserIds": [3], "ccUserIds": []})
ok(r, "POST /mails [send]")
mail_id = extid(r)

ok(req("GET", "/mails/inbox", users["head"]), "GET /mails/inbox")
ok(req("GET", "/mails/sent", users["user"]), "GET /mails/sent")

if mail_id:
    ok(req("GET", f"/mails/{mail_id}", users["head"]), "GET /mails/{id}")
    ok(req("POST", f"/mails/{mail_id}/read", users["head"]), "POST /mails/{id}/read")
    ok(req("POST", f"/mails/{mail_id}/retry-email", users["user"]), "POST /mails/{id}/retry-email")

# ========== 12. ANNOUNCEMENT ==========
print("\n=== 12. Announcement Management ===")
r = req("POST", "/announcements", users["office"],
        {"title": "FT Announcement", "content": "test ann", "category": "notice", "targetType": "all"})
ok(r, "POST /announcements [create]")
ann_id = extid(r)

ok(req("GET", "/announcements", users["user"]), "GET /announcements [list]")

if ann_id:
    ok(req("GET", f"/announcements/{ann_id}", users["user"]), "GET /announcements/{id}")
    ok(req("PUT", f"/announcements/{ann_id}", users["office"],
           {"title": "FT Ann Updated", "content": "updated", "category": "notice", "targetType": "all"}),
       "PUT /announcements/{id}")
    ok(req("POST", f"/announcements/{ann_id}/publish", users["office"]),
       "POST /announcements/{id}/publish")

ok(req("GET", "/announcements/latest", users["user"]), "GET /announcements/latest")

if ann_id:
    ok(req("POST", f"/announcements/{ann_id}/withdraw", users["office"]),
       "POST /announcements/{id}/withdraw")

ok(req("GET", "/announcements?includeDrafts=true", users["office"]),
   "GET /announcements?includeDrafts")

# ========== 13. DASHBOARD ==========
print("\n=== 13. Dashboard ===")
for u in ["admin", "user", "head", "leader", "office", "finance", "security", "keeper"]:
    ok(req("GET", "/dashboard", users[u]), f"GET /dashboard [{u}]")

# ========== 14. STATISTICS ==========
print("\n=== 14. Statistics ===")
ok(req("GET", "/statistics", users["admin"]), "GET /statistics [admin]")
ok(req("GET", "/statistics/export", users["admin"]), "GET /statistics/export")
ok(req("GET", "/statistics", users["user"]), "GET /statistics [user]")

# ========== 15. WORKFLOW GUIDE ==========
print("\n=== 15. Workflow Guide ===")
if doc_id:
    ok(req("GET", f"/workflow/guide?bizType=document&bizId={doc_id}", users["user"]),
       "GET /workflow/guide [document]")
if seal_id:
    ok(req("GET", f"/workflow/guide?bizType=seal&bizId={seal_id}", users["user"]),
       "GET /workflow/guide [seal]")
if trav_id:
    ok(req("GET", f"/workflow/guide?bizType=travel&bizId={trav_id}", users["user"]),
       "GET /workflow/guide [travel]")
if meet_id:
    ok(req("GET", f"/workflow/guide?bizType=meeting&bizId={meet_id}", users["user"]),
       "GET /workflow/guide [meeting]")

# ========== 16. NOTIFICATIONS ==========
print("\n=== 16. Notifications ===")
for u in ["admin", "user", "head", "leader", "office", "finance", "security", "keeper"]:
    ok(req("GET", "/workflow/notifications", users[u]), f"GET /workflow/notifications [{u}]")

ok(req("GET", "/workflow/notifications?unreadOnly=true", users["user"]),
   "GET /notifications?unreadOnly")

nr = req("GET", "/workflow/notifications", users["user"])
ndata = nr.get("data", [])
nid = ndata[0].get("id") if ndata else None
if nid:
    ok(req("POST", f"/workflow/notifications/{nid}/read", users["user"]),
       "POST /notifications/{id}/read")

# ========== 17. ATTACHMENTS ==========
print("\n=== 17. Attachments ===")
if doc_id:
    r = req("POST", "/workflow/attachments", users["user"],
            {"bizType": "document", "bizId": doc_id, "fileName": "ft.pdf", "fileUrl": "/uploads/ft.pdf", "secrecyLevel": "公开"})
    ok(r, "POST /workflow/attachments [add]")
    att_id = extid(r)

    ok(req("GET", f"/workflow/attachments?bizType=document&bizId={doc_id}", users["user"]),
       "GET /workflow/attachments [list]")

    if att_id:
        ok(req("PUT", f"/workflow/attachments/{att_id}", users["user"],
               {"fileName": "ft2.pdf", "secrecyLevel": "公开"}),
           "PUT /workflow/attachments/{id}")
        ok(req("DELETE", f"/workflow/attachments/{att_id}", users["user"], {"reason": "FT delete"}),
           "DELETE /workflow/attachments/{id}")

    ok(req("GET", f"/workflow/attachments?bizType=document&bizId={doc_id}&includeDeleted=true", users["user"]),
       "GET /attachments?includeDeleted")

# ========== 18. INSTANCES & TASKS ==========
print("\n=== 18. Workflow Instances & Tasks ===")
ok(req("GET", "/workflow/instances", users["user"]), "GET /workflow/instances")
ok(req("GET", "/workflow/tasks", users["user"]), "GET /workflow/tasks [onlyMine]")
ok(req("GET", "/workflow/tasks?onlyMine=false", users["head"]), "GET /workflow/tasks [all]")

# ========== 19. AUDIT LOGS ==========
print("\n=== 19. Audit Logs ===")
ok(req("GET", "/workflow/audit-logs", users["admin"]), "GET /workflow/audit-logs [admin]")
ok(req("GET", "/workflow/audit-logs?bizType=document", users["admin"]), "GET /audit-logs?bizType=document")
fail(req("GET", "/workflow/audit-logs", users["user"]), "GET /audit-logs [user blocked]")

# ========== 20. RBAC ==========
print("\n=== 20. Role-based Access Control ===")
fail(req("GET", "/admin/users", users["user"]), "RBAC: user /admin/users")
fail(req("GET", "/admin/dictionaries/types", users["user"]), "RBAC: user /admin/dict")
fail(req("POST", "/admin/users", users["user"], {"username": "h", "password": "h", "realName": "h", "email": "h@t.com"}),
     "RBAC: user POST /admin/users")
fail(req("POST", "/approvals/document/99999", users["keeper"], {"action": "approve", "opinion": "hack"}),
     "RBAC: keeper approve doc")
fail(req("POST", "/documents", users["finance"],
        {"title": "h", "docType": "通知", "secrecyLevel": "公开", "content": "h", "applicantId": 1}),
     "RBAC: finance create doc for others")
fail(req("POST", "/announcements", users["user"],
        {"title": "h", "content": "h", "category": "notice", "targetType": "all"}),
     "RBAC: user create announcement")

# ========== SUMMARY ==========
pass_count = sum(1 for s, _, _ in results if s == "PASS")
fail_count = sum(1 for s, _, _ in results if s == "FAIL")
total = len(results)
rate = f"{pass_count * 100 / total:.1f}%" if total > 0 else "0%"

print()
print("=" * 50)
print("           TEST REPORT")
print("=" * 50)
print(f"Total:   {total}")
print(f"Passed:  {pass_count}")
print(f"Failed:  {fail_count}")
print(f"Rate:    {rate}")
print("=" * 50)

if fail_count > 0:
    print()
    print("=== FAILED TESTS ===")
    for s, n, d in results:
        if s == "FAIL":
            print(f"  [FAIL] {n} - {d}")

print()
print("=== ALL TESTS BY MODULE ===")
module = ""
for s, n, d in results:
    m = n.split("]")[0].split("[")[-1] if "[" in n else n.split()[0]
    print(f"  [{s:4s}] {n}")
