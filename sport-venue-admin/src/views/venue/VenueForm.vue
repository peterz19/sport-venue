<template>
  <div class="venue-form">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑场馆' : '添加场馆' }}</span>
      </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        style="max-width: 600px"
      >
        <el-form-item label="场馆名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入场馆名称"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="场馆类型" prop="type">
          <el-select
            v-model="form.type"
            placeholder="请选择场馆类型"
            style="width: 100%"
          >
            <el-option
              v-for="type in venueTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="空间类型" prop="spaceType">
          <el-select
            v-model="form.spaceType"
            placeholder="请选择空间类型"
            style="width: 100%"
          >
            <el-option
              v-for="type in venueSpaceTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="付费类型" prop="chargeType">
          <el-select
            v-model="form.chargeType"
            placeholder="请选择付费类型"
            style="width: 100%"
          >
            <el-option
              v-for="type in venueChargeTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="场馆状态" prop="status">
          <el-select
            v-model="form.status"
            placeholder="请选择场馆状态"
            style="width: 100%"
          >
            <el-option
              v-for="status in venueStatuses"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="最大容量" prop="capacity">
          <el-input-number
            v-model="form.capacity"
            :min="1"
            :max="1000"
            style="width: 100%"
            placeholder="请输入最大容量"
          />
        </el-form-item>
        
        <el-form-item label="当前人数" prop="currentOccupancy">
          <el-input-number
            v-model="form.currentOccupancy"
            :min="0"
            :max="form.capacity || 1000"
            style="width: 100%"
            placeholder="请输入当前人数"
          />
        </el-form-item>
        
        <el-form-item label="评分" prop="rating">
          <el-rate
            v-model="form.rating"
            :max="5"
            show-score
            text-color="#ff9900"
            score-template="{value}"
            :model-value="Number(form.rating)"
            @update:model-value="form.rating = Number($event)"
          />
        </el-form-item>
        
        <el-form-item label="地址" prop="address">
          <el-input
            v-model="form.address"
            type="textarea"
            :rows="3"
            placeholder="请输入场馆地址"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入场馆描述"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="所属商户" prop="merchantId">
          <el-select
            v-model="form.merchantId"
            placeholder="请选择商户"
            style="width: 100%"
            filterable
            clearable
          >
            <el-option
              v-for="merchant in merchants"
              :key="merchant.id"
              :label="merchant.name"
              :value="merchant.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { venueApi } from "@/api/venue"
import { merchantApi } from "@/api/merchant"

