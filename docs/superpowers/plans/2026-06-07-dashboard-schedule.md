# 首页日程管理 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a read-only dashboard schedule section that shows the current month calendar and the current user's visible meetings and large activities, without showing approval tasks on the calendar.

**Architecture:** Extend the existing `/api/dashboard` aggregate response with `monthlyScheduleItems`. The backend builds schedule items from visible meetings using the same business-read permission path as the existing dashboard aggregation, and the Vue dashboard renders a simple in-component calendar without adding a calendar dependency.

**Tech Stack:** Spring Boot 2.5, Java 8, MockMvc/JUnit 5, Vue 3, Element Plus, Vite, Vitest.

---

## File Structure

- Modify `backend/src/test/java/com/university/oms/DashboardScopeTest.java`
  - Add a focused MockMvc regression test for dashboard monthly schedule visibility, current-month filtering, rejected-meeting exclusion, room name, and large-activity flag.
- Create `backend/src/main/java/com/university/oms/model/DashboardScheduleItem.java`
  - Carry the dashboard-only schedule projection returned in `DashboardStats`.
- Modify `backend/src/main/java/com/university/oms/model/DashboardStats.java`
  - Add `monthlyScheduleItems` with a non-null empty-list default.
- Modify `backend/src/main/java/com/university/oms/design/DashboardFacade.java`
  - Populate `monthlyScheduleItems` from visible meetings during dashboard aggregation.
- Modify `frontend/src/views/Dashboard.spec.js`
  - Add source-level checks for the schedule panel, calendar grid, dashboard response field, and the absence of approval-task calendar coupling.
- Modify `frontend/src/views/Dashboard.vue`
  - Render the schedule panel after announcements and before charts.
  - Compute the current-month calendar grid, date markers, selected-day list, and month list from `monthlyScheduleItems`.

---

### Task 1: Backend Failing Test For Dashboard Schedule

**Files:**
- Modify: `backend/src/test/java/com/university/oms/DashboardScopeTest.java`

- [ ] **Step 1: Write the failing test**

Add these imports near the existing imports:

```java
import com.university.oms.model.Meeting;
import com.university.oms.repository.InMemoryDatabase;

import java.time.LocalDate;
import java.time.LocalDateTime;
```

Add this field after `private ObjectMapper objectMapper;`:

```java
    @Autowired
    private InMemoryDatabase db;
```

Add this test and helper methods inside `DashboardScopeTest`:

