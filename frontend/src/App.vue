<script setup>
import AdminStatisticsCharts from './components/AdminStatisticsCharts.vue'
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const baseUrl = 'http://localhost:8080'
const baiduMapAk = import.meta.env.VITE_BAIDU_MAP_AK || ''

const token = ref(localStorage.getItem('car-rental-token') || '')
const currentUser = ref(loadJson('car-rental-user', null))
const authTab = ref('login')
const loading = reactive({ auth: false, cars: false, orders: false, pay: false })
const mapContainer = ref(null)
const mapInstance = ref(null)
const markerOverlays = ref([])
const selectedPoint = reactive({ address: '', longitude: '', latitude: '' })

const loginForm = reactive({ username: 'zhangsan', password: '123456' })
const registerForm = reactive({ username: '', password: '123456', realName: '', phone: '', email: '' })
const carFilters = reactive({ keyword: '', transmission: '', energyType: '', availableOnly: true })
const orderForm = reactive({ stationId: null, startDate: '', endDate: '', remark: '' })
const adminCarForm = reactive({
  brand: '比亚迪',
  model: '',
  plateNumber: '',
  color: '白色',
  transmission: '自动挡',
  energyType: '纯电',
  seatCount: 5,
  dailyPrice: 199,
  deposit: 1000,
  status: 'AVAILABLE',
  stationId: 1,
  coverImage: '',
  description: '',
})
const adminUserForm = reactive({
  username: '',
  password: '123456',
  realName: '',
  phone: '',
  email: '',
  role: 'CUSTOMER',
  status: 1,
})

const overview = ref({})
const cars = ref([])
const orders = ref([])
const stations = ref([])
const users = ref([])
const favorites = ref([])
const paymentConfig = ref({ enabled: false, ready: false, message: '支付宝沙盒未配置' })

const isLoggedIn = computed(() => !!token.value)
const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')
const selectedStation = computed(() => stations.value.find((item) => item.id === orderForm.stationId) || null)
const mapEnabled = computed(() => !!baiduMapAk)

const carStatusMap = {
  AVAILABLE: '可租赁',
  RENTED: '租赁中',
  RESERVED: '已预订',
}

const paymentStatusMap = {
  PAID: '已支付',
  UNPAID: '未支付',
}

const orderStatusMap = {
  PENDING: '待处理',
  BOOKED: '已预订',
  ONGOING: '租赁中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

function loadJson(key, fallback) {
  try {
    return JSON.parse(localStorage.getItem(key) || 'null') ?? fallback
  } catch {
    return fallback
  }
}

function setSession(data) {
  token.value = data.token
  currentUser.value = data.user
  localStorage.setItem('car-rental-token', data.token)
  localStorage.setItem('car-rental-user', JSON.stringify(data.user))
}

function clearSession() {
  token.value = ''
  currentUser.value = null
  localStorage.removeItem('car-rental-token')
  localStorage.removeItem('car-rental-user')
}

async function request(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) }
  if (token.value) {
    headers.Authorization = `Bearer ${token.value}`
  }
  const response = await fetch(`${baseUrl}${path}`, { ...options, headers })
  const contentType = response.headers.get('content-type') || ''
  const result = contentType.includes('application/json') ? await response.json() : await response.text()
  if (typeof result === 'string') return result
  if (result.code !== 200) {
    if (result.code === 401) clearSession()
    throw new Error(result.message || '请求失败')
  }
  return result.data
}

async function handleLogin() {
  loading.auth = true
  try {
    const data = await request('/api/auth/login', { method: 'POST', body: JSON.stringify(loginForm) })
    setSession(data)
    ElMessage.success('登录成功')
    await bootstrapData()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.auth = false
  }
}

async function handleRegister() {
  loading.auth = true
  try {
    await request('/api/auth/register', { method: 'POST', body: JSON.stringify(registerForm) })
    authTab.value = 'login'
    loginForm.username = registerForm.username
    ElMessage.success('注册成功，请登录')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.auth = false
  }
}

async function loadCars() {
  loading.cars = true
  try {
    const query = new URLSearchParams()
    Object.entries(carFilters).forEach(([key, value]) => {
      if (value !== '' && value !== null && value !== undefined) query.set(key, value)
    })
    cars.value = await request(`/api/cars?${query.toString()}`)
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.cars = false
  }
}