export default {
  name: "VenueForm",
  setup() {
    const route = useRoute()
    const router = useRouter()
    const formRef = ref()
    const loading = ref(false)
    const venueTypes = ref([])
    const venueSpaceTypes = ref([])
    const venueChargeTypes = ref([])
    const venueStatuses = ref([])
    const merchants = ref([])

    // 判断是否为编辑模式
    const isEdit = computed(() => !!route.params.id)

    // 表单数据
    const form = reactive({
      name: "",
      type: "",
      spaceType: "INDOOR",
      chargeType: "FREE",
      status: "ACTIVE",
      capacity: 100,
      currentOccupancy: 0,
      rating: 5,
      address: "",
      description: "",
      merchantId: null
    })

    // 表单验证规则
    const rules = {
      name: [
        { required: true, message: "请输入场馆名称", trigger: "blur" },
        { min: 2, max: 50, message: "场馆名称长度在 2 到 50 个字符", trigger: "blur" }
      ],
      type: [
        { required: true, message: "请选择场馆类型", trigger: "change" }
      ],
      spaceType: [
        { required: true, message: "请选择空间类型", trigger: "change" }
      ],
      chargeType: [
        { required: true, message: "请选择付费类型", trigger: "change" }
      ],
      status: [
        { required: true, message: "请选择场馆状态", trigger: "change" }
      ],
      capacity: [
        { required: true, message: "请输入最大容量", trigger: "blur" },
        { type: "number", min: 1, message: "最大容量必须大于0", trigger: "blur" }
      ],
      currentOccupancy: [
        { required: true, message: "请输入当前人数", trigger: "blur" },
        { type: "number", min: 0, message: "当前人数不能小于0", trigger: "blur" }
      ],
      address: [
        { required: true, message: "请输入场馆地址", trigger: "blur" }
      ],
      merchantId: [
        { required: true, message: "请选择商户", trigger: "change" }
      ]
    }

    // 获取场馆类型
    const getVenueTypes = async () => {
      try {
        const data = await venueApi.getVenueTypes()
        // 转换数据格式：{code, description} -> {value, label}
        venueTypes.value = (data || []).map(item => ({
          value: item.code,
          label: item.description
        }))
      } catch (error) {
        console.error("获取场馆类型失败:", error)
      }
    }

    // 获取空间类型
    const getVenueSpaceTypes = async () => {
      try {
        const data = await venueApi.getVenueSpaceTypes()
        // 转换数据格式：{code, description} -> {value, label}
        venueSpaceTypes.value = (data || []).map(item => ({
          value: item.code,
          label: item.description
        }))
      } catch (error) {
        console.error("获取空间类型失败:", error)
      }
    }

    // 获取付费类型
    const getVenueChargeTypes = async () => {
      try {
        const data = await venueApi.getVenueChargeTypes()
        // 转换数据格式：{code, description} -> {value, label}
        venueChargeTypes.value = (data || []).map(item => ({
          value: item.code,
          label: item.description
        }))
      } catch (error) {
        console.error("获取付费类型失败:", error)
      }
    }

    // 获取场馆状态
    const getVenueStatuses = async () => {
      try {
        const data = await venueApi.getVenueStatuses()
        // 转换数据格式：{code, description} -> {value, label}
        venueStatuses.value = (data || []).map(item => ({
          value: item.code,
          label: item.description
        }))
      } catch (error) {
        console.error("获取场馆状态失败:", error)
      }
    }

    // 获取商户列表
    const getMerchants = async () => {
      try {
        const data = await merchantApi.getMerchants()
        merchants.value = data || []
      } catch (error) {
        console.error("获取商户列表失败:", error)
        // 如果API不存在，使用模拟数据
        merchants.value = [
          { id: 1, name: '星光体育' },
          { id: 2, name: '网球天地' },
          { id: 3, name: '健身中心' }
        ]
      }
    }

    // 获取场馆详情
    const getVenueDetail = async (id) => {
      try {
        const data = await venueApi.getVenueById(id)
        Object.assign(form, data)
      } catch (error) {
        console.error("获取场馆详情失败:", error)
        ElMessage.error("获取场馆详情失败")
      }
    }

    // 提交表单
    const handleSubmit = async () => {
      try {
        await formRef.value.validate()
        loading.value = true

        if (isEdit.value) {
          await venueApi.updateVenue(route.params.id, form)
          ElMessage.success("更新成功")
        } else {
          await venueApi.createVenue(form)
          ElMessage.success("创建成功")
        }

        router.push("/venue/list")
      } catch (error) {
        console.error("提交失败:", error)
        ElMessage.error("提交失败")
      } finally {
        loading.value = false
      }
    }

    // 取消
    const handleCancel = () => {
      router.push("/venue/list")
    }

    onMounted(async () => {
      await getVenueTypes()
      await getVenueSpaceTypes()
      await getVenueChargeTypes()
      await getVenueStatuses()
      await getMerchants()
      
      if (isEdit.value) {
        await getVenueDetail(route.params.id)
      }
    })

    return {
      formRef,
      form,
      rules,
      loading,
      isEdit,
      venueTypes,
      venueSpaceTypes,
      venueChargeTypes,
      venueStatuses,
      merchants,
      handleSubmit,
      handleCancel
    }
  }
}
</script>

<style scoped>
.venue-form {
  padding: 0;
}
</style> 