```java
    @Test
    void dashboardIncludesOnlyVisibleCurrentMonthMeetingSchedules() throws Exception {
        String userToken = login("user");
        String adminToken = login("admin");
        LocalDate month = LocalDate.now().withDayOfMonth(1);

        Meeting ownActivity = meeting("own-visible-monthly-activity", 2L,
                month.plusDays(7).atTime(9, 0), month.plusDays(7).atTime(11, 0),
                "approved", true);
        Meeting hiddenMeeting = meeting("hidden-other-dept-monthly-meeting", 6L,
                month.plusDays(8).atTime(9, 0), month.plusDays(8).atTime(10, 0),
                "approved", false);
        Meeting rejectedMeeting = meeting("rejected-own-monthly-meeting", 2L,
                month.plusDays(9).atTime(9, 0), month.plusDays(9).atTime(10, 0),
                "rejected", false);
        Meeting nextMonthMeeting = meeting("next-month-own-meeting", 2L,
                month.plusMonths(1).plusDays(2).atTime(9, 0),
                month.plusMonths(1).plusDays(2).atTime(10, 0),
                "approved", false);

        db.meetings().put(ownActivity.getId(), ownActivity);
        db.meetings().put(hiddenMeeting.getId(), hiddenMeeting);
        db.meetings().put(rejectedMeeting.getId(), rejectedMeeting);
        db.meetings().put(nextMonthMeeting.getId(), nextMonthMeeting);

        JsonNode userSchedules = dashboard(userToken).get("monthlyScheduleItems");
        assertEquals(1, userSchedules.size());
        assertEquals("own-visible-monthly-activity", userSchedules.get(0).get("title").asText());
        assertEquals("meeting", userSchedules.get(0).get("bizType").asText());
        assertEquals("大型活动", userSchedules.get(0).get("typeText").asText());
        assertTrue(userSchedules.get(0).get("largeActivity").asBoolean());
        assertEquals("理科一号楼 101", userSchedules.get(0).get("roomName").asText());

        String userScheduleText = userSchedules.toString();
        assertTrue(!userScheduleText.contains("hidden-other-dept-monthly-meeting"));
        assertTrue(!userScheduleText.contains("rejected-own-monthly-meeting"));
        assertTrue(!userScheduleText.contains("next-month-own-meeting"));

        JsonNode adminSchedules = dashboard(adminToken).get("monthlyScheduleItems");
        String adminScheduleText = adminSchedules.toString();
        assertTrue(adminScheduleText.contains("own-visible-monthly-activity"));
        assertTrue(adminScheduleText.contains("hidden-other-dept-monthly-meeting"));
        assertTrue(!adminScheduleText.contains("rejected-own-monthly-meeting"));
        assertTrue(!adminScheduleText.contains("next-month-own-meeting"));
    }

    private Meeting meeting(String title, Long organizerId, LocalDateTime startTime,
                            LocalDateTime endTime, String status, boolean largeActivity) {
        Meeting meeting = new Meeting();
        db.fill(meeting, db.nextId());
        meeting.setTitle(title);
        meeting.setRoomId(1L);
        meeting.setOrganizerId(organizerId);
        meeting.setStartTime(startTime);
        meeting.setEndTime(endTime);
        meeting.setExpectedCount(largeActivity ? 600 : 20);
        meeting.setVenueType("室内");
        meeting.setMeetingType("国内业务会议");
        meeting.setStatus(status);
        meeting.setLargeActivity(largeActivity);
        return meeting;
    }
```

- [ ] **Step 2: Run the backend test to verify it fails**

Run:

```powershell
cd D:\work\xianyu\OfficeManagementSystem\backend
mvn -Dtest=DashboardScopeTest test
```

Expected: FAIL because `monthlyScheduleItems` does not exist on the dashboard response yet, or because `DashboardScheduleItem` has not been implemented.

- [ ] **Step 3: Commit the failing test**

```powershell
git add backend/src/test/java/com/university/oms/DashboardScopeTest.java
git commit -m "test: cover dashboard monthly schedules"
```

---

### Task 2: Backend Dashboard Schedule Model And Aggregation

**Files:**
- Create: `backend/src/main/java/com/university/oms/model/DashboardScheduleItem.java`
- Modify: `backend/src/main/java/com/university/oms/model/DashboardStats.java`
- Modify: `backend/src/main/java/com/university/oms/design/DashboardFacade.java`
- Test: `backend/src/test/java/com/university/oms/DashboardScopeTest.java`

- [ ] **Step 1: Create the dashboard schedule projection**

Create `backend/src/main/java/com/university/oms/model/DashboardScheduleItem.java`:

```java
package com.university.oms.model;

import java.time.LocalDateTime;

public class DashboardScheduleItem {
    private Long id;
    private String bizType;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private boolean largeActivity;
    private String roomName;
    private String typeText;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isLargeActivity() { return largeActivity; }
    public void setLargeActivity(boolean largeActivity) { this.largeActivity = largeActivity; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getTypeText() { return typeText; }
    public void setTypeText(String typeText) { this.typeText = typeText; }
}
```

- [ ] **Step 2: Add the field to `DashboardStats`**

In `backend/src/main/java/com/university/oms/model/DashboardStats.java`, add the import:

```java
import java.util.ArrayList;
import java.util.List;
```

Ensure the existing imports include `ArrayList` and `List`; if `ArrayList` and `List` are already imported by this file, keep one import each.

Add this field after `monthlyBusinessCounts`:

```java
    private List<DashboardScheduleItem> monthlyScheduleItems = new ArrayList<>();
```

Add this getter and setter before the final closing brace:

