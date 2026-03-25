<script setup>
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

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

const pieRef = ref(null)
const barRef = ref(null)
const incomeRef = ref(null)
const incomeMode = ref('month')
const chartData = ref({
  energyDistribution: [],
  orderStatusDistribution: [],
  incomeTrendByDay: [],
  incomeTrendByMonth: [],
  incomeTrendByYear: [],
})

let pieChart
let barChart
let incomeChart

const orderStatusText = {
  PENDING: '待处理',
  BOOKED: '已预订',
  ONGOING: '租赁中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

async function loadCharts() {
  try {
    const response = await fetch(`${props.baseUrl}/api/dashboard/charts`, {
      headers: {
        Authorization: `Bearer ${props.token}`,
      },
    })
    const result = await response.json()
    if (result.code !== 200) {
      throw new Error(result.message || '加载统计数据失败')
    }
    chartData.value = result.data
    await nextTick()
    renderCharts()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function currentIncomeSeries() {
  if (incomeMode.value === 'day') return chartData.value.incomeTrendByDay || []
  if (incomeMode.value === 'year') return chartData.value.incomeTrendByYear || []
  return chartData.value.incomeTrendByMonth || []
}

function currentIncomeTitle() {
  if (incomeMode.value === 'day') return '租金收入按日统计'
  if (incomeMode.value === 'year') return '租金收入按年统计'
  return '租金收入按月统计'
}

function initCharts() {
  if (!pieChart && pieRef.value) pieChart = echarts.init(pieRef.value)
  if (!barChart && barRef.value) barChart = echarts.init(barRef.value)
  if (!incomeChart && incomeRef.value) incomeChart = echarts.init(incomeRef.value)
}

function renderCharts() {
  initCharts()
  if (!pieChart || !barChart || !incomeChart) return

  pieChart.setOption({
    title: { text: '车辆能源类型分布', left: 'center', textStyle: { fontSize: 16 } },
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 3 },
        label: { formatter: '{b} {d}%' },
        data: chartData.value.energyDistribution || [],
      },
    ],
  })

  barChart.setOption({
    title: { text: '订单状态统计', left: 'center', textStyle: { fontSize: 16 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, bottom: 30, top: 56 },
    xAxis: {
      type: 'category',
      data: (chartData.value.orderStatusDistribution || []).map((item) => orderStatusText[item.name] || item.name),
      axisLabel: { interval: 0 },
    },
    yAxis: { type: 'value', name: '订单数' },
    series: [
      {
        type: 'bar',
        data: (chartData.value.orderStatusDistribution || []).map((item) => item.value),
        barWidth: 26,
        itemStyle: { color: '#2f6fed', borderRadius: [8, 8, 0, 0] },
      },
    ],
  })

  const incomeSeries = currentIncomeSeries()
  incomeChart.setOption({
    title: { text: currentIncomeTitle(), left: 'center', textStyle: { fontSize: 16 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, bottom: 40, top: 56 },
    xAxis: {
      type: 'category',
      data: incomeSeries.map((item) => item.label),
      axisLabel: { rotate: incomeMode.value === 'day' ? 30 : 0 },
    },
    yAxis: { type: 'value', name: '收入（元）' },
    series: [
      {
        type: 'bar',
        data: incomeSeries.map((item) => item.value),
        barWidth: 22,
        barCategoryGap: '55%',
        itemStyle: { color: '#ef7d22', borderRadius: [8, 8, 0, 0] },
      },
    ],
  })
}

function resizeCharts() {
  pieChart?.resize()
  barChart?.resize()
  incomeChart?.resize()
}

watch(() => props.token, () => {
  if (props.token) loadCharts()
})

watch(incomeMode, () => {
  renderCharts()
})

onMounted(async () => {
  window.addEventListener('resize', resizeCharts)
  if (props.token) {
    await loadCharts()
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  pieChart?.dispose()
  barChart?.dispose()
  incomeChart?.dispose()
})
</script>

<template>
  <section class="chart-shell">
    <div class="chart-card">
      <div ref="pieRef" class="chart-box"></div>
    </div>
    <div class="chart-card">
      <div ref="barRef" class="chart-box"></div>
    </div>
    <div class="chart-card chart-card-wide">
      <div class="chart-toolbar">
        <span>租金收入趋势</span>
        <el-radio-group v-model="incomeMode" size="small">
          <el-radio-button label="day">按日</el-radio-button>
          <el-radio-button label="month">按月</el-radio-button>
          <el-radio-button label="year">按年</el-radio-button>
        </el-radio-group>
      </div>
      <div ref="incomeRef" class="chart-box chart-box-wide"></div>
    </div>
  </section>
</template>

<style scoped>
.chart-shell {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.chart-card {
  padding: 18px;
  background: #fff;
  border: 1px solid rgba(20, 36, 56, 0.08);
  border-radius: 20px;
  box-shadow: 0 14px 28px rgba(28, 54, 90, 0.06);
}

.chart-card-wide {
  grid-column: 1 / -1;
}

.chart-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #223044;
  font-weight: 600;
}

.chart-box {
  height: 320px;
}

.chart-box-wide {
  height: 360px;
}

@media (max-width: 900px) {
  .chart-shell {
    grid-template-columns: 1fr;
  }
}
</style>
