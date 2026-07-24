<template>
  <div class="page">
    <el-card class="toolbar">
      <el-form inline>
        <el-form-item label="片场">
          <el-select v-model="courtId" style="width: 220px" @change="load">
            <el-option v-for="c in courts" :key="c.id" :label="`${c.venueName} · ${c.name}`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="date" type="date" value-format="YYYY-MM-DD" @change="load" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="openCreate()">新建订场</el-button>
          <el-button @click="load">刷新</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <div class="grid">
        <div
          v-for="slot in slots"
          :key="slot.startTime"
          class="slot"
          :class="{ occupied: slot.occupied }"
          @click="onSlotClick(slot)"
        >
          <div class="time">{{ slot.startTime.slice(11, 16) }}</div>
          <div class="title">{{ slot.occupied ? slot.title : "可订" }}</div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="dialog.visible" title="新建订场" width="520px">
      <el-form :model="dialog.form" label-width="100px">
        <el-form-item label="片场">{{ courtLabel }}</el-form-item>
        <el-form-item label="开始">
          <el-input v-model="dialog.form.startTime" />
        </el-form-item>
        <el-form-item label="结束">
          <el-input v-model="dialog.form.endTime" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-radio-group v-model="dialog.form.bookType">
            <el-radio label="TEAM">球队</el-radio>
            <el-radio label="PERSON">个人</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="dialog.form.bookType === 'TEAM'">
          <el-form-item label="球队" required>
            <el-select v-model="dialog.form.teamId" style="width: 100%" @change="onTeamChange">
              <el-option v-for="t in teams" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="对接员工">
            <span>{{ dialog.liaisonName || "-" }}</span>
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="联系人" required><el-input v-model="dialog.form.personName" /></el-form-item>
          <el-form-item label="电话" required><el-input v-model="dialog.form.personPhone" /></el-form-item>
        </template>
        <el-form-item label="金额">
          <el-input-number v-model="dialog.form.amount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="dialog.form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="save">确认订场</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { computed, onMounted, reactive, ref } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { bookingApi, courtApi, teamApi } from "@/api"

export default {
  name: "BookingCalendar",
  setup() {
    const courts = ref([])
    const teams = ref([])
    const courtId = ref()
    const date = ref(new Date().toISOString().slice(0, 10))
    const slots = ref([])
    const dialog = reactive({
      visible: false,
      liaisonName: "",
      form: {
        courtId: null,
        startTime: "",
        endTime: "",
        bookType: "TEAM",
        teamId: null,
        personName: "",
        personPhone: "",
        amount: 0,
        remark: ""
      }
    })

    const courtLabel = computed(() => {
      const c = courts.value.find((x) => x.id === courtId.value)
      return c ? `${c.venueName} · ${c.name}` : "-"
    })

    const loadCourts = async () => {
      courts.value = (await courtApi.options()) || []
      if (!courtId.value && courts.value.length) courtId.value = courts.value[0].id
    }

    const loadTeams = async () => {
      teams.value = (await teamApi.options()) || []
    }

    const load = async () => {
      if (!courtId.value || !date.value) return
      const data = await bookingApi.calendar({ courtId: courtId.value, date: date.value })
      slots.value = data?.slots || []
    }

    const openCreate = (slot) => {
      if (!courtId.value) {
        ElMessage.warning("请先创建片场")
        return
      }
      const start = slot?.startTime || `${date.value} 18:00:00`
      const end = slot?.endTime || `${date.value} 18:30:00`
      dialog.form = {
        courtId: courtId.value,
        startTime: start,
        endTime: end,
        bookType: "TEAM",
        teamId: teams.value[0]?.id || null,
        personName: "",
        personPhone: "",
        amount: 0,
        remark: ""
      }
      onTeamChange(dialog.form.teamId)
      dialog.visible = true
    }

    const onSlotClick = async (slot) => {
      if (slot.occupied) {
        await ElMessageBox.alert(`已占用：${slot.title || ""}（${slot.orderNo}）`, "时段占用")
        return
      }
      openCreate(slot)
    }

    const onTeamChange = (id) => {
      const t = teams.value.find((x) => x.id === id)
      dialog.liaisonName = t?.liaisonStaffName || ""
    }

    const save = async () => {
      try {
        await bookingApi.create(dialog.form)
        ElMessage.success("订场成功")
        dialog.visible = false
        load()
      } catch (e) {
        // request interceptor already tips
      }
    }

    onMounted(async () => {
      await Promise.all([loadCourts(), loadTeams()])
      await load()
    })

    return {
      courts, teams, courtId, date, slots, dialog, courtLabel,
      load, openCreate, onSlotClick, onTeamChange, save
    }
  }
}
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
  gap: 8px;
}
.slot {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  padding: 8px;
  cursor: pointer;
  min-height: 64px;
  background: #f0f9eb;
}
.slot.occupied {
  background: #fef0f0;
  border-color: #f56c6c;
  cursor: not-allowed;
}
.time { font-weight: 600; font-size: 13px; }
.title { margin-top: 4px; font-size: 12px; color: #606266; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