```java
    public List<DashboardScheduleItem> getMonthlyScheduleItems() {
        return monthlyScheduleItems;
    }

    public void setMonthlyScheduleItems(List<DashboardScheduleItem> monthlyScheduleItems) {
        this.monthlyScheduleItems = monthlyScheduleItems == null ? new ArrayList<>() : monthlyScheduleItems;
    }
```

- [ ] **Step 3: Populate monthly schedules in `DashboardFacade`**

In `backend/src/main/java/com/university/oms/design/DashboardFacade.java`, add this call after `stats.setMonthlyBusinessCounts(monthly);`:

```java
        stats.setMonthlyScheduleItems(scheduleItems(meetings));
```

Add these private methods before the final closing brace of `DashboardFacade`:

```java
    private List<DashboardScheduleItem> scheduleItems(Collection<Meeting> meetings) {
        LocalDateTime monthStart = LocalDateTime.now().toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = monthStart.plusMonths(1);
        List<DashboardScheduleItem> items = new ArrayList<DashboardScheduleItem>();
        for (Meeting meeting : meetings) {
            if (!shouldIncludeInSchedule(meeting, monthStart, nextMonthStart)) {
                continue;
            }
            MeetingRoom room = db.rooms().get(meeting.getRoomId());
            DashboardScheduleItem item = new DashboardScheduleItem();
            item.setId(meeting.getId());
            item.setBizType("meeting");
            item.setTitle(meeting.getTitle());
            item.setStartTime(meeting.getStartTime());
            item.setEndTime(meeting.getEndTime());
            item.setStatus(meeting.getStatus());
            item.setLargeActivity(meeting.isLargeActivity());
            item.setRoomName(room == null ? "" : room.getRoomName());
            item.setTypeText(meeting.isLargeActivity() ? "大型活动" : "会议");
            items.add(item);
        }
        items.sort(Comparator.comparing(DashboardScheduleItem::getStartTime));
        return items;
    }

    private boolean shouldIncludeInSchedule(Meeting meeting, LocalDateTime monthStart, LocalDateTime nextMonthStart) {
        if (meeting.getStartTime() == null || meeting.getEndTime() == null) {
            return false;
        }
        if ("rejected".equals(meeting.getStatus())) {
            return false;
        }
        return meeting.getStartTime().isBefore(nextMonthStart) && !meeting.getEndTime().isBefore(monthStart);
    }
```

- [ ] **Step 4: Run the backend test to verify it passes**

Run:

```powershell
cd D:\work\xianyu\OfficeManagementSystem\backend
mvn -Dtest=DashboardScopeTest test
```

Expected: PASS. The new `dashboardIncludesOnlyVisibleCurrentMonthMeetingSchedules` test should pass, and the existing dashboard scope test should still pass.

- [ ] **Step 5: Commit the backend implementation**

```powershell
git add backend/src/main/java/com/university/oms/model/DashboardScheduleItem.java backend/src/main/java/com/university/oms/model/DashboardStats.java backend/src/main/java/com/university/oms/design/DashboardFacade.java
git commit -m "feat: add dashboard monthly schedule data"
```

---

### Task 3: Frontend Failing Test For Schedule Panel

**Files:**
- Modify: `frontend/src/views/Dashboard.spec.js`

- [ ] **Step 1: Write the failing frontend source test**

Append this test to the existing `describe('Dashboard announcement links', () => { ... })` block:

```js
  it('renders a dashboard schedule calendar without approval tasks', () => {
    expect(dashboardSource).toContain('日程管理')
    expect(dashboardSource).toContain('monthlyScheduleItems')
    expect(dashboardSource).toContain('calendar-days')
    expect(dashboardSource).toContain('schedule-item')
    expect(dashboardSource).toContain('当日暂无会议或活动')
    expect(dashboardSource).not.toContain('flowTasks')
    expect(dashboardSource).not.toContain('待审批任务')
  })
```

- [ ] **Step 2: Run the frontend test to verify it fails**

Run:

```powershell
cd D:\work\xianyu\OfficeManagementSystem\frontend
npm test -- Dashboard.spec.js
```