async function loadOrders() {
  loading.orders = true
  try {
    orders.value = await request(isAdmin.value ? '/api/orders' : '/api/orders/my')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.orders = false
  }
}

async function loadStations() {
  stations.value = await request('/api/stations')
  await nextTick()
  await initMapIfNeeded()
}

async function loadOverview() {
  overview.value = await request('/api/dashboard/overview')
}

async function loadUsers() {
  users.value = await request('/api/users')
}

async function loadFavorites() {
  favorites.value = await request('/api/interactions/favorites')
}

async function loadPaymentConfig() {
  paymentConfig.value = await request('/api/pay/alipay/config')
}

async function bootstrapData() {
  if (!isLoggedIn.value) return
  const tasks = [loadCars(), loadOrders(), loadStations()]
  if (isAdmin.value) tasks.push(loadOverview(), loadUsers())
  else tasks.push(loadFavorites(), loadPaymentConfig())
  await Promise.all(tasks)
}

function shouldShowPayButton(order) {
  return !isAdmin.value && order.orderStatus !== 'CANCELLED'
}

function canPayOrder(order) {
  return shouldShowPayButton(order) && order.paymentStatus !== 'PAID' && order.orderStatus !== 'COMPLETED' && paymentConfig.value.ready
}

function getPayButtonText(order) {
  if (order.paymentStatus === 'PAID') return '已支付'
  if (!paymentConfig.value.ready) return '待配置支付'
  if (order.orderStatus === 'COMPLETED') return '订单已完成'
  return '支付宝沙盒支付'
}

function isFavorite(carId) {
  return favorites.value.some((item) => item.carId === carId)
}

function getDisplayPayButtonText(order) {
  if (!paymentConfig.value.ready) return '\u5f85\u914d\u7f6e\u652f\u4ed8'
  if (order.paymentStatus === 'PAID') return '\u5df2\u652f\u4ed8'
  if (order.orderStatus === 'COMPLETED') return '\u8ba2\u5355\u5df2\u5b8c\u6210'
  return '\u652f\u4ed8'
}

function getCarStatusText(status) {
  return carStatusMap[status] || status
}

function getPaymentStatusText(status) {
  return paymentStatusMap[status] || status
}

function getOrderStatusText(status) {
  return orderStatusMap[status] || status
}

function canCancelOrder(order) {
  if (isAdmin.value) return order.orderStatus !== 'CANCELLED' && order.orderStatus !== 'COMPLETED'
  return order.orderStatus === 'BOOKED' || order.orderStatus === 'PENDING'
}

function canReturnOrder(order) {
  if (isAdmin.value) return order.orderStatus !== 'COMPLETED' && order.orderStatus !== 'CANCELLED'
  return order.paymentStatus === 'PAID' && (order.orderStatus === 'BOOKED' || order.orderStatus === 'ONGOING')
}

