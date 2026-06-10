#!/bin/bash
# Comprehensive Functional Test Script
# Tests all 82 endpoints across 8 users and 8 roles
# Uses unicode escapes for Chinese characters (Windows bash compatibility)

BASE="http://localhost:8080/api"
PASS=0
FAIL=0
TOTAL=0
RESULTS=()

log() {
  local status="$1" name="$2" detail="$3"
  TOTAL=$((TOTAL+1))
  if [ "$status" = "PASS" ]; then PASS=$((PASS+1))
  elif [ "$status" = "FAIL" ]; then FAIL=$((FAIL+1)); fi
  RESULTS+=("$status|$name|$detail")
}

login() {
  local user="$1"
  local resp=$(curl -s "$BASE/auth/login" -H "Content-Type: application/json" -d "{\"username\":\"$user\",\"password\":\"123456\"}")
  echo "$resp" | grep -q '"success":true' && echo "$resp" | sed 's/.*"token":"\([^"]*\)".*/\1/' || echo "FAIL"
}

get() { curl -s "$BASE$2" -H "Authorization: Bearer $1"; }
post() { curl -s "$BASE$2" -H "Authorization: Bearer $1" -H "Content-Type: application/json" -d "$3"; }
put() { curl -s "$BASE$2" -H "Authorization: Bearer $1" -H "Content-Type: application/json" -X PUT -d "$3"; }
del() { curl -s "$BASE$2" -H "Authorization: Bearer $1" -H "Content-Type: application/json" -X DELETE -d "$3"; }
ok() { echo "$1" | grep -q '"success":true'; }
fail() { echo "$1" | grep -q '"success":false'; }
extid() { echo "$1" | sed 's/.*"id":\([0-9]*\).*/\1/' | head -1; }

# Unicode shortcuts for dictionary values
U_TONGZHI='\\u901a\\u77e5'           # notice
U_GONGKAI='\\u516c\\u5f00'           # public
U_CHANGGUI='\\u5e38\\u89c4\\u4e8b\\u9879' # routine
U_SHINEI='\\u5ba4\\u5185'            # indoor
U_GUONEI='\\u56fd\\u5185\\u7ba1\\u7406\\u4f1a\\u8bae' # domestic meeting
U_SANLEI='\\u4e09\\u7c7b'            # class-3
U_JIAOXUE='\\u6559\\u5b66\\u79d1\\u7814\\u4e1a\\u52a1' # teaching/research
U_GAOTIE='\\u9ad8\\u94c1\\u4e8c\\u7b49\\u5ea7'  # high-speed rail 2nd class
U_QINGSHI='\\u8bf7\\u793a'            # petition
U_ZHONGDA='\\u91cd\\u5927\\u4e8b\\u9879' # major

echo "========================================="
echo "  OMS Comprehensive Functional Test"
echo "  $(date '+%Y-%m-%d %H:%M:%S')"
echo "========================================="

# ========== 1. AUTH ==========
echo ""; echo "=== 1. Authentication ==="

for u in admin user head leader office finance security keeper; do
  T=$(login "$u")
  [ "$T" != "FAIL" ] && log "PASS" "Login [$u]" "token ok" || log "FAIL" "Login [$u]" "failed"
done