Expected: FAIL because `Dashboard.vue` does not contain the schedule panel yet.

- [ ] **Step 3: Commit the failing frontend test**

```powershell
git add frontend/src/views/Dashboard.spec.js
git commit -m "test: cover dashboard schedule panel"
```

---

### Task 4: Frontend Schedule Calendar Implementation

**Files:**
- Modify: `frontend/src/views/Dashboard.vue`
- Test: `frontend/src/views/Dashboard.spec.js`

- [ ] **Step 1: Add the schedule panel template**

In `frontend/src/views/Dashboard.vue`, insert this block after the announcement panel and before `<div class="chart-grid">`:

```vue
    <div class="panel schedule-panel">
      <div class="panel-title">
        <h3>日程管理</h3>
        <span class="schedule-month">{{ scheduleMonthTitle }}</span>
      </div>
      <div class="schedule-layout">
        <div class="calendar-box">
          <div class="calendar-weekdays">
            <span v-for="day in weekdayNames" :key="day">{{ day }}</span>
          </div>
          <div class="calendar-days">
            <button
              v-for="day in calendarDays"
              :key="day.key"
              type="button"
              class="calendar-day"
              :class="{
                'is-empty': day.empty,
                'is-today': day.isToday,
                'is-selected': day.key === selectedDateKey
              }"
              :disabled="day.empty"
              @click="selectDate(day.key)"
            >
              <span>{{ day.label }}</span>
              <span v-if="!day.empty && scheduleTypeForDay(day.key)" class="schedule-dots">
                <i v-if="scheduleTypeForDay(day.key).meeting" class="dot meeting-dot"></i>
                <i v-if="scheduleTypeForDay(day.key).activity" class="dot activity-dot"></i>
              </span>
            </button>
          </div>
        </div>

        <div class="schedule-list">
          <div class="schedule-list-title">
            <h4>{{ selectedDateLabel }}</h4>
            <span>本月 {{ monthlyScheduleItems.length }} 项</span>
          </div>
          <el-empty
            v-if="selectedDayItems.length === 0"
            :description="monthlyScheduleItems.length === 0 ? '本月暂无会议或活动' : '当日暂无会议或活动'"
          />
          <div v-else class="schedule-items">
            <div v-for="item in selectedDayItems" :key="item.bizType + '-' + item.id" class="schedule-item">
              <div>
                <el-tag :type="item.largeActivity ? 'warning' : 'primary'" size="small">
                  {{ item.typeText || (item.largeActivity ? '大型活动' : '会议') }}
                </el-tag>
                <strong>{{ item.title }}</strong>
              </div>
              <p>{{ formatScheduleTime(item) }}</p>
              <p>{{ item.roomName || '未指定会议室' }} · {{ labelOf('business_status', item.status) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
```

- [ ] **Step 2: Add schedule state and computed values**

In the `<script setup>` block, keep the existing imports and add these variables after `const selectedAnnouncement = ref(null)`:

```js
const monthlyScheduleItems = ref([])
const selectedDateKey = ref(dateKey(new Date()))
const weekdayNames = ['一', '二', '三', '四', '五', '六', '日']
```

Add these computed values after the existing `hasTravelBudgetData` computed:

