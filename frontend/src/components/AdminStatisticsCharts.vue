<script setup>
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps({
  token: {
    type: String,
    default: '',
  },
  baseUrl: {
    type: String,
    required: true,
  },
})

const incomeDimension = ref('day')
const chartData = ref({
  energyDistribution: [],
  orderStatusDistribution: [],
  incomeTrendByDay: [],
  incomeTrendByMonth: [],
  incomeTrendByYear: [],
})

const orderStatusLabelMap = {
  PENDING: '待处理',
  BOOKED: '已预订',
  ONGOING: '租赁中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

const energyChartRef = ref(null)
const orderChartRef = ref(null)
const incomeChartRef = ref(null)
let energyChart = null
let orderChart = null
let incomeChart = null

async function request(path) {
  const response = await fetch(`${props.baseUrl}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${props.token}`,
    },
  })
  const result = await response.json()
  if (result.code !== 200) {
    throw new Error(result.message || '加载图表数据失败')
  }
  return result.data
}

async function loadCharts() {
  try {
    chartData.value = await request('/api/dashboard/charts')
    await nextTick()
    initCharts()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function initCharts() {
  renderEnergyChart()
  renderOrderChart()
  renderIncomeChart()
}

function getLocalizedOrderStatusData() {
  return (chartData.value.orderStatusDistribution || []).map((item) => ({
    ...item,
    name: orderStatusLabelMap[item.name] || item.name,
  }))
}

function renderEnergyChart() {
  if (!energyChartRef.value) return
  energyChart?.dispose()
  energyChart = echarts.init(energyChartRef.value)
  energyChart.setOption({
    title: { text: '车辆能源类型分布', left: 'center', textStyle: { fontSize: 16 } },
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        name: '车辆数量',
        type: 'pie',
        radius: ['40%', '68%'],
        data: chartData.value.energyDistribution || [],
      },
    ],
  })
}

function renderOrderChart() {
  if (!orderChartRef.value) return
  orderChart?.dispose()
  orderChart = echarts.init(orderChartRef.value)
  const localizedData = getLocalizedOrderStatusData()
  orderChart.setOption({
    title: { text: '订单状态统计', left: 'center', textStyle: { fontSize: 16 } },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: localizedData.map((item) => item.name),
    },
    yAxis: { type: 'value', name: '订单数' },
    series: [
      {
        name: '订单数',
        data: localizedData.map((item) => item.value),
        type: 'bar',
        barWidth: 36,
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
          color: '#d97706',
        },
      },
    ],
  })
}

function renderIncomeChart() {
  if (!incomeChartRef.value) return
  incomeChart?.dispose()
  incomeChart = echarts.init(incomeChartRef.value)
  const currentData = getIncomeTrendData()
  incomeChart.setOption({
    title: { text: getIncomeTitle(), left: 'center', textStyle: { fontSize: 16 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 24, top: 64, bottom: 40 },
    xAxis: {
      type: 'category',
      data: currentData.map((item) => item.label),
      axisLabel: {
        rotate: incomeDimension.value === 'day' ? 35 : 0,
      },
    },
    yAxis: { type: 'value', name: '租金收入（元）' },
    series: [
      {
        name: '租金收入',
        data: currentData.map((item) => item.value),
        type: 'bar',
        barWidth: 22,
        barCategoryGap: '55%',
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
          color: '#2563eb',
        },
      },
    ],
  })
}

function getIncomeTrendData() {
  if (incomeDimension.value === 'year') return chartData.value.incomeTrendByYear || []
  if (incomeDimension.value === 'month') return chartData.value.incomeTrendByMonth || []
  return chartData.value.incomeTrendByDay || []
}

function getIncomeTitle() {
  if (incomeDimension.value === 'year') return '租金收入按年统计'
  if (incomeDimension.value === 'month') return '租金收入按月统计'
  return '租金收入按日统计'
}

function handleDimensionChange(value) {
  incomeDimension.value = value
  renderIncomeChart()
}

function resizeCharts() {
  energyChart?.resize()
  orderChart?.resize()
  incomeChart?.resize()
}

onMounted(() => {
  loadCharts()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  energyChart?.dispose()
  orderChart?.dispose()
  incomeChart?.dispose()
})
</script>

<template>
  <section class="section-block">
    <div class="section-head">
      <h2>图表统计</h2>
    </div>
    <div class="chart-grid">
      <div ref="energyChartRef" class="chart-card"></div>
      <div ref="orderChartRef" class="chart-card"></div>
    </div>
    <div class="section-head chart-toolbar">
      <h2>租金收入趋势</h2>
      <el-radio-group :model-value="incomeDimension" @update:model-value="handleDimensionChange">
        <el-radio-button label="day">按日</el-radio-button>
        <el-radio-button label="month">按月</el-radio-button>
        <el-radio-button label="year">按年</el-radio-button>
      </el-radio-group>
    </div>
    <div ref="incomeChartRef" class="chart-card chart-card-wide"></div>
  </section>
</template>

<style scoped>
.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.chart-card {
  min-height: 360px;
  border-radius: 20px;
  background: #f8fafc;
  border: 1px solid rgba(24, 33, 47, 0.08);
}

.chart-card-wide {
  min-height: 420px;
}

.chart-toolbar {
  margin-bottom: 16px;
}

@media (max-width: 1024px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }
}
</style>
