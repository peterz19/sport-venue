<template>
  <div class="venue-detail">
    <el-card>
      <template #header>
        <div class="header">
          <span>场馆详情</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>
      <el-descriptions v-loading="loading" :column="2" border>
        <el-descriptions-item label="ID">{{ venue.id }}</el-descriptions-item>
        <el-descriptions-item label="名称">{{ venue.name }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ venue.status }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ venue.address }}</el-descriptions-item>
        <el-descriptions-item label="当前人数">{{ venue.currentOccupancy }}</el-descriptions-item>
        <el-descriptions-item label="容量">{{ venue.capacity }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from "vue"
import { useRoute } from "vue-router"
import { merchantVenueApi } from "@/api"

export default {
  name: "VenueDetail",
  setup() {
    const route = useRoute()
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

    onMounted(load)
    return { loading, venue }
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
