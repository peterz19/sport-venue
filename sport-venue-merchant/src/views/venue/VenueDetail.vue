<template>
  <div class="venue-detail">
    <el-card>
      <template #header>
        <div class="header">
          <span>场馆详情</span>
          <div>
            <el-button type="primary" @click="goEdit">去编辑</el-button>
            <el-button @click="$router.back()">返回</el-button>
          </div>
        </div>
      </template>
      <el-descriptions v-loading="loading" :column="2" border>
        <el-descriptions-item label="ID">{{ venue.id }}</el-descriptions-item>
        <el-descriptions-item label="名称">{{ venue.name }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ venue.type }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ venue.status }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ venue.address }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ venue.phone || "-" }}</el-descriptions-item>
        <el-descriptions-item label="开放">{{ venue.openTime || "-" }} ~ {{ venue.closeTime || "-" }}</el-descriptions-item>
        <el-descriptions-item label="容量">{{ venue.currentOccupancy || 0 }} / {{ venue.capacity || "-" }}</el-descriptions-item>
        <el-descriptions-item label="评分">{{ venue.rating || 0 }}</el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">{{ venue.description || "-" }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script>
import { onMounted, ref } from "vue"
import { useRoute, useRouter } from "vue-router"
import { merchantVenueApi } from "@/api"

export default {
  name: "VenueDetail",
  setup() {
    const route = useRoute()
    const router = useRouter()
    const loading = ref(false)
    const venue = ref({})

    const load = async () => {
      loading.value = true
      try {
        venue.value = (await merchantVenueApi.getVenueById(route.params.id)) || {}
      } finally {
        loading.value = false
      }
    }

    const goEdit = () => router.push("/venue/list")

    onMounted(load)
    return { loading, venue, goEdit }
  }
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