async function toggleFavorite(carId, favored) {
  try {
    await request(`/api/interactions/favorites/${carId}`, { method: favored ? 'DELETE' : 'POST' })
    ElMessage.success(favored ? '已取消收藏' : '已加入收藏')
    await loadFavorites()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function createOrder(carId) {
  if (!orderForm.stationId || !orderForm.startDate || !orderForm.endDate) {
    ElMessage.warning('请先选择取车点和租赁日期')
    return
  }
  if (!selectedPoint.longitude || !selectedPoint.latitude) {
    ElMessage.warning('请先在地图上选择取车位置')
    return
  }
  try {
    await request('/api/orders', {
      method: 'POST',
      body: JSON.stringify({
        ...orderForm,
        carId,
        pickupAddress: selectedPoint.address,
        pickupLongitude: selectedPoint.longitude,
        pickupLatitude: selectedPoint.latitude,
      }),
    })
    ElMessage.success('订单创建成功，请尽快支付')
    await Promise.all([loadCars(), loadOrders()])
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function payOrder(orderId) {
  if (!paymentConfig.value.ready) {
    ElMessage.warning('支付宝沙盒尚未配置，当前无法发起支付')
    return
  }
  loading.pay = true
  try {
    const formHtml = await request(`/api/pay/alipay/${orderId}/form`)
    const payWindow = window.open('', '_blank')
    if (!payWindow) {
      throw new Error('浏览器拦截了支付窗口，请允许弹窗后重试')
    }
    payWindow.document.open()
    payWindow.document.write(formHtml)
    payWindow.document.close()
    ElMessage.success('已打开支付宝沙盒支付页面')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.pay = false
  }
}

async function operateOrder(orderId, action, admin = false) {
  try {
    await request(`/api/orders/${orderId}${admin ? `/admin/${action}` : `/${action}`}`, { method: 'POST' })
    ElMessage.success('操作成功')
    await Promise.all([loadCars(), loadOrders(), isAdmin.value ? loadOverview() : Promise.resolve()])
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function createCar() {
  try {
    await request('/api/cars', { method: 'POST', body: JSON.stringify(adminCarForm) })
    ElMessage.success('车辆已新增')
    await Promise.all([loadCars(), loadOverview()])
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function createUser() {
  try {
    await request('/api/users', { method: 'POST', body: JSON.stringify(adminUserForm) })
    ElMessage.success('用户已新增')
    await Promise.all([loadUsers(), loadOverview()])
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function logout() {
  clearSession()
  overview.value = {}
  cars.value = []
  orders.value = []
  users.value = []
  favorites.value = []
  selectedPoint.address = ''
  selectedPoint.longitude = ''
  selectedPoint.latitude = ''
}

function loadBaiduMapScript() {
  if (window.BMap) {
    return Promise.resolve(window.BMap)
  }
  if (!baiduMapAk) {
    return Promise.reject(new Error('未配置百度地图 AK，请在 frontend/.env 中设置 VITE_BAIDU_MAP_AK'))
  }
  return new Promise((resolve, reject) => {
    const callbackName = `initBaiduMap_${Date.now()}`
    window[callbackName] = () => {
      resolve(window.BMap)
      delete window[callbackName]
    }
    const script = document.createElement('script')
    script.src = `https://api.map.baidu.com/api?v=3.0&ak=${baiduMapAk}&callback=${callbackName}`
    script.onerror = () => reject(new Error('百度地图脚本加载失败'))
    document.body.appendChild(script)
  })
}

async function initMapIfNeeded() {
  if (!isLoggedIn.value || isAdmin.value || !mapContainer.value || stations.value.length === 0) return
  if (!mapEnabled.value) return
  try {
    const BMap = await loadBaiduMapScript()
    if (!mapInstance.value) {
      const map = new BMap.Map(mapContainer.value)
      const firstStation = stations.value[0]
      map.centerAndZoom(new BMap.Point(Number(firstStation.longitude), Number(firstStation.latitude)), 12)
      map.enableScrollWheelZoom(true)
      map.addControl(new BMap.NavigationControl())
      map.addEventListener('click', (event) => handleMapPointSelect(BMap, event.point))
      mapInstance.value = map
    }
    renderStationMarkers(BMap)
  } catch (error) {
    ElMessage.warning(error.message)
  }
}

function renderStationMarkers(BMap) {
  if (!mapInstance.value) return
  markerOverlays.value.forEach((overlay) => mapInstance.value.removeOverlay(overlay))
  markerOverlays.value = []
  stations.value.forEach((station) => {
    const point = new BMap.Point(Number(station.longitude), Number(station.latitude))
    const marker = new BMap.Marker(point)
    marker.addEventListener('click', () => {
      orderForm.stationId = station.id
      setSelectedPoint({
        address: `${station.city}${station.address}`,
        longitude: station.longitude,
        latitude: station.latitude,
      })
    })
    mapInstance.value.addOverlay(marker)
    markerOverlays.value.push(marker)
  })
}

function handleMapPointSelect(BMap, point) {
  const geocoder = new BMap.Geocoder()
  geocoder.getLocation(point, (result) => {
    const nearestStation = findNearestStation(point.lng, point.lat)
    if (nearestStation) {
      orderForm.stationId = nearestStation.id
    }
    setSelectedPoint({
      address: result?.address || `经度 ${point.lng}，纬度 ${point.lat}`,
      longitude: String(point.lng),
      latitude: String(point.lat),
    })
  })
}

function findNearestStation(lng, lat) {
  if (stations.value.length === 0) return null
  return stations.value
    .map((station) => ({
      station,
      distance: Math.hypot(Number(station.longitude) - lng, Number(station.latitude) - lat),
    }))
    .sort((a, b) => a.distance - b.distance)[0]?.station || null
}

function setSelectedPoint(point) {
  selectedPoint.address = point.address
  selectedPoint.longitude = point.longitude
  selectedPoint.latitude = point.latitude
}

watch(
  () => orderForm.stationId,
  (value) => {
    const station = stations.value.find((item) => item.id === value)
    if (!station || selectedPoint.address) return
    setSelectedPoint({
      address: `${station.city}${station.address}`,
      longitude: station.longitude,
      latitude: station.latitude,
    })
  }
)

onMounted(async () => {
  if (isLoggedIn.value) {
    await bootstrapData()
  }
})
</script>

<template>
  <div class="page-shell">
    <section class="hero-panel">
      <div>
        <h1>&#27773;&#36710;&#31199;&#36161;&#31649;&#29702;&#31995;&#32479;</h1>
      </div>
      <div class="auth-panel">
        <template v-if="!isLoggedIn">
          <el-tabs v-model="authTab">
            <el-tab-pane label="登录" name="login">
              <el-form label-position="top">
                <el-form-item label="用户名"><el-input v-model="loginForm.username" /></el-form-item>
                <el-form-item label="密码"><el-input v-model="loginForm.password" show-password /></el-form-item>
                <el-button class="wide-btn" type="primary" :loading="loading.auth" @click="handleLogin">登录系统</el-button>
                <p class="tip">演示账号：zhangsan / 123456，管理员：admin / 123456</p>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="注册" name="register">
              <el-form label-position="top">
                <el-form-item label="用户名"><el-input v-model="registerForm.username" /></el-form-item>
                <el-form-item label="密码"><el-input v-model="registerForm.password" show-password /></el-form-item>
                <el-form-item label="姓名"><el-input v-model="registerForm.realName" /></el-form-item>
                <el-form-item label="手机号"><el-input v-model="registerForm.phone" /></el-form-item>
                <el-form-item label="邮箱"><el-input v-model="registerForm.email" /></el-form-item>
                <el-button class="wide-btn" type="success" :loading="loading.auth" @click="handleRegister">创建账户</el-button>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </template>
        <template v-else>
          <div class="profile-card">
            <div>
              <div class="profile-name">{{ currentUser?.realName || currentUser?.username }}</div>
              <div class="profile-role">{{ isAdmin ? '管理员' : '租赁用户' }}</div>
            </div>
            <el-button text @click="logout">退出</el-button>
          </div>
          <div class="mini-stats">
            <div><strong>{{ orders.length }}</strong><span>订单</span></div>
            <div><strong>{{ cars.length }}</strong><span>车辆</span></div>
            <div><strong>{{ stations.length }}</strong><span>站点</span></div>
          </div>
        </template>
      </div>
    </section>

    <template v-if="isLoggedIn">
      <section v-if="isAdmin" class="section-block">
        <div class="section-head">
          <h2>运营总览</h2>
          <el-button @click="bootstrapData">刷新</el-button>
        </div>
        <div class="stats-grid">
          <div class="stats-card"><span>用户总数</span><strong>{{ overview.userCount || 0 }}</strong></div>
          <div class="stats-card"><span>车辆总数</span><strong>{{ overview.carCount || 0 }}</strong></div>
          <div class="stats-card"><span>可租车辆</span><strong>{{ overview.availableCarCount || 0 }}</strong></div>
          <div class="stats-card"><span>租金收入</span><strong>¥ {{ overview.income || 0 }}</strong></div>
        </div>
      </section>

      <AdminStatisticsCharts v-if="isAdmin && token" :token="token" :base-url="baseUrl" />

      <section class="section-block">
        <div class="section-head"><h2>{{ isAdmin ? '车辆管理' : '车辆大厅' }}</h2></div>
        <div class="toolbar-grid">
          <el-input v-model="carFilters.keyword" placeholder="品牌 / 型号 / 车牌" clearable />
          <el-select v-model="carFilters.transmission" placeholder="变速箱" clearable>
            <el-option label="自动挡" value="自动挡" />
            <el-option label="手动挡" value="手动挡" />
          </el-select>
          <el-select v-model="carFilters.energyType" placeholder="能源类型" clearable>
            <el-option label="燃油" value="燃油" />
            <el-option label="混动" value="混动" />
            <el-option label="纯电" value="纯电" />
          </el-select>
          <el-switch v-model="carFilters.availableOnly" active-text="仅看可租" />
          <el-button type="primary" :loading="loading.cars" @click="loadCars">筛选</el-button>
        </div>
        <div class="car-grid">
          <article v-for="car in cars" :key="car.id" class="car-card">
            <img :src="car.coverImage || 'https://via.placeholder.com/640x360?text=Car'" :alt="car.model" />
            <div class="car-body">
              <div class="card-top">
                <div>
                  <h3>{{ car.brand }} {{ car.model }}</h3>
                  <p>{{ car.plateNumber }} · {{ car.energyType }} · {{ car.transmission }}</p>
                </div>
                <el-tag :type="car.status === 'AVAILABLE' ? 'success' : car.status === 'RENTED' ? 'danger' : 'warning'">{{ getCarStatusText(car.status) }}</el-tag>
              </div>
              <p class="desc">{{ car.description }}</p>
              <div class="car-meta">
                <strong>¥ {{ car.dailyPrice }}/天</strong>
                <span>押金 ¥ {{ car.deposit }}</span>
              </div>
              <div v-if="!isAdmin" class="rent-actions">
                <el-button plain @click="toggleFavorite(car.id, isFavorite(car.id))">{{ isFavorite(car.id) ? '取消收藏' : '加入收藏' }}</el-button>
                <el-button type="primary" :disabled="car.status !== 'AVAILABLE'" @click="createOrder(car.id)">选择此车下单</el-button>
              </div>
            </div>
          </article>
        </div>
      </section>

      <section v-if="!isAdmin" class="section-block">
        <div class="section-head"><h2>地图选点下单</h2></div>
        <div class="toolbar-grid">
          <el-select v-model="orderForm.stationId" placeholder="系统取车点">
            <el-option v-for="station in stations" :key="station.id" :label="`${station.city} · ${station.name}`" :value="station.id" />
          </el-select>
          <el-date-picker v-model="orderForm.startDate" type="date" value-format="YYYY-MM-DD" placeholder="取车日期" />
          <el-date-picker v-model="orderForm.endDate" type="date" value-format="YYYY-MM-DD" placeholder="还车日期" />
          <el-input v-model="orderForm.remark" placeholder="备注信息" />
        </div>
        <div class="map-panel">
          <div v-if="mapEnabled" ref="mapContainer" class="map-canvas"></div>
          <div v-else class="map-canvas map-fallback">
            <h3>站点下单模式</h3>
            <p>当前使用系统取车点下单。</p>
            <p>选择上方取车点后，系统会自动填充取车位置。</p>
          </div>
          <div class="selected-point">
            <h3>当前选中的地图位置</h3>
            <p>地址：{{ selectedPoint.address || (mapEnabled ? '请在地图上点击选择取车位置' : '请先选择系统取车点') }}</p>
            <p>经度：{{ selectedPoint.longitude || '-' }}</p>
            <p>纬度：{{ selectedPoint.latitude || '-' }}</p>
            <p>关联站点：{{ selectedStation ? `${selectedStation.city} · ${selectedStation.name}` : '未选择' }}</p>
            <p class="tip">
              {{ mapEnabled ? '提示：点击站点标记会直接选择该站点；点击地图空白位置会自动匹配最近取车点。' : '提示：当前为降级模式，选择系统取车点后会自动使用站点经纬度。' }}
            </p>
          </div>
        </div>
      </section>

      <section class="section-block">
        <div class="section-head"><h2>{{ isAdmin ? '订单管理' : '我的订单' }}</h2></div>
        <p v-if="!isAdmin && !paymentConfig.ready" class="tip">支付宝沙盒未配置，支付按钮已禁用。配置完成后刷新页面即可启用。</p>
        <el-table :data="orders" stripe>
          <el-table-column v-if="isAdmin" prop="orderNo" label="&#35746;&#21333;&#21495;" min-width="180" />
          <el-table-column prop="pickupAddress" label="取车地址" min-width="220" />
          <el-table-column prop="rentalDays" label="天数" width="90" />
          <el-table-column prop="rentAmount" label="租金" width="100" />
          <el-table-column prop="paymentStatus" label="支付状态" width="120">
            <template #default="{ row }">
              {{ getPaymentStatusText(row.paymentStatus) }}
            </template>
          </el-table-column>
          <el-table-column prop="orderStatus" label="订单状态" width="120">
            <template #default="{ row }">
              {{ getOrderStatusText(row.orderStatus) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="340">
            <template #default="{ row }">
              <el-space wrap>
                <el-button
                  v-if="shouldShowPayButton(row)"
                  size="small"
                  type="success"
                  :disabled="!canPayOrder(row)"
                  :loading="loading.pay"
                  @click="payOrder(row.id)"
                >
                  {{ getDisplayPayButtonText(row) }}
                </el-button>
                <el-button v-if="isAdmin" size="small" type="success" @click="operateOrder(row.id, 'pay', true)" :disabled="row.paymentStatus === 'PAID'">后台设为已支付</el-button>
                <el-button size="small" type="warning" @click="operateOrder(row.id, isAdmin ? 'cancel' : 'cancel', isAdmin)" :disabled="!canCancelOrder(row)">取消</el-button>
                <el-button size="small" type="primary" @click="operateOrder(row.id, isAdmin ? 'return' : 'return', isAdmin)" :disabled="!canReturnOrder(row)">归还</el-button>
              </el-space>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="isAdmin" class="section-block admin-grid">
        <div class="admin-card">
          <div class="section-head"><h2>新增车辆</h2></div>
          <div class="toolbar-grid compact">
            <el-input v-model="adminCarForm.brand" placeholder="品牌" />
            <el-input v-model="adminCarForm.model" placeholder="型号" />
            <el-input v-model="adminCarForm.plateNumber" placeholder="车牌号" />
            <el-input-number v-model="adminCarForm.dailyPrice" :min="100" />
            <el-input-number v-model="adminCarForm.deposit" :min="500" />
            <el-select v-model="adminCarForm.transmission">
              <el-option label="自动挡" value="自动挡" />
              <el-option label="手动挡" value="手动挡" />
            </el-select>
            <el-select v-model="adminCarForm.energyType">
              <el-option label="燃油" value="燃油" />
              <el-option label="混动" value="混动" />
              <el-option label="纯电" value="纯电" />
            </el-select>
            <el-select v-model="adminCarForm.stationId">
              <el-option v-for="station in stations" :key="station.id" :label="station.name" :value="station.id" />
            </el-select>
            <el-input v-model="adminCarForm.coverImage" placeholder="图片链接" />
            <el-input v-model="adminCarForm.description" placeholder="车辆说明" />
          </div>
          <el-button type="primary" @click="createCar">保存车辆</el-button>
        </div>

        <div class="admin-card">
          <div class="section-head"><h2>新增用户</h2></div>
          <div class="toolbar-grid compact">
            <el-input v-model="adminUserForm.username" placeholder="用户名" />
            <el-input v-model="adminUserForm.realName" placeholder="姓名" />
            <el-input v-model="adminUserForm.phone" placeholder="手机号" />
            <el-input v-model="adminUserForm.email" placeholder="邮箱" />
            <el-select v-model="adminUserForm.role">
              <el-option label="租赁用户" value="CUSTOMER" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
          </div>
          <el-button type="success" @click="createUser">保存用户</el-button>
        </div>
      </section>

      <section v-if="isAdmin" class="section-block">
        <div class="section-head"><h2>用户列表</h2></div>
        <el-table :data="users" stripe>
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="realName" label="姓名" />
          <el-table-column prop="phone" label="手机号" />
          <el-table-column prop="role" label="角色" />
          <el-table-column prop="status" label="状态" />
        </el-table>
      </section>
    </template>
  </div>
</template>