```js
const scheduleMonthDate = computed(() => {
  const now = new Date()
  return new Date(now.getFullYear(), now.getMonth(), 1)
})

const scheduleMonthTitle = computed(() => {
  const month = scheduleMonthDate.value
  return `${month.getFullYear()} 年 ${month.getMonth() + 1} 月日程`
})

const calendarDays = computed(() => {
  const month = scheduleMonthDate.value
  const year = month.getFullYear()
  const monthIndex = month.getMonth()
  const firstDay = new Date(year, monthIndex, 1)
  const leadingDays = (firstDay.getDay() + 6) % 7
  const totalDays = new Date(year, monthIndex + 1, 0).getDate()
  const days = []
  for (let i = 0; i < leadingDays; i++) {
    days.push({ key: `empty-${i}`, label: '', empty: true })
  }
  const today = dateKey(new Date())
  for (let day = 1; day <= totalDays; day++) {
    const current = new Date(year, monthIndex, day)
    const key = dateKey(current)
    days.push({
      key,
      label: day,
      empty: false,
      isToday: key === today
    })
  }
  return days
})

const scheduleByDate = computed(() => {
  const grouped = {}
  monthlyScheduleItems.value.forEach((item) => {
    dateRangeKeys(item).forEach((key) => {
      grouped[key] = grouped[key] || []
      grouped[key].push(item)
    })
  })
  Object.values(grouped).forEach((items) => {
    items.sort((a, b) => String(a.startTime || '').localeCompare(String(b.startTime || '')))
  })
  return grouped
})

const selectedDayItems = computed(() => scheduleByDate.value[selectedDateKey.value] || [])

const selectedDateLabel = computed(() => {
  const date = parseDateKey(selectedDateKey.value)
  return date ? `${date.getMonth() + 1} 月 ${date.getDate()} 日事项` : '当日事项'
})
```

Inside `onMounted`, after `Object.assign(stats, dashboardData)`, add:

```js
  monthlyScheduleItems.value = dashboardData.monthlyScheduleItems || []
```

- [ ] **Step 3: Add schedule helper functions**

Add these functions before `formatDate(value)`:

```js
function selectDate(key) {
  selectedDateKey.value = key
}

function scheduleTypeForDay(key) {
  const items = scheduleByDate.value[key] || []
  if (items.length === 0) {
    return null
  }
  return {
    meeting: items.some((item) => !item.largeActivity),
    activity: items.some((item) => item.largeActivity)
  }
}

function dateRangeKeys(item) {
  if (!item.startTime) {
    return []
  }
  const start = new Date(item.startTime)
  const end = item.endTime ? new Date(item.endTime) : start
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
    return []
  }
  const month = scheduleMonthDate.value
  const monthStart = new Date(month.getFullYear(), month.getMonth(), 1)
  const monthEnd = new Date(month.getFullYear(), month.getMonth() + 1, 0)
  const current = new Date(Math.max(startOfDay(start).getTime(), monthStart.getTime()))
  const finalDay = new Date(Math.min(startOfDay(end).getTime(), monthEnd.getTime()))
  const keys = []
  while (current <= finalDay) {
    keys.push(dateKey(current))
    current.setDate(current.getDate() + 1)
  }
  return keys
}

function formatScheduleTime(item) {
  if (!item.startTime) {
    return '-'
  }
  const start = new Date(item.startTime)
  const end = item.endTime ? new Date(item.endTime) : null
  if (Number.isNaN(start.getTime())) {
    return '-'
  }
  const startText = `${pad(start.getHours())}:${pad(start.getMinutes())}`
  if (!end || Number.isNaN(end.getTime())) {
    return startText
  }
  const endText = `${pad(end.getHours())}:${pad(end.getMinutes())}`
  if (dateKey(start) === dateKey(end)) {
    return `${startText} - ${endText}`
  }
  return `${start.getMonth() + 1}/${start.getDate()} ${startText} - ${end.getMonth() + 1}/${end.getDate()} ${endText}`
}

function dateKey(date) {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

function parseDateKey(key) {
  const parts = String(key || '').split('-').map(Number)
  if (parts.length !== 3 || parts.some((part) => Number.isNaN(part))) {
    return null
  }
  return new Date(parts[0], parts[1] - 1, parts[2])
}

function startOfDay(date) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate())
}

function pad(value) {
  return String(value).padStart(2, '0')
}
```

- [ ] **Step 4: Add responsive schedule styles**

In the `<style scoped>` block of `Dashboard.vue`, add these styles before the existing `@media (max-width: 700px)` block:

