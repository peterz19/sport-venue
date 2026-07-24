<template>
  <div class="page">
    <el-card class="toolbar">
      <el-button type="primary" @click="load">刷新排行</el-button>
    </el-card>
    <el-card>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="teamName" label="球队" min-width="140" />
        <el-table-column prop="played" label="场次" width="80" />
        <el-table-column prop="win" label="胜" width="70" />
        <el-table-column prop="draw" label="平" width="70" />
        <el-table-column prop="loss" label="负" width="70" />
        <el-table-column prop="pointsFor" label="得分" width="80" />
        <el-table-column prop="pointsAgainst" label="失分" width="80" />
        <el-table-column prop="pointDiff" label="净胜" width="80" />
        <el-table-column prop="rankingPoints" label="积分" width="80" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { onMounted, ref } from "vue"
import { matchApi } from "@/api"

export default {
  name: "TeamRanking",
  setup() {
    const loading = ref(false)
    const list = ref([])
    const load = async () => {
      loading.value = true
      try {
        list.value = (await matchApi.ranking()) || []
      } finally {
        loading.value = false
      }
    }
    onMounted(load)
    return { loading, list, load }
  }
}
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
</style>