R=$(curl -s "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"admin","password":"wrong"}')
fail "$R" && log "PASS" "Login [wrong pwd]" "rejected" || log "FAIL" "Login [wrong pwd]" "not rejected"

R=$(curl -s "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"nobody","password":"123456"}')
fail "$R" && log "PASS" "Login [bad user]" "rejected" || log "FAIL" "Login [bad user]" "not rejected"

R=$(get "invalidtoken" "/documents")
fail "$R" && log "PASS" "GET /documents [bad token]" "rejected" || log "FAIL" "GET /documents [bad token]" "not rejected"

ADMIN=$(login "admin"); USER=$(login "user"); HEAD=$(login "head")
LEADER=$(login "leader"); OFFICE=$(login "office"); FINANCE=$(login "finance")
SECURITY=$(login "security"); KEEPER=$(login "keeper")

R=$(post "$ADMIN" "/auth/logout" '{}')
ok "$R" && log "PASS" "POST /auth/logout" "ok" || log "FAIL" "POST /auth/logout" "failed"
ADMIN=$(login "admin")

R=$(get "$USER" "/auth/user-options")
ok "$R" && log "PASS" "GET /auth/user-options" "ok" || log "FAIL" "GET /auth/user-options" "failed"

R=$(get "$USER" "/auth/dept-options")
ok "$R" && log "PASS" "GET /auth/dept-options" "ok" || log "FAIL" "GET /auth/dept-options" "failed"

R=$(get "$ADMIN" "/auth/users")
ok "$R" && log "PASS" "GET /auth/users [admin]" "ok" || log "FAIL" "GET /auth/users [admin]" "failed"

R=$(get "$USER" "/auth/users")
fail "$R" && log "PASS" "GET /auth/users [user blocked]" "rejected" || log "FAIL" "GET /auth/users [user blocked]" "not rejected"

# ========== 2. DICTIONARY ==========
echo ""; echo "=== 2. Dictionary Management ==="

R=$(get "$USER" "/dictionaries")
ok "$R" && log "PASS" "GET /dictionaries [catalog]" "ok" || log "FAIL" "GET /dictionaries [catalog]" "failed"

R=$(get "$USER" "/dictionaries/version")
ok "$R" && log "PASS" "GET /dictionaries/version" "ok" || log "FAIL" "GET /dictionaries/version" "failed"

R=$(get "$ADMIN" "/admin/dictionaries/types")
ok "$R" && log "PASS" "GET /admin/dictionaries/types" "ok" || log "FAIL" "GET /admin/dictionaries/types" "failed"

R=$(post "$ADMIN" "/admin/dictionaries/types" '{"dictType":"test_ft","dictName":"FT Type","enabled":true}')
ok "$R" && log "PASS" "POST /admin/dictionaries/types [create]" "ok" || log "FAIL" "POST /admin/dictionaries/types [create]" "failed"

R=$(put "$ADMIN" "/admin/dictionaries/types/test_ft" '{"dictType":"test_ft","dictName":"FT Type Updated","enabled":true}')
ok "$R" && log "PASS" "PUT /admin/dictionaries/types/test_ft" "ok" || log "FAIL" "PUT /admin/dictionaries/types/test_ft" "failed"

R=$(get "$ADMIN" "/admin/dictionaries/types/test_ft/items")
ok "$R" && log "PASS" "GET /admin/dictionaries/types/test_ft/items" "ok" || log "FAIL" "GET /admin/dictionaries/types/test_ft/items" "failed"

R=$(post "$ADMIN" "/admin/dictionaries/types/test_ft/items" '{"dictCode":"t1","dictLabel":"FT Item","sortOrder":1,"enabled":true}')
ok "$R" && log "PASS" "POST /admin/dict items [create]" "ok" || log "FAIL" "POST /admin/dict items [create]" "failed"

R=$(put "$ADMIN" "/admin/dictionaries/types/test_ft/items/t1" '{"dictCode":"t1","dictLabel":"FT Item Updated","sortOrder":2,"enabled":false}')
ok "$R" && log "PASS" "PUT /admin/dict items/t1" "ok" || log "FAIL" "PUT /admin/dict items/t1" "failed"

R=$(post "$USER" "/admin/dictionaries/types" '{"dictType":"hack","dictName":"hack"}')
fail "$R" && log "PASS" "Dict create [user blocked]" "rejected" || log "FAIL" "Dict create [user blocked]" "not rejected"

# ========== 3. USER MANAGEMENT ==========
echo ""; echo "=== 3. User Management ==="

R=$(get "$ADMIN" "/admin/users")
ok "$R" && log "PASS" "GET /admin/users" "ok" || log "FAIL" "GET /admin/users" "failed"

R=$(post "$ADMIN" "/admin/users" '{"username":"ftuser","password":"123456","realName":"FT User","deptId":4,"email":"ft@test.com","roleKeys":"office_user"}')
ok "$R" && log "PASS" "POST /admin/users [create]" "ok" || log "FAIL" "POST /admin/users [create]" "failed"
TUID=$(extid "$R")

if [ -n "$TUID" ]; then
  R=$(get "$ADMIN" "/admin/users/$TUID")
  ok "$R" && log "PASS" "GET /admin/users/{id}" "ok" || log "FAIL" "GET /admin/users/{id}" "failed"

  R=$(put "$ADMIN" "/admin/users/$TUID" '{"realName":"FT User Updated","email":"ft2@test.com"}')
  ok "$R" && log "PASS" "PUT /admin/users/{id}" "ok" || log "FAIL" "PUT /admin/users/{id}" "failed"

  R=$(del "$ADMIN" "/admin/users/$TUID" '{}')
  ok "$R" && log "PASS" "DELETE /admin/users/{id}" "ok" || log "FAIL" "DELETE /admin/users/{id}" "failed"
fi

R=$(get "$ADMIN" "/admin/depts")
ok "$R" && log "PASS" "GET /admin/depts" "ok" || log "FAIL" "GET /admin/depts" "failed"

R=$(post "$ADMIN" "/admin/depts" '{"deptName":"FT Dept","parentId":0}')
ok "$R" && log "PASS" "POST /admin/depts [create]" "ok" || log "FAIL" "POST /admin/depts [create]" "failed"
TDID=$(extid "$R")

if [ -n "$TDID" ]; then
  R=$(put "$ADMIN" "/admin/depts/$TDID" '{"deptName":"FT Dept Updated"}')
  ok "$R" && log "PASS" "PUT /admin/depts/{id}" "ok" || log "FAIL" "PUT /admin/depts/{id}" "failed"

  R=$(del "$ADMIN" "/admin/depts/$TDID" '{}')
  ok "$R" && log "PASS" "DELETE /admin/depts/{id}" "ok" || log "FAIL" "DELETE /admin/depts/{id}" "failed"
fi

R=$(get "$ADMIN" "/admin/roles")
ok "$R" && log "PASS" "GET /admin/roles" "ok" || log "FAIL" "GET /admin/roles" "failed"

R=$(get "$USER" "/admin/users")
fail "$R" && log "PASS" "RBAC: user GET /admin/users" "rejected" || log "FAIL" "RBAC: user GET /admin/users" "not rejected"

# ========== 4. ORG TREE ==========
echo ""; echo "=== 4. Organization Tree ==="

R=$(get "$ADMIN" "/org/tree")
ok "$R" && log "PASS" "GET /org/tree [admin]" "ok" || log "FAIL" "GET /org/tree [admin]" "failed"

R=$(get "$USER" "/org/tree")
ok "$R" && log "PASS" "GET /org/tree [user]" "ok" || log "FAIL" "GET /org/tree [user]" "failed"

# ========== 5. DOCUMENT ==========
echo ""; echo "=== 5. Document Lifecycle ==="

R=$(post "$USER" "/documents" "{\"title\":\"FT Doc\",\"docType\":\"$U_TONGZHI\",\"secrecyLevel\":\"$U_GONGKAI\",\"content\":\"test\",\"applicantId\":2}")
ok "$R" && log "PASS" "POST /documents [create]" "ok" || log "FAIL" "POST /documents [create]" "failed"
DOC_ID=$(extid "$R")

R=$(get "$USER" "/documents")
ok "$R" && log "PASS" "GET /documents [list]" "ok" || log "FAIL" "GET /documents [list]" "failed"

if [ -n "$DOC_ID" ]; then
  R=$(post "$USER" "/documents/$DOC_ID/ai-review" '{}')
  ok "$R" && log "PASS" "POST /documents/{id}/ai-review" "ok" || log "FAIL" "POST /documents/{id}/ai-review" "failed"
fi

R=$(post "$USER" "/documents/ai-draft" "{\"docType\":\"$U_TONGZHI\",\"topic\":\"FT draft\",\"keyPoints\":\"p1,p2\"}")
ok "$R" && log "PASS" "POST /documents/ai-draft" "ok" || log "FAIL" "POST /documents/ai-draft" "failed"

if [ -n "$DOC_ID" ]; then
  R=$(post "$USER" "/documents/$DOC_ID/submit" '{}')
  ok "$R" && log "PASS" "POST /documents/{id}/submit" "ok" || log "FAIL" "POST /documents/{id}/submit" "failed"

  R=$(post "$HEAD" "/approvals/document/$DOC_ID" '{"action":"approve","opinion":"dept ok"}')
  ok "$R" && log "PASS" "Approve doc [head]" "ok" || log "FAIL" "Approve doc [head]" "failed"

  R=$(post "$OFFICE" "/approvals/document/$DOC_ID" '{"action":"approve","opinion":"office ok"}')
  ok "$R" && log "PASS" "Approve doc [office]" "ok" || log "FAIL" "Approve doc [office]" "failed"

  R=$(post "$LEADER" "/approvals/document/$DOC_ID" '{"action":"approve","opinion":"signed"}')
  ok "$R" && log "PASS" "Approve doc [leader]" "ok" || log "FAIL" "Approve doc [leader]" "failed"

  R=$(post "$OFFICE" "/documents/$DOC_ID/distributions" '{"receiverId":2,"receiverDeptId":4}')
  ok "$R" && log "PASS" "POST /documents/{id}/distributions" "ok" || log "FAIL" "POST /documents/{id}/distributions" "failed"
  DIST_ID=$(extid "$R")

  R=$(get "$OFFICE" "/documents/$DOC_ID/distributions")
  ok "$R" && log "PASS" "GET /documents/{id}/distributions" "ok" || log "FAIL" "GET /documents/{id}/distributions" "failed"

  if [ -n "$DIST_ID" ]; then
    R=$(post "$OFFICE" "/documents/$DOC_ID/distributions/$DIST_ID/remind" '{}')
    ok "$R" && log "PASS" "POST distributions/{id}/remind" "ok" || log "FAIL" "POST distributions/{id}/remind" "failed"

    R=$(post "$USER" "/documents/$DOC_ID/distributions/$DIST_ID/receipt" '{}')
    ok "$R" && log "PASS" "POST distributions/{id}/receipt" "ok" || log "FAIL" "POST distributions/{id}/receipt" "failed"
  fi

  R=$(post "$OFFICE" "/documents/$DOC_ID/archive" '{}')
  ok "$R" && log "PASS" "POST /documents/{id}/archive" "ok" || log "FAIL" "POST /documents/{id}/archive" "failed"
fi

# Document rejection flow
R=$(post "$USER" "/documents" "{\"title\":\"FT Reject\",\"docType\":\"$U_TONGZHI\",\"secrecyLevel\":\"$U_GONGKAI\",\"content\":\"reject test\",\"applicantId\":2}")
REJ_DOC=$(extid "$R")
if [ -n "$REJ_DOC" ]; then
  post "$USER" "/documents/$REJ_DOC/submit" '{}'
  R=$(post "$HEAD" "/approvals/document/$REJ_DOC" '{"action":"reject","opinion":"revise please"}')
  ok "$R" && log "PASS" "Reject doc [head]" "ok" || log "FAIL" "Reject doc [head]" "failed"
fi

# ========== 6. SEAL ==========
echo ""; echo "=== 6. Seal Management ==="

R=$(get "$USER" "/seals")
ok "$R" && log "PASS" "GET /seals" "ok" || log "FAIL" "GET /seals" "failed"

R=$(get "$USER" "/seals/applications")
ok "$R" && log "PASS" "GET /seals/applications [list]" "ok" || log "FAIL" "GET /seals/applications [list]" "failed"

# Dept seal, routine
R=$(post "$USER" "/seals/applications" "{\"sealId\":2,\"applicantId\":2,\"purpose\":\"FT seal\",\"copies\":1,\"takeOut\":false,\"matterLevel\":\"$U_CHANGGUI\"}")
ok "$R" && log "PASS" "POST /seals/applications [create]" "ok" || log "FAIL" "POST /seals/applications [create]" "failed"
SEAL_ID=$(extid "$R")

if [ -n "$SEAL_ID" ]; then
  R=$(post "$USER" "/seals/applications/$SEAL_ID/submit" '{}')
  ok "$R" && log "PASS" "POST /seals/{id}/submit" "ok" || log "FAIL" "POST /seals/{id}/submit" "failed"

  R=$(post "$HEAD" "/approvals/seal/$SEAL_ID" '{"action":"approve","opinion":"ok"}')
  ok "$R" && log "PASS" "Approve seal [head]" "ok" || log "FAIL" "Approve seal [head]" "failed"

  R=$(post "$OFFICE" "/approvals/seal/$SEAL_ID" '{"action":"approve","opinion":"ok"}')
  ok "$R" && log "PASS" "Approve seal [office]" "ok" || log "FAIL" "Approve seal [office]" "failed"

  R=$(post "$KEEPER" "/seals/applications/$SEAL_ID/used?keeperId=8" '{}')
  ok "$R" && log "PASS" "POST /seals/{id}/used" "ok" || log "FAIL" "POST /seals/{id}/used" "failed"

  R=$(post "$KEEPER" "/seals/applications/$SEAL_ID/returned?keeperId=8" '{}')
  ok "$R" && log "PASS" "POST /seals/{id}/returned" "ok" || log "FAIL" "POST /seals/{id}/returned" "failed"
fi

# School seal, major matter
R=$(post "$USER" "/seals/applications" "{\"sealId\":1,\"applicantId\":2,\"purpose\":\"FT school seal\",\"copies\":1,\"takeOut\":false,\"matterLevel\":\"$U_ZHONGDA\"}")
ok "$R" && log "PASS" "POST /seals/applications [school major]" "ok" || log "FAIL" "POST /seals/applications [school major]" "failed"
SSEAL_ID=$(extid "$R")

if [ -n "$SSEAL_ID" ]; then
  post "$USER" "/seals/applications/$SSEAL_ID/submit" '{}'
  R=$(post "$OFFICE" "/approvals/seal/$SSEAL_ID" '{"action":"approve","opinion":"ok"}')
  ok "$R" && log "PASS" "Approve school seal [office]" "ok" || log "FAIL" "Approve school seal [office]" "failed"

  R=$(post "$LEADER" "/approvals/seal/$SSEAL_ID" '{"action":"approve","opinion":"ok"}')
  ok "$R" && log "PASS" "Approve school seal [leader]" "ok" || log "FAIL" "Approve school seal [leader]" "failed"
fi

# Seal transfer
R=$(get "$KEEPER" "/seals/transfers")
ok "$R" && log "PASS" "GET /seals/transfers" "ok" || log "FAIL" "GET /seals/transfers" "failed"

R=$(post "$KEEPER" "/seals/transfers" '{"sealId":1,"receiverId":8,"supervisorId":5,"materialUrl":"/transfer.pdf","remark":"FT transfer"}')
ok "$R" && log "PASS" "POST /seals/transfers [create]" "ok" || log "FAIL" "POST /seals/transfers [create]" "failed"

# ========== 7. MEETING ==========
echo ""; echo "=== 7. Meeting Management ==="

R=$(get "$USER" "/meetings/rooms")
ok "$R" && log "PASS" "GET /meetings/rooms" "ok" || log "FAIL" "GET /meetings/rooms" "failed"

R=$(post "$USER" "/meetings/recommend" '{"expectedCount":50,"startTime":"2026-11-01T10:00:00","endTime":"2026-11-01T12:00:00"}')
ok "$R" && log "PASS" "POST /meetings/recommend" "ok" || log "FAIL" "POST /meetings/recommend" "failed"

R=$(post "$USER" "/meetings" "{\"title\":\"FT Meeting\",\"roomId\":1,\"startTime\":\"2026-11-01T10:00:00\",\"endTime\":\"2026-11-01T12:00:00\",\"organizerId\":2,\"venueType\":\"$U_SHINEI\",\"meetingType\":\"$U_GUONEI\",\"budget\":300,\"participants\":[2,3],\"recorderId\":3}")
ok "$R" && log "PASS" "POST /meetings [create]" "ok" || log "FAIL" "POST /meetings [create]" "failed"
MEET_ID=$(extid "$R")

R=$(get "$USER" "/meetings")
ok "$R" && log "PASS" "GET /meetings [list]" "ok" || log "FAIL" "GET /meetings [list]" "failed"

if [ -n "$MEET_ID" ]; then
  R=$(get "$USER" "/meetings/$MEET_ID/participants")
  ok "$R" && log "PASS" "GET /meetings/{id}/participants" "ok" || log "FAIL" "GET /meetings/{id}/participants" "failed"
fi

R=$(get "$USER" "/meetings/participated")
ok "$R" && log "PASS" "GET /meetings/participated" "ok" || log "FAIL" "GET /meetings/participated" "failed"

if [ -n "$MEET_ID" ]; then
  R=$(post "$HEAD" "/approvals/meeting/$MEET_ID" '{"action":"approve","opinion":"ok"}')
  ok "$R" && log "PASS" "Approve meeting [head]" "ok" || log "FAIL" "Approve meeting [head]" "failed"

  R=$(post "$HEAD" "/meetings/$MEET_ID/minutes" '{"minutes":"FT minutes","signInCount":2}')
  ok "$R" && log "PASS" "POST /meetings/{id}/minutes" "ok" || log "FAIL" "POST /meetings/{id}/minutes" "failed"

  R=$(post "$USER" "/meetings/$MEET_ID/confirm-minutes" '{}')
  ok "$R" && log "PASS" "POST /meetings/{id}/confirm-minutes" "ok" || log "FAIL" "POST /meetings/{id}/confirm-minutes" "failed"

  R=$(post "$USER" "/meetings/$MEET_ID/publish" '{}')
  ok "$R" && log "PASS" "POST /meetings/{id}/publish" "ok" || log "FAIL" "POST /meetings/{id}/publish" "failed"

  R=$(post "$USER" "/meetings/$MEET_ID/archive" '{}')
  ok "$R" && log "PASS" "POST /meetings/{id}/archive" "ok" || log "FAIL" "POST /meetings/{id}/archive" "failed"

  R=$(post "$USER" "/meetings/$MEET_ID/remind-participant/3" '{}')
  ok "$R" && log "PASS" "POST /meetings/{id}/remind-participant/{uid}" "ok" || log "FAIL" "POST /meetings/{id}/remind-participant/{uid}" "failed"
fi

# Large activity (>500 participants)
R=$(post "$USER" "/meetings" "{\"title\":\"FT Large Activity\",\"roomId\":4,\"startTime\":\"2026-12-01T10:00:00\",\"endTime\":\"2026-12-01T12:00:00\",\"organizerId\":2,\"venueType\":\"$U_SHINEI\",\"meetingType\":\"$U_GUONEI\",\"budget\":500,\"participants\":[2,3,4,5,6,7,8,1],\"recorderId\":3,\"expectedCount\":600,\"riskReportUrl\":\"/r.pdf\",\"securityPlanUrl\":\"/s.pdf\",\"emergencyPlanUrl\":\"/e.pdf\"}")
ok "$R" && log "PASS" "POST /meetings [large activity]" "ok" || log "FAIL" "POST /meetings [large activity]" "failed"
LMEET_ID=$(extid "$R")

if [ -n "$LMEET_ID" ]; then
  R=$(post "$HEAD" "/approvals/meeting/$LMEET_ID" '{"action":"approve","opinion":"ok"}')
  ok "$R" && log "PASS" "Approve large [head]" "ok" || log "FAIL" "Approve large [head]" "failed"

  R=$(post "$SECURITY" "/approvals/meeting/$LMEET_ID" '{"action":"approve","opinion":"security ok"}')
  ok "$R" && log "PASS" "Approve large [security]" "ok" || log "FAIL" "Approve large [security]" "failed"
fi

# ========== 8. TRAVEL ==========
echo ""; echo "=== 8. Travel Management ==="

R=$(post "$USER" "/travels" "{\"applicantId\":2,\"destination\":\"Shanghai\",\"startDate\":\"2026-11-01\",\"endDate\":\"2026-11-03\",\"reason\":\"FT travel\",\"staffLevel\":\"$U_SANLEI\",\"travelType\":\"$U_JIAOXUE\",\"transport\":\"$U_GAOTIE\",\"budget\":2600}")
ok "$R" && log "PASS" "POST /travels [create]" "ok" || log "FAIL" "POST /travels [create]" "failed"
TRAV_ID=$(extid "$R")

R=$(get "$USER" "/travels")
ok "$R" && log "PASS" "GET /travels [list]" "ok" || log "FAIL" "GET /travels [list]" "failed"

if [ -n "$TRAV_ID" ]; then
  R=$(post "$HEAD" "/approvals/travel/$TRAV_ID" '{"action":"approve","opinion":"ok"}')
  ok "$R" && log "PASS" "Approve travel [head]" "ok" || log "FAIL" "Approve travel [head]" "failed"

  R=$(post "$FINANCE" "/approvals/travel/$TRAV_ID" '{"action":"approve","opinion":"budget ok"}')
  ok "$R" && log "PASS" "Approve travel [finance]" "ok" || log "FAIL" "Approve travel [finance]" "failed"

  R=$(post "$USER" "/travels/$TRAV_ID/reimburse" '{"actualExpense":1200,"receiptUrl":"/ticket.pdf","overLimitReason":""}')
  ok "$R" && log "PASS" "POST /travels/{id}/reimburse" "ok" || log "FAIL" "POST /travels/{id}/reimburse" "failed"

  R=$(post "$FINANCE" "/approvals/travel/$TRAV_ID" '{"action":"approve","opinion":"recheck ok"}')
  ok "$R" && log "PASS" "Finance recheck travel" "ok" || log "FAIL" "Finance recheck travel" "failed"
fi

# ========== 9. REPORT ==========
echo ""; echo "=== 9. Report Management ==="

R=$(post "$USER" "/reports" "{\"title\":\"FT Report\",\"type\":\"$U_QINGSHI\",\"secrecyLevel\":\"$U_GONGKAI\",\"content\":\"test\",\"applicantId\":2}")
ok "$R" && log "PASS" "POST /reports [create]" "ok" || log "FAIL" "POST /reports [create]" "failed"
REPT_ID=$(extid "$R")

R=$(get "$USER" "/reports")
ok "$R" && log "PASS" "GET /reports [list]" "ok" || log "FAIL" "GET /reports [list]" "failed"

if [ -n "$REPT_ID" ]; then
  R=$(post "$LEADER" "/reports/$REPT_ID/reply" '{"reply":"FT reply content"}')
  ok "$R" && log "PASS" "POST /reports/{id}/reply" "ok" || log "FAIL" "POST /reports/{id}/reply" "failed"
fi

# ========== 10. APPROVALS ==========
echo ""; echo "=== 10. Approval Queue ==="

R=$(get "$HEAD" "/approvals")
ok "$R" && log "PASS" "GET /approvals [head]" "ok" || log "FAIL" "GET /approvals [head]" "failed"

R=$(get "$HEAD" "/approvals?bizType=document")
ok "$R" && log "PASS" "GET /approvals?bizType=document" "ok" || log "FAIL" "GET /approvals?bizType=document" "failed"

R=$(get "$FINANCE" "/approvals")
ok "$R" && log "PASS" "GET /approvals [finance]" "ok" || log "FAIL" "GET /approvals [finance]" "failed"

R=$(get "$SECURITY" "/approvals")
ok "$R" && log "PASS" "GET /approvals [security]" "ok" || log "FAIL" "GET /approvals [security]" "failed"

R=$(get "$OFFICE" "/approvals")
ok "$R" && log "PASS" "GET /approvals [office]" "ok" || log "FAIL" "GET /approvals [office]" "failed"

# ========== 11. MAIL ==========
echo ""; echo "=== 11. Mail System ==="

R=$(post "$USER" "/mails" '{"subject":"FT Mail","content":"test mail","toUserIds":[3],"ccUserIds":[]}')
ok "$R" && log "PASS" "POST /mails [send]" "ok" || log "FAIL" "POST /mails [send]" "failed"
MAIL_ID=$(extid "$R")

R=$(get "$HEAD" "/mails/inbox")
ok "$R" && log "PASS" "GET /mails/inbox" "ok" || log "FAIL" "GET /mails/inbox" "failed"

R=$(get "$USER" "/mails/sent")
ok "$R" && log "PASS" "GET /mails/sent" "ok" || log "FAIL" "GET /mails/sent" "failed"

if [ -n "$MAIL_ID" ]; then
  R=$(get "$HEAD" "/mails/$MAIL_ID")
  ok "$R" && log "PASS" "GET /mails/{id}" "ok" || log "FAIL" "GET /mails/{id}" "failed"

  R=$(post "$HEAD" "/mails/$MAIL_ID/read" '{}')
  ok "$R" && log "PASS" "POST /mails/{id}/read" "ok" || log "FAIL" "POST /mails/{id}/read" "failed"

  R=$(post "$USER" "/mails/$MAIL_ID/retry-email" '{}')
  ok "$R" && log "PASS" "POST /mails/{id}/retry-email" "ok" || log "FAIL" "POST /mails/{id}/retry-email" "failed"
fi

# ========== 12. ANNOUNCEMENT ==========
echo ""; echo "=== 12. Announcement Management ==="

R=$(post "$OFFICE" "/announcements" '{"title":"FT Announcement","content":"test ann","category":"notice","targetType":"all"}')
ok "$R" && log "PASS" "POST /announcements [create]" "ok" || log "FAIL" "POST /announcements [create]" "failed"
ANN_ID=$(extid "$R")

R=$(get "$USER" "/announcements")
ok "$R" && log "PASS" "GET /announcements [list]" "ok" || log "FAIL" "GET /announcements [list]" "failed"

if [ -n "$ANN_ID" ]; then
  R=$(get "$USER" "/announcements/$ANN_ID")
  ok "$R" && log "PASS" "GET /announcements/{id}" "ok" || log "FAIL" "GET /announcements/{id}" "failed"

  R=$(put "$OFFICE" "/announcements/$ANN_ID" '{"title":"FT Ann Updated","content":"updated","category":"notice","targetType":"all"}')
  ok "$R" && log "PASS" "PUT /announcements/{id}" "ok" || log "FAIL" "PUT /announcements/{id}" "failed"

  R=$(post "$OFFICE" "/announcements/$ANN_ID/publish" '{}')
  ok "$R" && log "PASS" "POST /announcements/{id}/publish" "ok" || log "FAIL" "POST /announcements/{id}/publish" "failed"
fi

R=$(get "$USER" "/announcements/latest")
ok "$R" && log "PASS" "GET /announcements/latest" "ok" || log "FAIL" "GET /announcements/latest" "failed"

if [ -n "$ANN_ID" ]; then
  R=$(post "$OFFICE" "/announcements/$ANN_ID/withdraw" '{}')
  ok "$R" && log "PASS" "POST /announcements/{id}/withdraw" "ok" || log "FAIL" "POST /announcements/{id}/withdraw" "failed"
fi

R=$(get "$OFFICE" "/announcements?includeDrafts=true")
ok "$R" && log "PASS" "GET /announcements?includeDrafts" "ok" || log "FAIL" "GET /announcements?includeDrafts" "failed"

# ========== 13. DASHBOARD ==========
echo ""; echo "=== 13. Dashboard ==="

for u in admin user head leader office finance security keeper; do
  T=$(login "$u")
  R=$(get "$T" "/dashboard")
  ok "$R" && log "PASS" "GET /dashboard [$u]" "ok" || log "FAIL" "GET /dashboard [$u]" "failed"
done

# ========== 14. STATISTICS ==========
echo ""; echo "=== 14. Statistics ==="

R=$(get "$ADMIN" "/statistics")
ok "$R" && log "PASS" "GET /statistics [admin]" "ok" || log "FAIL" "GET /statistics [admin]" "failed"

R=$(get "$ADMIN" "/statistics/export")
ok "$R" && log "PASS" "GET /statistics/export" "ok" || log "FAIL" "GET /statistics/export" "failed"

R=$(get "$USER" "/statistics")
ok "$R" && log "PASS" "GET /statistics [user]" "ok" || log "FAIL" "GET /statistics [user]" "failed"

# ========== 15. WORKFLOW GUIDE ==========
echo ""; echo "=== 15. Workflow Guide ==="

if [ -n "$DOC_ID" ]; then
  R=$(get "$USER" "/workflow/guide?bizType=document&bizId=$DOC_ID")
  ok "$R" && log "PASS" "GET /workflow/guide [document]" "ok" || log "FAIL" "GET /workflow/guide [document]" "failed"
fi
if [ -n "$SEAL_ID" ]; then
  R=$(get "$USER" "/workflow/guide?bizType=seal&bizId=$SEAL_ID")
  ok "$R" && log "PASS" "GET /workflow/guide [seal]" "ok" || log "FAIL" "GET /workflow/guide [seal]" "failed"
fi
if [ -n "$TRAV_ID" ]; then
  R=$(get "$USER" "/workflow/guide?bizType=travel&bizId=$TRAV_ID")
  ok "$R" && log "PASS" "GET /workflow/guide [travel]" "ok" || log "FAIL" "GET /workflow/guide [travel]" "failed"
fi
if [ -n "$MEET_ID" ]; then
  R=$(get "$USER" "/workflow/guide?bizType=meeting&bizId=$MEET_ID")
  ok "$R" && log "PASS" "GET /workflow/guide [meeting]" "ok" || log "FAIL" "GET /workflow/guide [meeting]" "failed"
fi

# ========== 16. WORKFLOW NOTIFICATIONS ==========
echo ""; echo "=== 16. Notifications ==="

for u in admin user head leader office finance security keeper; do
  T=$(login "$u")
  R=$(get "$T" "/workflow/notifications")
  ok "$R" && log "PASS" "GET /workflow/notifications [$u]" "ok" || log "FAIL" "GET /workflow/notifications [$u]" "failed"
done

R=$(get "$USER" "/workflow/notifications?unreadOnly=true")
ok "$R" && log "PASS" "GET /notifications?unreadOnly" "ok" || log "FAIL" "GET /notifications?unreadOnly" "failed"

NR=$(get "$USER" "/workflow/notifications")
NID=$(extid "$NR")
if [ -n "$NID" ]; then
  R=$(post "$USER" "/workflow/notifications/$NID/read" '{}')
  ok "$R" && log "PASS" "POST /notifications/{id}/read" "ok" || log "FAIL" "POST /notifications/{id}/read" "failed"
fi

# ========== 17. WORKFLOW ATTACHMENTS ==========
echo ""; echo "=== 17. Attachments ==="

if [ -n "$DOC_ID" ]; then
  R=$(post "$USER" "/workflow/attachments" "{\"bizType\":\"document\",\"bizId\":$DOC_ID,\"fileName\":\"ft.pdf\",\"fileUrl\":\"/uploads/ft.pdf\",\"secrecyLevel\":\"$U_GONGKAI\"}")
  ok "$R" && log "PASS" "POST /workflow/attachments [add]" "ok" || log "FAIL" "POST /workflow/attachments [add]" "failed"
  ATT_ID=$(extid "$R")

  R=$(get "$USER" "/workflow/attachments?bizType=document&bizId=$DOC_ID")
  ok "$R" && log "PASS" "GET /workflow/attachments [list]" "ok" || log "FAIL" "GET /workflow/attachments [list]" "failed"

  if [ -n "$ATT_ID" ]; then
    R=$(put "$USER" "/workflow/attachments/$ATT_ID" "{\"fileName\":\"ft2.pdf\",\"secrecyLevel\":\"$U_GONGKAI\"}")
    ok "$R" && log "PASS" "PUT /workflow/attachments/{id}" "ok" || log "FAIL" "PUT /workflow/attachments/{id}" "failed"

    R=$(curl -s "$BASE/workflow/attachments/$ATT_ID" -H "Authorization: Bearer $USER" -H "Content-Type: application/json" -X DELETE -d '{"reason":"FT delete"}')
    ok "$R" && log "PASS" "DELETE /workflow/attachments/{id}" "ok" || log "FAIL" "DELETE /workflow/attachments/{id}" "failed"
  fi

  R=$(get "$USER" "/workflow/attachments?bizType=document&bizId=$DOC_ID&includeDeleted=true")
  ok "$R" && log "PASS" "GET /attachments?includeDeleted" "ok" || log "FAIL" "GET /attachments?includeDeleted" "failed"
fi

# ========== 18. WORKFLOW INSTANCES & TASKS ==========
echo ""; echo "=== 18. Workflow Instances & Tasks ==="

R=$(get "$USER" "/workflow/instances")
ok "$R" && log "PASS" "GET /workflow/instances" "ok" || log "FAIL" "GET /workflow/instances" "failed"

R=$(get "$USER" "/workflow/tasks")
ok "$R" && log "PASS" "GET /workflow/tasks [onlyMine]" "ok" || log "FAIL" "GET /workflow/tasks [onlyMine]" "failed"

R=$(get "$HEAD" "/workflow/tasks?onlyMine=false")
ok "$R" && log "PASS" "GET /workflow/tasks [all]" "ok" || log "FAIL" "GET /workflow/tasks [all]" "failed"

# ========== 19. AUDIT LOGS ==========
echo ""; echo "=== 19. Audit Logs ==="

R=$(get "$ADMIN" "/workflow/audit-logs")
ok "$R" && log "PASS" "GET /workflow/audit-logs [admin]" "ok" || log "FAIL" "GET /workflow/audit-logs [admin]" "failed"

R=$(get "$ADMIN" "/workflow/audit-logs?bizType=document")
ok "$R" && log "PASS" "GET /audit-logs?bizType=document" "ok" || log "FAIL" "GET /audit-logs?bizType=document" "failed"

R=$(get "$USER" "/workflow/audit-logs")
fail "$R" && log "PASS" "GET /audit-logs [user blocked]" "rejected" || log "FAIL" "GET /audit-logs [user blocked]" "not rejected"

# ========== 20. RBAC ==========
echo ""; echo "=== 20. Role-based Access Control ==="

R=$(get "$USER" "/admin/users")
fail "$R" && log "PASS" "RBAC: user /admin/users" "rejected" || log "FAIL" "RBAC: user /admin/users" "not rejected"

R=$(get "$USER" "/admin/dictionaries/types")
fail "$R" && log "PASS" "RBAC: user /admin/dict" "rejected" || log "FAIL" "RBAC: user /admin/dict" "not rejected"

R=$(post "$USER" "/admin/users" '{"username":"h","password":"h","realName":"h","email":"h@t.com"}')
fail "$R" && log "PASS" "RBAC: user POST /admin/users" "rejected" || log "FAIL" "RBAC: user POST /admin/users" "not rejected"

R=$(post "$KEEPER" "/approvals/document/99999" '{"action":"approve","opinion":"hack"}')
fail "$R" && log "PASS" "RBAC: keeper approve doc" "rejected" || log "FAIL" "RBAC: keeper approve doc" "not rejected"

R=$(post "$FINANCE" "/documents" "{\"title\":\"h\",\"docType\":\"$U_TONGZHI\",\"secrecyLevel\":\"$U_GONGKAI\",\"content\":\"h\",\"applicantId\":1}")
fail "$R" && log "PASS" "RBAC: finance create doc for others" "rejected" || log "FAIL" "RBAC: finance create doc for others" "not rejected"

R=$(post "$USER" "/announcements" '{"title":"h","content":"h","category":"notice","targetType":"all"}')
fail "$R" && log "PASS" "RBAC: user create announcement" "rejected" || log "FAIL" "RBAC: user create announcement" "not rejected"

# ========== SUMMARY ==========
echo ""
echo "========================================="
echo "           TEST REPORT"
echo "========================================="
echo "Total:   $TOTAL"
echo "Passed:  $PASS"
echo "Failed:  $FAIL"
echo "Rate:    $(awk "BEGIN{if($TOTAL>0) printf \"%.1f\", $PASS*100/$TOTAL; else print \"0\"}")%"
echo "========================================="

if [ "$FAIL" -gt 0 ]; then
  echo ""
  echo "=== FAILED TESTS ==="
  for r in "${RESULTS[@]}"; do
    IFS='|' read -r s n d <<< "$r"
    [ "$s" = "FAIL" ] && printf "  [FAIL] %-55s %s\n" "$n" "$d"
  done
fi

echo ""
echo "=== ALL TESTS BY MODULE ==="
for r in "${RESULTS[@]}"; do
  IFS='|' read -r s n d <<< "$r"
  printf "  [%-4s] %s\n" "$s" "$n"
done