```css
.schedule-panel {
  margin-top: 14px;
}

.schedule-month {
  color: #657487;
  font-size: 14px;
}

.schedule-layout {
  display: grid;
  grid-template-columns: minmax(320px, 1fr) minmax(280px, 420px);
  gap: 16px;
  margin-top: 14px;
}

.calendar-weekdays,
.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 6px;
}

.calendar-weekdays {
  margin-bottom: 8px;
  color: #657487;
  font-size: 13px;
  text-align: center;
}

.calendar-day {
  position: relative;
  aspect-ratio: 1 / 0.72;
  min-height: 46px;
  border: 1px solid #e3e8ef;
  border-radius: 8px;
  background: #fff;
  color: #223042;
  cursor: pointer;
  font: inherit;
}

.calendar-day:hover:not(.is-empty),
.calendar-day.is-selected {
  border-color: #1f5f8b;
  background: #eef7fc;
}

.calendar-day.is-today {
  box-shadow: inset 0 0 0 2px rgba(31, 95, 139, 0.18);
}

.calendar-day.is-empty {
  cursor: default;
  background: #f8fafc;
}

.schedule-dots {
  position: absolute;
  left: 50%;
  bottom: 7px;
  display: flex;
  gap: 4px;
  transform: translateX(-50%);
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.meeting-dot {
  background: #1f5f8b;
}

.activity-dot {
  background: #e6a23c;
}

.schedule-list {
  min-width: 0;
}

.schedule-list-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.schedule-list-title h4 {
  margin: 0;
  font-size: 16px;
  letter-spacing: 0;
}

.schedule-list-title span {
  color: #657487;
  white-space: nowrap;
}

.schedule-items {
  display: grid;
  gap: 10px;
}

.schedule-item {
  border: 1px solid #eef2f6;
  border-radius: 8px;
  padding: 10px;
}

.schedule-item div {
  display: flex;
  align-items: center;
  gap: 8px;
}

.schedule-item strong {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.schedule-item p {
  margin: 6px 0 0;
  color: #657487;
}
```

Extend the existing mobile media rule with this block:

```css
  .schedule-layout {
    grid-template-columns: 1fr;
  }

  .calendar-day {
    min-height: 42px;
  }
```

- [ ] **Step 5: Run the frontend test to verify it passes**

Run:

```powershell
cd D:\work\xianyu\OfficeManagementSystem\frontend
npm test -- Dashboard.spec.js
```

Expected: PASS. The new schedule source test and existing dashboard tests should pass.

- [ ] **Step 6: Commit the frontend implementation**

```powershell
git add frontend/src/views/Dashboard.vue
git commit -m "feat: show dashboard schedule calendar"
```

---

### Task 5: Full Verification

**Files:**
- Verify only; no source files should be modified by this task.

- [ ] **Step 1: Run backend dashboard scope tests**

Run:

```powershell
cd D:\work\xianyu\OfficeManagementSystem\backend
mvn -Dtest=DashboardScopeTest test
```

Expected: PASS.

- [ ] **Step 2: Run all frontend tests**

Run:

```powershell
cd D:\work\xianyu\OfficeManagementSystem\frontend
npm test
```

Expected: PASS.

- [ ] **Step 3: Build the frontend**

Run:

```powershell
cd D:\work\xianyu\OfficeManagementSystem\frontend
npm run build
```

Expected: PASS and Vite writes the production bundle to `frontend/dist`.

- [ ] **Step 4: Check git status**

Run:

```powershell
cd D:\work\xianyu\OfficeManagementSystem
git status --short
```

Expected: only pre-existing unrelated untracked files remain, such as `docs/IMPROVEMENT-PLAN.md` and `test-screenshots/24-login-empty-creds.png`, unless the implementation produced additional intended files.

---

## Self-Review

- Spec coverage: backend aggregation, current-month filter, current-user visibility, rejected exclusion, room name, large activity label, front-end calendar, selected-day list, no approval tasks, empty states, and test coverage are each mapped to a task above.
- Placeholder scan: no unresolved placeholder steps are present; every code-editing step includes concrete code.
- Type consistency: the plan consistently uses `monthlyScheduleItems`, `DashboardScheduleItem`, `largeActivity`, `roomName`, `typeText`, `selectedDateKey`, and `scheduleByDate`.
