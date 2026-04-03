<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import AdminStatisticsCharts from './components/AdminStatisticsCharts.vue'

const baseUrl = 'http://localhost:8080'
const baiduMapAk = import.meta.env.VITE_BAIDU_MAP_AK || ''

const token = ref(localStorage.getItem('car-rental-token') || '')
const currentUser = ref(loadJson('car-rental-user', null))
const authTab = ref('login')
const userView = ref('home')
const adminView = ref('system-overview')
const selectedCar = ref(null)

const loading = reactive({ auth: false, cars: false, orders: false, save: false, pay: false, bannerUpload: false, profileUpload: false, carUpload: false })
const loginForm = reactive({ username: 'zhangsan', password: '123456' })
const registerForm = reactive({ username: '', password: '', realName: '', phone: '', email: '' })
const profileForm = reactive({ realName: '', phone: '', email: '', idCardNo: '', idCardFront: '', idCardBack: '', verifyStatus: 'PENDING', verifyRemark: '' })
const carFilters = reactive({ brand: '', typeName: '', transmission: '', energyType: '', sortBy: 'hot', availableOnly: true })
const rentalForm = reactive({ pickupStationId: null, returnStationId: null, startDate: '', endDate: '', paymentMethod: 'ALIPAY', remark: '' })
const locationTarget = ref('pickup')
const pickupPoint = reactive({ address: '', longitude: '', latitude: '' })
const returnPoint = reactive({ address: '', longitude: '', latitude: '' })

const publicHome = ref({ banners: [], notices: [], hotCars: [], brands: [], types: [] })
const overview = ref({})
const cars = ref([])
const orders = ref([])
const stations = ref([])
const notices = ref([])
const banners = ref([])
const users = ref([])
const feedbacks = ref([])
const favorites = ref([])
const brands = ref([])
const types = ref([])
const paymentConfig = ref({ enabled: false, ready: false, message: '支付服务未配置' })

const bannerForm = reactive({ title: '', imageUrl: '', linkUrl: '', sort: 1, status: 1 })
const noticeForm = reactive({ title: '', content: '', imageUrl: '', sort: 1, status: 1 })
const feedbackForm = reactive({ title: '', content: '' })
const stationForm = reactive({ name: '', city: '上海', address: '', contactPhone: '', longitude: '', latitude: '', businessHours: '09:00-21:00', description: '', status: 1 })
const brandForm = reactive({ name: '', sort: 1, status: 1 })
const typeForm = reactive({ name: '', sort: 1, status: 1 })
const carForm = reactive({ brand: '', typeName: '', model: '', plateNumber: '', color: '白色', transmission: '自动挡', energyType: '纯电', seatCount: 5, dailyPrice: 200, deposit: 1000, status: 'AVAILABLE', stationId: null, hotFlag: 0, coverImage: '', description: '' })
const userForm = reactive({ username: '', password: '123456', realName: '', phone: '', email: '', role: 'CUSTOMER', status: 1, verifyStatus: 'PENDING', verifyRemark: '' })
const replyDrafts = reactive({})
const dialogVisible = reactive({ banner: false, notice: false, station: false, brand: false, type: false, car: false, admin: false })
const editingId = reactive({ banner: null, notice: null, station: null, brand: null, type: null, car: null, admin: null })
const defaultBannerForm = { title: '', imageUrl: '', linkUrl: '', sort: 1, status: 1 }
const defaultNoticeForm = { title: '', content: '', imageUrl: '', sort: 1, status: 1 }
const defaultStationForm = { name: '', city: '上海', address: '', contactPhone: '', longitude: '', latitude: '', businessHours: '09:00-21:00', description: '', status: 1 }
const defaultBrandForm = { name: '', sort: 1, status: 1 }
const defaultTypeForm = { name: '', sort: 1, status: 1 }
const defaultCarForm = { brand: '', typeName: '', model: '', plateNumber: '', color: '白色', transmission: '自动挡', energyType: '纯电', seatCount: 5, dailyPrice: 200, deposit: 1000, status: 'AVAILABLE', stationId: null, hotFlag: 0, coverImage: '', description: '' }
const defaultUserForm = { username: '', password: '123456', realName: '', phone: '', email: '', role: 'ADMIN', status: 1, verifyStatus: 'APPROVED', verifyRemark: '' }

const mapContainer = ref(null)
const mapInstance = ref(null)
const markerOverlays = ref([])

const adminMenuGroups = [
  { index: 'system', title: '系统首页', children: [{ index: 'system-overview', title: '系统总览' }, { index: 'system-banners', title: '上传轮播图' }] },
  { index: 'info', title: '信息管理', children: [{ index: 'info-cars', title: '车辆管理' }, { index: 'info-orders', title: '订单管理' }, { index: 'info-feedbacks', title: '问答管理' }, { index: 'info-stations', title: '营业网点管理' }, { index: 'info-brands', title: '品牌管理' }, { index: 'info-types', title: '类型管理' }, { index: 'info-notices', title: '系统公告' }] },
  { index: 'users', title: '用户管理', children: [{ index: 'users-admins', title: '管理员信息' }, { index: 'users-customers', title: '用户信息' }] },
]

const userNavItems = [
  { key: 'home', label: '首页' },
  { key: 'rent', label: '租赁汽车' },
  { key: 'catalog', label: '车型查询' },
  { key: 'payment', label: '下单支付' },
  { key: 'orders', label: '我的订单' },
  { key: 'favorites', label: '收藏车辆' },
  { key: 'profile', label: '用户信息' },
  { key: 'feedback', label: '提出反馈' },
  { key: 'notices', label: '系统公告' },
]

const carStatusText = { AVAILABLE: '可租赁', RENTED: '租赁中', RESERVED: '已预订', MAINTENANCE: '维护中', DISABLED: '已停用' }
const paymentStatusText = { UNPAID: '未支付', PAID: '已支付' }
const orderStatusText = { PENDING: '待处理', BOOKED: '已预订', ONGOING: '租赁中', COMPLETED: '已完成', CANCELLED: '已取消' }
const verifyStatusText = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' }
const feedbackStatusText = { PENDING: '待回复', REPLIED: '已回复' }

const isLoggedIn = computed(() => !!token.value)
const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')
const favoriteCars = computed(() => cars.value.filter((car) => favorites.value.some((item) => item.carId === car.id)))
const selectedPickupStation = computed(() => stations.value.find((item) => item.id === rentalForm.pickupStationId) || null)
const selectedReturnStation = computed(() => stations.value.find((item) => item.id === rentalForm.returnStationId) || null)
const adminList = computed(() => users.value.filter((item) => item.role === 'ADMIN'))
const customerList = computed(() => users.value.filter((item) => item.role === 'CUSTOMER'))
const mapEnabled = computed(() => !!baiduMapAk)
const rentalSummary = computed(() => {
  if (!selectedCar.value || !rentalForm.startDate || !rentalForm.endDate) {
    return { days: 0, rentAmount: 0, deposit: Number(selectedCar.value?.deposit || 0), total: Number(selectedCar.value?.deposit || 0) }
  }
  const start = new Date(rentalForm.startDate)
  const end = new Date(rentalForm.endDate)
  const days = Math.max(0, Math.floor((end - start) / 86400000) + 1)
  const rentAmount = days * Number(selectedCar.value.dailyPrice || 0)
  const deposit = Number(selectedCar.value.deposit || 0)
  return { days, rentAmount, deposit, total: rentAmount + deposit }
})

function loadJson(key, fallback) {
  try { return JSON.parse(localStorage.getItem(key) || 'null') ?? fallback } catch { return fallback }
}
function resetObject(target, source) { Object.keys(source).forEach((key) => { target[key] = source[key] }) }
function openDialog(type, record = null) {
  dialogVisible[type] = true
  editingId[type] = record?.id || null
  if (type === 'banner') resetObject(bannerForm, record ? { title: record.title, imageUrl: record.imageUrl, linkUrl: record.linkUrl, sort: record.sort, status: record.status } : defaultBannerForm)
  if (type === 'notice') resetObject(noticeForm, record ? { title: record.title, content: record.content, imageUrl: record.imageUrl, sort: record.sort, status: record.status } : defaultNoticeForm)
  if (type === 'station') resetObject(stationForm, record ? { name: record.name, city: record.city, address: record.address, contactPhone: record.contactPhone, longitude: record.longitude, latitude: record.latitude, businessHours: record.businessHours, description: record.description, status: record.status } : defaultStationForm)
  if (type === 'brand') resetObject(brandForm, record ? { name: record.name, sort: record.sort, status: record.status } : defaultBrandForm)
  if (type === 'type') resetObject(typeForm, record ? { name: record.name, sort: record.sort, status: record.status } : defaultTypeForm)
  if (type === 'car') resetObject(carForm, record ? { brand: record.brand, typeName: record.typeName, model: record.model, plateNumber: record.plateNumber, color: record.color, transmission: record.transmission, energyType: record.energyType, seatCount: record.seatCount, dailyPrice: record.dailyPrice, deposit: record.deposit, status: record.status, stationId: record.stationId, hotFlag: record.hotFlag, coverImage: record.coverImage, description: record.description } : defaultCarForm)
  if (type === 'admin') resetObject(userForm, record ? { username: record.username, password: '', realName: record.realName, phone: record.phone, email: record.email, role: record.role, status: record.status, verifyStatus: record.verifyStatus || 'APPROVED', verifyRemark: record.verifyRemark || '' } : defaultUserForm)
}
function closeDialog(type) {
  dialogVisible[type] = false
  editingId[type] = null
}

async function request(path, options = {}) {
  const headers = { ...(options.headers || {}) }
  if (options.body && !(options.body instanceof FormData)) headers['Content-Type'] = 'application/json'
  if (token.value) headers.Authorization = `Bearer ${token.value}`
  const response = await fetch(`${baseUrl}${path}`, { ...options, headers })
  const contentType = response.headers.get('content-type') || ''
  if (contentType.includes('application/json')) {
    const result = await response.json()
    if (result.code !== 200) {
      if (result.code === 401) logout()
      throw new Error(result.message || '请求失败')
    }
    return result.data
  }
  return response.text()
}

function saveSession(data) {
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

async function loadPublicHome() {
  publicHome.value = await request('/api/public/home')
  brands.value = publicHome.value.brands || []
  types.value = publicHome.value.types || []
}
async function loadCars() {
  loading.cars = true
  try {
    const query = new URLSearchParams()
    Object.entries(carFilters).forEach(([key, value]) => { if (value !== '' && value !== null && value !== undefined) query.set(key, value) })
    cars.value = await request(`/api/cars?${query.toString()}`)
  } finally { loading.cars = false }
}
async function loadOrders() { loading.orders = true; try { orders.value = await request(isAdmin.value ? '/api/orders' : '/api/orders/my') } finally { loading.orders = false } }
async function loadStations() { stations.value = await request('/api/stations'); await nextTick(); await initMap() }
async function loadProfile() {
  const data = await request('/api/users/profile')
  resetObject(profileForm, { realName: data.realName || '', phone: data.phone || '', email: data.email || '', idCardNo: data.idCardNo || '', idCardFront: data.idCardFront || '', idCardBack: data.idCardBack || '', verifyStatus: data.verifyStatus || 'PENDING', verifyRemark: data.verifyRemark || '' })
  currentUser.value = { ...currentUser.value, ...data }
  localStorage.setItem('car-rental-user', JSON.stringify(currentUser.value))
}
async function loadFavorites() { favorites.value = await request('/api/interactions/favorites') }
async function loadNotices() { notices.value = await request('/api/notices') }
async function loadPaymentConfig() { paymentConfig.value = await request('/api/pay/alipay/config') }
async function loadOverview() { overview.value = await request('/api/dashboard/overview') }
async function loadUsers() { users.value = await request('/api/users') }
async function loadMyFeedbacks() { feedbacks.value = await request('/api/feedbacks/my') }
async function loadAdminAssets() {
  const [bannerData, noticeData, feedbackData, brandData, typeData] = await Promise.all([request('/api/banners'), request('/api/notices/admin'), request('/api/feedbacks'), request('/api/brands/admin'), request('/api/car-types/admin')])
  banners.value = bannerData
  notices.value = noticeData
  feedbacks.value = feedbackData
  brands.value = brandData
  types.value = typeData
}
async function bootstrapPrivate() {
  const tasks = [loadCars(), loadOrders(), loadStations(), loadNotices()]
  if (isAdmin.value) tasks.push(loadOverview(), loadUsers(), loadAdminAssets())
  else tasks.push(loadProfile(), loadFavorites(), loadPaymentConfig())
  await Promise.all(tasks)
}

async function handleLogin() {
  loading.auth = true
  try {
    const data = await request('/api/auth/login', { method: 'POST', body: JSON.stringify(loginForm) })
    saveSession(data)
    userView.value = 'home'
    adminView.value = 'system-overview'
    ElMessage.success('登录成功')
    await bootstrapPrivate()
  } catch (error) { ElMessage.error(error.message) } finally { loading.auth = false }
}
async function handleRegister() {
  loading.auth = true
  try {
    await request('/api/auth/register', { method: 'POST', body: JSON.stringify(registerForm) })
    authTab.value = 'login'
    loginForm.username = registerForm.username
    loginForm.password = ''
    resetObject(registerForm, { username: '', password: '', realName: '', phone: '', email: '' })
    ElMessage.success('注册成功，请登录并完善身份证信息')
  } catch (error) { ElMessage.error(error.message) } finally { loading.auth = false }
}
function logout() { clearSession(); userView.value = 'home'; adminView.value = 'system-overview'; selectedCar.value = null; orders.value = []; users.value = []; favorites.value = []; feedbacks.value = []; notices.value = [] }
function goToCatalogWithCar(car) { carFilters.brand = car.brand; carFilters.typeName = car.typeName; if (!isLoggedIn.value) { authTab.value = 'login'; return ElMessage.info('请先登录后查询并下单') } userView.value = 'catalog'; loadCars().catch((error) => ElMessage.error(error.message)) }
function chooseCar(car) { selectedCar.value = car; rentalForm.pickupStationId = car.stationId; rentalForm.returnStationId = null; fillPointByStation('pickup', car.stationId); Object.assign(returnPoint, { address: '', longitude: '', latitude: '' }); userView.value = 'payment' }
function isFavorite(carId) { return favorites.value.some((item) => item.carId === carId) }
async function toggleFavorite(carId) { try { const favored = isFavorite(carId); await request(`/api/interactions/favorites/${carId}`, { method: favored ? 'DELETE' : 'POST' }); await loadFavorites(); ElMessage.success(favored ? '已取消收藏' : '已加入收藏') } catch (error) { ElMessage.error(error.message) } }
async function saveProfile() { loading.save = true; try { await request('/api/users/profile', { method: 'PUT', body: JSON.stringify(profileForm) }); await loadProfile(); ElMessage.success('资料已提交，等待管理员审核') } catch (error) { ElMessage.error(error.message) } finally { loading.save = false } }
async function uploadProfileImage(option, field) {
  loading.profileUpload = true
  try {
    const formData = new FormData()
    formData.append('file', option.file)
    const imageUrl = await request('/api/users/profile/upload', {
      method: 'POST',
      body: formData,
    })
    profileForm[field] = `${baseUrl}${imageUrl}`
    ElMessage.success('证件图片上传成功')
    option.onSuccess?.(imageUrl)
  } catch (error) {
    ElMessage.error(error.message)
    option.onError?.(error)
  } finally {
    loading.profileUpload = false
  }
}
async function createFeedback() { try { await request('/api/feedbacks', { method: 'POST', body: JSON.stringify(feedbackForm) }); feedbackForm.title = ''; feedbackForm.content = ''; await loadMyFeedbacks(); ElMessage.success('反馈已提交') } catch (error) { ElMessage.error(error.message) } }
function validateRentalForm() {
  if (!selectedCar.value) return ElMessage.warning('请先选择车辆'), false
  if (!rentalForm.pickupStationId || !rentalForm.returnStationId || !rentalForm.startDate || !rentalForm.endDate) return ElMessage.warning('请完善取还车地点和时间'), false
  const today = new Date().toISOString().slice(0, 10)
  if (rentalForm.startDate < today) return ElMessage.warning('取车日期不能早于今天'), false
  if (rentalForm.endDate < rentalForm.startDate) return ElMessage.warning('还车日期不能早于取车日期'), false
  if (!pickupPoint.longitude || !returnPoint.longitude) return ElMessage.warning('请确认取还车位置'), false
  return true
}
async function submitOrder() {
  if (!validateRentalForm()) return
  if (currentUser.value?.verifyStatus !== 'APPROVED') return ElMessage.warning('身份证信息尚未审核通过，暂时不能下单')
  loading.save = true
  try {
    const order = await request('/api/orders', { method: 'POST', body: JSON.stringify({ carId: selectedCar.value.id, stationId: rentalForm.pickupStationId, returnStationId: rentalForm.returnStationId, startDate: rentalForm.startDate, endDate: rentalForm.endDate, paymentMethod: rentalForm.paymentMethod, pickupAddress: pickupPoint.address, pickupLongitude: pickupPoint.longitude, pickupLatitude: pickupPoint.latitude, returnAddress: returnPoint.address, returnLongitude: returnPoint.longitude, returnLatitude: returnPoint.latitude, remark: rentalForm.remark }) })
    await loadOrders(); userView.value = 'orders'; ElMessage.success('订单创建成功'); if (rentalForm.paymentMethod === 'ALIPAY' && paymentConfig.value.ready) await payOrder(order.id)
  } catch (error) { ElMessage.error(error.message) } finally { loading.save = false }
}
async function payOrder(orderId) {
  if (!paymentConfig.value.ready) return ElMessage.warning('当前支付服务未配置完成')
  loading.pay = true
  try {
    const formHtml = await request(`/api/pay/alipay/${orderId}/form`)
    const payWindow = window.open('', '_blank')
    if (!payWindow) throw new Error('浏览器拦截了支付窗口，请允许弹窗后重试')
    payWindow.document.open(); payWindow.document.write(formHtml); payWindow.document.close()
  } catch (error) { ElMessage.error(error.message) } finally { loading.pay = false }
}
function canCancelOrder(order) { if (isAdmin.value) return order.orderStatus !== 'COMPLETED' && order.orderStatus !== 'CANCELLED'; return ['BOOKED', 'PENDING'].includes(order.orderStatus) }
function canReturnOrder(order) { if (isAdmin.value) return order.orderStatus !== 'COMPLETED' && order.orderStatus !== 'CANCELLED'; return order.paymentStatus === 'PAID' && ['BOOKED', 'ONGOING'].includes(order.orderStatus) }
function canPayOrder(order) { return !isAdmin.value && paymentConfig.value.ready && order.paymentStatus !== 'PAID' && !['COMPLETED', 'CANCELLED'].includes(order.orderStatus) }
function getPayText(order) { if (!paymentConfig.value.ready) return '待配置支付'; if (order.paymentStatus === 'PAID') return '已支付'; return '支付' }
async function operateOrder(orderId, action, admin = false) { try { await request(`/api/orders/${orderId}${admin ? `/admin/${action}` : `/${action}`}`, { method: 'POST' }); await Promise.all([loadOrders(), loadCars(), isAdmin.value ? loadOverview() : Promise.resolve()]); ElMessage.success('操作成功') } catch (error) { ElMessage.error(error.message) } }
async function saveBanner() { try { await request(editingId.banner ? `/api/banners/${editingId.banner}` : '/api/banners', { method: editingId.banner ? 'PUT' : 'POST', body: JSON.stringify(bannerForm) }); resetObject(bannerForm, defaultBannerForm); closeDialog('banner'); await loadAdminAssets(); ElMessage.success(editingId.banner ? '轮播图已修改' : '轮播图已添加') } catch (error) { ElMessage.error(error.message) } }
async function uploadBannerImage(option) {
  loading.bannerUpload = true
  try {
    const formData = new FormData()
    formData.append('file', option.file)
    const imageUrl = await request('/api/banners/upload', {
      method: 'POST',
      body: formData,
    })
    bannerForm.imageUrl = `${baseUrl}${imageUrl}`
    ElMessage.success('图片上传成功')
    option.onSuccess?.(imageUrl)
  } catch (error) {
    ElMessage.error(error.message)
    option.onError?.(error)
  } finally {
    loading.bannerUpload = false
  }
}
async function uploadCarImage(option) {
  loading.carUpload = true
  try {
    const formData = new FormData()
    formData.append('file', option.file)
    const imageUrl = await request('/api/cars/upload', {
      method: 'POST',
      body: formData,
    })
    carForm.coverImage = `${baseUrl}${imageUrl}`
    ElMessage.success('车辆图片上传成功')
    option.onSuccess?.(imageUrl)
  } catch (error) {
    ElMessage.error(error.message)
    option.onError?.(error)
  } finally {
    loading.carUpload = false
  }
}
async function deleteBanner(id) { try { await request(`/api/banners/${id}`, { method: 'DELETE' }); await loadAdminAssets(); ElMessage.success('轮播图已删除') } catch (error) { ElMessage.error(error.message) } }
async function saveNotice() { try { await request(editingId.notice ? `/api/notices/${editingId.notice}` : '/api/notices', { method: editingId.notice ? 'PUT' : 'POST', body: JSON.stringify(noticeForm) }); resetObject(noticeForm, defaultNoticeForm); closeDialog('notice'); await Promise.all([loadAdminAssets(), loadPublicHome()]); ElMessage.success(editingId.notice ? '公告已修改' : '公告已发布') } catch (error) { ElMessage.error(error.message) } }
async function deleteNotice(id) { try { await request(`/api/notices/${id}`, { method: 'DELETE' }); await Promise.all([loadAdminAssets(), loadPublicHome()]); ElMessage.success('公告已删除') } catch (error) { ElMessage.error(error.message) } }
async function saveStation() { try { await request(editingId.station ? `/api/stations/${editingId.station}` : '/api/stations', { method: editingId.station ? 'PUT' : 'POST', body: JSON.stringify(stationForm) }); resetObject(stationForm, defaultStationForm); closeDialog('station'); await loadStations(); ElMessage.success(editingId.station ? '营业网点已修改' : '营业网点已新增') } catch (error) { ElMessage.error(error.message) } }
async function deleteStation(id) { try { await request(`/api/stations/${id}`, { method: 'DELETE' }); await loadStations(); ElMessage.success('营业网点已删除') } catch (error) { ElMessage.error(error.message) } }
async function saveBrand() { try { await request(editingId.brand ? `/api/brands/${editingId.brand}` : '/api/brands', { method: editingId.brand ? 'PUT' : 'POST', body: JSON.stringify(brandForm) }); resetObject(brandForm, defaultBrandForm); closeDialog('brand'); await Promise.all([loadAdminAssets(), loadPublicHome()]); ElMessage.success(editingId.brand ? '品牌已修改' : '品牌已添加') } catch (error) { ElMessage.error(error.message) } }
async function deleteBrand(id) { try { await request(`/api/brands/${id}`, { method: 'DELETE' }); await Promise.all([loadAdminAssets(), loadPublicHome()]); ElMessage.success('品牌已删除') } catch (error) { ElMessage.error(error.message) } }
async function saveType() { try { await request(editingId.type ? `/api/car-types/${editingId.type}` : '/api/car-types', { method: editingId.type ? 'PUT' : 'POST', body: JSON.stringify(typeForm) }); resetObject(typeForm, defaultTypeForm); closeDialog('type'); await Promise.all([loadAdminAssets(), loadPublicHome()]); ElMessage.success(editingId.type ? '类型已修改' : '类型已添加') } catch (error) { ElMessage.error(error.message) } }
async function deleteType(id) { try { await request(`/api/car-types/${id}`, { method: 'DELETE' }); await Promise.all([loadAdminAssets(), loadPublicHome()]); ElMessage.success('类型已删除') } catch (error) { ElMessage.error(error.message) } }
async function saveCar() { try { await request(editingId.car ? `/api/cars/${editingId.car}` : '/api/cars', { method: editingId.car ? 'PUT' : 'POST', body: JSON.stringify(carForm) }); resetObject(carForm, defaultCarForm); closeDialog('car'); await Promise.all([loadCars(), loadOverview()]); ElMessage.success(editingId.car ? '车辆已修改' : '车辆已新增') } catch (error) { ElMessage.error(error.message) } }
async function deleteCar(id) { try { await request(`/api/cars/${id}`, { method: 'DELETE' }); await Promise.all([loadCars(), loadOverview()]); ElMessage.success('车辆已删除') } catch (error) { ElMessage.error(error.message) } }
async function saveUser() { try { await request(editingId.admin ? `/api/users/${editingId.admin}` : '/api/users', { method: editingId.admin ? 'PUT' : 'POST', body: JSON.stringify(userForm) }); resetObject(userForm, defaultUserForm); closeDialog('admin'); await Promise.all([loadUsers(), loadOverview()]); ElMessage.success(editingId.admin ? '管理员已修改' : '管理员已新增') } catch (error) { ElMessage.error(error.message) } }
async function updateUser(user, changes) { try { await request(`/api/users/${user.id}`, { method: 'PUT', body: JSON.stringify({ ...user, ...changes }) }); await Promise.all([loadUsers(), isAdmin.value ? loadOverview() : Promise.resolve()]); ElMessage.success('用户信息已更新') } catch (error) { ElMessage.error(error.message) } }
async function replyFeedback(item) { const reply = replyDrafts[item.id]; if (!reply) return ElMessage.warning('请先输入回复内容'); try { await request(`/api/feedbacks/${item.id}/reply`, { method: 'PUT', body: JSON.stringify({ replyContent: reply }) }); replyDrafts[item.id] = ''; await loadAdminAssets(); ElMessage.success('反馈已回复') } catch (error) { ElMessage.error(error.message) } }
function disableStartDate(date) { const today = new Date(); today.setHours(0, 0, 0, 0); return date.getTime() < today.getTime() }
function disableEndDate(date) { if (!rentalForm.startDate) return disableStartDate(date); const start = new Date(rentalForm.startDate); start.setHours(0, 0, 0, 0); return date.getTime() < start.getTime() }
function loadBaiduMapScript() {
  if (window.BMap) return Promise.resolve(window.BMap)
  if (!baiduMapAk) return Promise.reject(new Error('当前未启用百度地图，已切换为站点选择模式'))
  return new Promise((resolve, reject) => {
    const callbackName = `initBMap_${Date.now()}`
    window[callbackName] = () => { resolve(window.BMap); delete window[callbackName] }
    const script = document.createElement('script')
    script.src = `https://api.map.baidu.com/api?v=3.0&ak=${baiduMapAk}&callback=${callbackName}`
    script.onerror = () => reject(new Error('百度地图脚本加载失败'))
    document.body.appendChild(script)
  })
}
async function initMap() {
  if (!isLoggedIn.value || isAdmin.value || !mapEnabled.value || !mapContainer.value || !stations.value.length) return
  try {
    const BMap = await loadBaiduMapScript()
    if (!mapInstance.value) {
      const first = stations.value[0]
      const map = new BMap.Map(mapContainer.value)
      map.centerAndZoom(new BMap.Point(Number(first.longitude), Number(first.latitude)), 12)
      map.enableScrollWheelZoom(true)
      map.addControl(new BMap.NavigationControl())
      map.addEventListener('click', (event) => handleMapClick(BMap, event.point))
      mapInstance.value = map
    }
    renderMarkers(BMap)
  } catch (error) { ElMessage.info(error.message) }
}
function renderMarkers(BMap) {
  if (!mapInstance.value) return
  markerOverlays.value.forEach((overlay) => mapInstance.value.removeOverlay(overlay))
  markerOverlays.value = []
  stations.value.forEach((station) => {
    const point = new BMap.Point(Number(station.longitude), Number(station.latitude))
    const marker = new BMap.Marker(point)
    marker.addEventListener('click', () => applyStationToTarget(locationTarget.value, station))
    mapInstance.value.addOverlay(marker)
    markerOverlays.value.push(marker)
  })
}
function handleMapClick(BMap, point) {
  const geocoder = new BMap.Geocoder()
  geocoder.getLocation(point, (result) => {
    const station = findNearestStation(point.lng, point.lat)
    if (station) {
      if (locationTarget.value === 'pickup') rentalForm.pickupStationId = station.id
      else rentalForm.returnStationId = station.id
    }
    applyPoint(locationTarget.value, { address: result?.address || `经度 ${point.lng}，纬度 ${point.lat}`, longitude: String(point.lng), latitude: String(point.lat) })
  })
}
function applyPoint(target, point) { Object.assign(target === 'pickup' ? pickupPoint : returnPoint, point) }
function applyStationToTarget(target, station) { if (target === 'pickup') rentalForm.pickupStationId = station.id; else rentalForm.returnStationId = station.id; applyPoint(target, { address: `${station.city}${station.address}`, longitude: station.longitude, latitude: station.latitude }) }
function fillPointByStation(target, stationId) { const station = stations.value.find((item) => item.id === stationId); if (station) applyPoint(target, { address: `${station.city}${station.address}`, longitude: station.longitude, latitude: station.latitude }) }
function findNearestStation(lng, lat) { return stations.value.map((station) => ({ station, distance: Math.hypot(Number(station.longitude) - lng, Number(station.latitude) - lat) })).sort((a, b) => a.distance - b.distance)[0]?.station || null }
watch(() => rentalForm.pickupStationId, (value) => { if (value) fillPointByStation('pickup', value) })
watch(() => rentalForm.returnStationId, (value) => { if (value) fillPointByStation('return', value) })
watch(userView, async (value) => { if (value === 'feedback' && isLoggedIn.value && !isAdmin.value) await loadMyFeedbacks(); if (value === 'payment') { await nextTick(); await initMap() } })
onMounted(async () => { await loadPublicHome(); if (isLoggedIn.value) await bootstrapPrivate() })
</script>

<template>
  <div class="page-shell">
    <template v-if="!isLoggedIn">
      <section class="guest-hero">
        <div class="guest-main-card">
          <el-carousel height="360px" indicator-position="outside">
            <el-carousel-item v-for="banner in publicHome.banners" :key="banner.id">
              <div class="banner-card" :style="{ backgroundImage: `linear-gradient(rgba(15,31,54,.28), rgba(15,31,54,.48)), url(${banner.imageUrl})` }">
                <div class="banner-copy"><h1>{{ banner.title }}</h1></div>
              </div>
            </el-carousel-item>
          </el-carousel>
          <div class="section-head guest-section-head"><h2>系统公告</h2></div>
          <div class="notice-list"><article v-for="notice in publicHome.notices" :key="notice.id" class="notice-card"><h3>{{ notice.title }}</h3><p>{{ notice.content }}</p></article></div>
        </div>
        <div class="auth-panel">
          <el-tabs v-model="authTab">
            <el-tab-pane label="登录" name="login"><el-form label-position="top"><el-form-item label="用户名"><el-input v-model="loginForm.username" /></el-form-item><el-form-item label="密码"><el-input v-model="loginForm.password" show-password /></el-form-item><el-button class="wide-btn" type="primary" :loading="loading.auth" @click="handleLogin">登录系统</el-button></el-form></el-tab-pane>
            <el-tab-pane label="注册" name="register"><el-form label-position="top"><el-form-item label="用户名"><el-input v-model="registerForm.username" /></el-form-item><el-form-item label="密码"><el-input v-model="registerForm.password" show-password /></el-form-item><el-form-item label="姓名"><el-input v-model="registerForm.realName" /></el-form-item><el-form-item label="手机号"><el-input v-model="registerForm.phone" /></el-form-item><el-form-item label="邮箱"><el-input v-model="registerForm.email" /></el-form-item><el-button class="wide-btn" type="success" :loading="loading.auth" @click="handleRegister">创建账户</el-button></el-form></el-tab-pane>
          </el-tabs>
        </div>
      </section>
    </template>

    <template v-else-if="!isAdmin">
      <section class="top-shell top-user-shell"><div class="top-user-bar"><div class="top-user"><div><div class="profile-name">{{ currentUser?.realName || currentUser?.username }}</div><div class="profile-role">审核状态：{{ verifyStatusText[currentUser?.verifyStatus] || '待完善' }}</div></div><el-button text @click="logout">退出</el-button></div></div></section>
      <section class="top-shell nav-shell"><div class="top-nav"><div class="nav-pills"><el-button v-for="item in userNavItems" :key="item.key" :type="userView === item.key ? 'primary' : 'default'" @click="userView = item.key">{{ item.label }}</el-button></div></div></section>
      <section v-if="userView === 'home'" class="section-block"><el-carousel height="280px" indicator-position="outside"><el-carousel-item v-for="banner in publicHome.banners" :key="banner.id"><div class="banner-card" :style="{ backgroundImage: `linear-gradient(rgba(15,31,54,.28), rgba(15,31,54,.48)), url(${banner.imageUrl})` }"><div class="banner-copy"><p class="eyebrow">首页推荐</p><h2>{{ banner.title }}</h2></div></div></el-carousel-item></el-carousel><div class="section-head"><h2>热门车辆</h2><el-button @click="userView = 'catalog'">查看全部车型</el-button></div><div class="car-grid"><article v-for="car in publicHome.hotCars" :key="car.id" class="car-card"><img :src="car.coverImage" :alt="car.model" /><div class="car-body"><div class="card-top"><div><h3>{{ car.brand }} {{ car.model }}</h3><p>{{ car.typeName }} · {{ car.energyType }} · {{ car.transmission }}</p></div><el-tag type="danger">热门</el-tag></div><p class="desc">{{ car.description }}</p><div class="car-meta"><strong>￥{{ car.dailyPrice }}/天</strong><span>押金 ￥{{ car.deposit }}</span></div><div class="rent-actions"><el-button plain @click="goToCatalogWithCar(car)">车型查询</el-button><el-button type="primary" @click="chooseCar(car)">立即下单</el-button></div></div></article></div></section>
      <section v-if="userView === 'rent' || userView === 'catalog'" class="section-block"><div class="section-head"><h2>{{ userView === 'rent' ? '租赁汽车' : '车型查询' }}</h2><el-button @click="loadCars" :loading="loading.cars">刷新结果</el-button></div><div class="toolbar-grid filter-grid"><el-select v-model="carFilters.brand" placeholder="品牌" clearable><el-option v-for="item in brands" :key="item.id" :label="item.name" :value="item.name" /></el-select><el-select v-model="carFilters.typeName" placeholder="车型类型" clearable><el-option v-for="item in types" :key="item.id" :label="item.name" :value="item.name" /></el-select><el-select v-model="carFilters.transmission" placeholder="变速箱" clearable><el-option label="自动挡" value="自动挡" /><el-option label="手动挡" value="手动挡" /></el-select><el-select v-model="carFilters.energyType" placeholder="能源类型" clearable><el-option label="燃油" value="燃油" /><el-option label="混动" value="混动" /><el-option label="纯电" value="纯电" /></el-select><el-select v-model="carFilters.sortBy"><el-option label="热门优先" value="hot" /><el-option label="价格从低到高" value="priceAsc" /><el-option label="价格从高到低" value="priceDesc" /></el-select><el-switch v-model="carFilters.availableOnly" active-text="仅看可租车辆" /><el-button type="primary" @click="loadCars" :loading="loading.cars">筛选车辆</el-button></div><div v-if="userView === 'rent'" class="map-layout"><div class="map-side"><div class="rental-form-column"><el-radio-group v-model="locationTarget"><el-radio-button label="pickup">选择取车位置</el-radio-button><el-radio-button label="return">选择还车位置</el-radio-button></el-radio-group><el-select v-model="rentalForm.pickupStationId" placeholder="取车网点"><el-option v-for="station in stations" :key="station.id" :label="`${station.city} · ${station.name}`" :value="station.id" /></el-select><el-select v-model="rentalForm.returnStationId" placeholder="还车网点"><el-option v-for="station in stations" :key="station.id" :label="`${station.city} · ${station.name}`" :value="station.id" /></el-select><el-date-picker v-model="rentalForm.startDate" type="date" value-format="YYYY-MM-DD" placeholder="取车日期" :disabled-date="disableStartDate" /><el-date-picker v-model="rentalForm.endDate" type="date" value-format="YYYY-MM-DD" placeholder="还车日期" :disabled-date="disableEndDate" /></div><div class="point-summary"><div><h4>取车位置</h4><p>{{ pickupPoint.address || '请在地图或下拉框中选择取车位置' }}</p></div><div><h4>还车位置</h4><p>{{ returnPoint.address || '请在地图或下拉框中选择还车位置' }}</p></div></div></div><div class="map-wrap"><div v-if="mapEnabled" ref="mapContainer" class="map-canvas"></div><div v-else class="map-canvas map-fallback"><h3>当前未启用百度地图</h3><p>已自动切换为站点选择模式，你仍然可以通过上方网点下单。</p></div></div></div><div class="car-grid"><article v-for="car in cars" :key="car.id" class="car-card"><img :src="car.coverImage" :alt="car.model" /><div class="car-body"><div class="card-top"><div><h3>{{ car.brand }} {{ car.model }}</h3><p>{{ car.typeName }} · {{ car.energyType }} · {{ car.transmission }}</p></div><el-tag :type="car.status === 'AVAILABLE' ? 'success' : 'warning'">{{ carStatusText[car.status] || car.status }}</el-tag></div><p class="desc">{{ car.description }}</p><div class="car-meta"><strong>￥{{ car.dailyPrice }}/天</strong><span>押金 ￥{{ car.deposit }}</span></div><div class="rent-actions"><el-button plain @click="toggleFavorite(car.id)">{{ isFavorite(car.id) ? '取消收藏' : '加入收藏' }}</el-button><el-button type="primary" :disabled="car.status !== 'AVAILABLE'" @click="chooseCar(car)">下单租赁</el-button></div></div></article></div></section>
      <section v-if="userView === 'payment'" class="section-block"><div class="section-head"><h2>下单支付</h2><el-button @click="userView = 'rent'">返回选车</el-button></div><div v-if="selectedCar" class="payment-layout"><div class="payment-card"><h3>订单车辆</h3><div class="selected-car"><img :src="selectedCar.coverImage" :alt="selectedCar.model" /><div><h3>{{ selectedCar.brand }} {{ selectedCar.model }}</h3><p>{{ selectedCar.typeName }} · {{ selectedCar.energyType }} · {{ selectedCar.transmission }}</p><p>日租金：￥{{ selectedCar.dailyPrice }}，押金：￥{{ selectedCar.deposit }}</p></div></div><el-form label-position="top" class="payment-form"><el-form-item label="取车网点"><el-select v-model="rentalForm.pickupStationId"><el-option v-for="station in stations" :key="station.id" :label="`${station.city} · ${station.name}`" :value="station.id" /></el-select></el-form-item><el-form-item label="还车网点"><el-select v-model="rentalForm.returnStationId"><el-option v-for="station in stations" :key="station.id" :label="`${station.city} · ${station.name}`" :value="station.id" /></el-select></el-form-item><el-form-item label="取车时间"><el-date-picker v-model="rentalForm.startDate" type="date" value-format="YYYY-MM-DD" placeholder="取车日期" :disabled-date="disableStartDate" /></el-form-item><el-form-item label="还车时间"><el-date-picker v-model="rentalForm.endDate" type="date" value-format="YYYY-MM-DD" placeholder="还车日期" :disabled-date="disableEndDate" /></el-form-item><el-form-item label="支付方式"><el-radio-group v-model="rentalForm.paymentMethod"><el-radio label="ALIPAY">支付宝</el-radio><el-radio label="OFFLINE">线下支付</el-radio></el-radio-group></el-form-item><el-form-item label="备注"><el-input v-model="rentalForm.remark" type="textarea" :rows="3" placeholder="可填写用车用途等信息" /></el-form-item></el-form></div><div class="payment-card payment-summary"><h3>费用明细</h3><p>租赁天数：{{ rentalSummary.days }} 天</p><p>车辆租金：￥{{ rentalSummary.rentAmount }}</p><p>押金：￥{{ rentalSummary.deposit }}</p><p>取车地点：{{ pickupPoint.address || (selectedPickupStation ? `${selectedPickupStation.city}${selectedPickupStation.address}` : '未选择') }}</p><p>还车地点：{{ returnPoint.address || (selectedReturnStation ? `${selectedReturnStation.city}${selectedReturnStation.address}` : '未选择') }}</p><p class="tip">如果超出还车时间，系统会按车辆日租金收取超时费用。</p><div class="summary-total">合计：￥{{ rentalSummary.total }}</div><el-button class="wide-btn" type="primary" :loading="loading.save" @click="submitOrder">提交订单</el-button><p v-if="rentalForm.paymentMethod === 'ALIPAY' && !paymentConfig.ready" class="tip">当前支付服务未配置，提交后订单会保留为未支付状态。</p></div></div><el-empty v-else description="请先在车型列表中选择车辆" /></section>
      <section v-if="userView === 'orders'" class="section-block"><div class="section-head"><h2>我的订单</h2><el-button @click="loadOrders">刷新订单</el-button></div><el-table :data="orders" stripe><el-table-column prop="pickupAddress" label="取车地点" min-width="180" /><el-table-column prop="returnAddress" label="还车地点" min-width="180" /><el-table-column prop="startDate" label="取车日期" width="120" /><el-table-column prop="endDate" label="还车日期" width="120" /><el-table-column prop="settlementAmount" label="总金额" width="110" /><el-table-column prop="extraFee" label="超时费用" width="110" /><el-table-column label="支付状态" width="110"><template #default="{ row }">{{ paymentStatusText[row.paymentStatus] || row.paymentStatus }}</template></el-table-column><el-table-column label="订单状态" width="110"><template #default="{ row }">{{ orderStatusText[row.orderStatus] || row.orderStatus }}</template></el-table-column><el-table-column label="操作" min-width="260"><template #default="{ row }"><el-space wrap><el-button size="small" type="success" :disabled="!canPayOrder(row)" :loading="loading.pay" @click="payOrder(row.id)">{{ getPayText(row) }}</el-button><el-button size="small" type="warning" :disabled="!canCancelOrder(row)" @click="operateOrder(row.id, 'cancel')">取消</el-button><el-button size="small" type="primary" :disabled="!canReturnOrder(row)" @click="operateOrder(row.id, 'return')">归还</el-button></el-space></template></el-table-column></el-table></section>
      <section v-if="userView === 'favorites'" class="section-block"><div class="section-head"><h2>收藏车辆</h2></div><div v-if="favoriteCars.length" class="car-grid"><article v-for="car in favoriteCars" :key="car.id" class="car-card"><img :src="car.coverImage" :alt="car.model" /><div class="car-body"><div class="card-top"><div><h3>{{ car.brand }} {{ car.model }}</h3><p>{{ car.typeName }} · {{ car.energyType }} · {{ car.transmission }}</p></div><el-tag>{{ carStatusText[car.status] || car.status }}</el-tag></div><p class="desc">{{ car.description }}</p><div class="rent-actions"><el-button type="danger" plain @click="toggleFavorite(car.id)">取消收藏</el-button><el-button type="primary" @click="chooseCar(car)">立即下单</el-button></div></div></article></div><el-empty v-else description="暂无收藏车辆" /></section>
      <section v-if="userView === 'profile'" class="section-block"><div class="section-head"><h2>用户信息</h2><el-tag :type="profileForm.verifyStatus === 'APPROVED' ? 'success' : profileForm.verifyStatus === 'REJECTED' ? 'danger' : 'warning'">{{ verifyStatusText[profileForm.verifyStatus] || '待完善' }}</el-tag></div><div class="profile-layout"><el-form label-position="top" class="profile-form"><el-form-item label="姓名"><el-input v-model="profileForm.realName" /></el-form-item><el-form-item label="手机号"><el-input v-model="profileForm.phone" /></el-form-item><el-form-item label="邮箱"><el-input v-model="profileForm.email" /></el-form-item><el-form-item label="身份证号"><el-input v-model="profileForm.idCardNo" /></el-form-item><el-form-item label="身份证正面照片"><el-upload :show-file-list="false" accept=".jpg,.jpeg,.png,.webp" :http-request="(option) => uploadProfileImage(option, 'idCardFront')"><el-button :loading="loading.profileUpload">上传正面照片</el-button></el-upload><div v-if="profileForm.idCardFront" class="upload-preview"><img :src="profileForm.idCardFront" alt="身份证正面" /></div></el-form-item><el-form-item label="身份证反面照片"><el-upload :show-file-list="false" accept=".jpg,.jpeg,.png,.webp" :http-request="(option) => uploadProfileImage(option, 'idCardBack')"><el-button :loading="loading.profileUpload">上传反面照片</el-button></el-upload><div v-if="profileForm.idCardBack" class="upload-preview"><img :src="profileForm.idCardBack" alt="身份证反面" /></div></el-form-item><el-button type="primary" :loading="loading.save" @click="saveProfile">提交审核资料</el-button></el-form><div class="profile-tip"><h3>审核说明</h3><p>1. 请上传清晰可辨认的身份证正反面照片。</p><p>2. 管理员审核通过后，才可正常提交租车订单。</p><p>3. 如果资料被驳回，请根据备注重新提交。</p><p>审核备注：{{ profileForm.verifyRemark || '暂无备注' }}</p></div></div></section>
      <section v-if="userView === 'feedback'" class="section-block"><div class="section-head"><h2>提出反馈</h2></div><div class="admin-grid"><div class="admin-card"><el-form label-position="top"><el-form-item label="反馈标题"><el-input v-model="feedbackForm.title" /></el-form-item><el-form-item label="反馈内容"><el-input v-model="feedbackForm.content" type="textarea" :rows="5" /></el-form-item><el-button type="primary" @click="createFeedback">提交反馈</el-button></el-form></div><div class="admin-card"><h3>历史反馈</h3><div class="feedback-list"><article v-for="item in feedbacks" :key="item.id" class="feedback-card"><div class="card-top"><h4>{{ item.title }}</h4><el-tag :type="item.status === 'REPLIED' ? 'success' : 'warning'">{{ feedbackStatusText[item.status] || item.status }}</el-tag></div><p>{{ item.content }}</p><p class="tip">回复：{{ item.replyContent || '管理员暂未回复' }}</p></article></div></div></div></section>
      <section v-if="userView === 'notices'" class="section-block"><div class="section-head"><h2>系统公告</h2></div><div class="notice-list"><article v-for="item in notices" :key="item.id" class="notice-card"><div class="card-top"><h3>{{ item.title }}</h3><span class="tip">{{ item.publishAt?.slice?.(0, 10) || '' }}</span></div><p>{{ item.content }}</p></article></div></section>
    </template>
    <template v-else>
      <section class="admin-layout">
        <aside class="admin-aside">
          <div class="admin-brand">后台管理</div>
          <el-menu :default-active="adminView" class="admin-menu" unique-opened @select="(index) => (adminView = index)">
            <el-sub-menu v-for="group in adminMenuGroups" :key="group.index" :index="group.index">
              <template #title>{{ group.title }}</template>
              <el-menu-item v-for="item in group.children" :key="item.index" :index="item.index">{{ item.title }}</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </aside>
        <main class="admin-main">
          <section class="top-shell top-user-shell"><div class="top-user-bar"><div class="top-user"><div><div class="profile-name">{{ currentUser?.realName || currentUser?.username }}</div><div class="profile-role">管理员</div></div><el-button text @click="logout">退出</el-button></div></div></section>
          <section v-if="adminView === 'system-overview'" class="section-block"><div class="section-head"><h2>系统总览</h2><el-button @click="bootstrapPrivate">刷新</el-button></div><div class="stats-grid"><div class="stats-card"><span>用户总数</span><strong>{{ overview.userCount || 0 }}</strong></div><div class="stats-card"><span>车辆总数</span><strong>{{ overview.carCount || 0 }}</strong></div><div class="stats-card"><span>订单总数</span><strong>{{ overview.orderCount || 0 }}</strong></div><div class="stats-card"><span>租金收入</span><strong>￥{{ overview.income || 0 }}</strong></div><div class="stats-card"><span>轮播图数量</span><strong>{{ overview.bannerCount || 0 }}</strong></div><div class="stats-card"><span>公告数量</span><strong>{{ overview.noticeCount || 0 }}</strong></div><div class="stats-card"><span>反馈数量</span><strong>{{ overview.feedbackCount || 0 }}</strong></div><div class="stats-card"><span>可租车辆</span><strong>{{ overview.availableCarCount || 0 }}</strong></div></div></section>
          <AdminStatisticsCharts v-if="adminView === 'system-overview'" :token="token" :base-url="baseUrl" />
          <section v-if="adminView === 'system-banners'" class="section-block"><div class="section-head"><h2>轮播图管理</h2><el-button type="primary" @click="openDialog('banner')">新增轮播图</el-button></div><el-table :data="banners" stripe><el-table-column label="图片" width="160"><template #default="{ row }"><img class="table-thumb" :src="row.imageUrl" :alt="row.title" /></template></el-table-column><el-table-column prop="title" label="标题" min-width="180" /><el-table-column prop="sort" label="排序" width="90" /><el-table-column label="操作" width="160"><template #default="{ row }"><el-space><el-button link type="primary" @click="openDialog('banner', row)">修改</el-button><el-button link type="danger" @click="deleteBanner(row.id)">删除</el-button></el-space></template></el-table-column></el-table></section>
          <section v-if="adminView === 'info-cars'" class="section-block"><div class="section-head"><h2>车辆管理</h2><el-button type="primary" @click="openDialog('car')">新增车辆</el-button></div><el-table :data="cars" stripe><el-table-column prop="brand" label="品牌" width="100" /><el-table-column prop="model" label="车型" min-width="140" /><el-table-column prop="typeName" label="类型" width="100" /><el-table-column prop="dailyPrice" label="日租金" width="100" /><el-table-column prop="stationId" label="所属网点" width="100" /><el-table-column prop="status" label="状态" width="100"><template #default="{ row }">{{ carStatusText[row.status] || row.status }}</template></el-table-column><el-table-column label="操作" width="160"><template #default="{ row }"><el-space><el-button link type="primary" @click="openDialog('car', row)">修改</el-button><el-button link type="danger" @click="deleteCar(row.id)">删除</el-button></el-space></template></el-table-column></el-table></section>
          <section v-if="adminView === 'info-orders'" class="section-block"><div class="section-head"><h2>订单管理</h2><el-button @click="loadOrders">刷新订单</el-button></div><el-table :data="orders" stripe><el-table-column prop="orderNo" label="订单号" min-width="170" /><el-table-column prop="pickupAddress" label="取车地点" min-width="180" /><el-table-column prop="returnAddress" label="还车地点" min-width="180" /><el-table-column prop="settlementAmount" label="结算金额" width="110" /><el-table-column prop="extraFee" label="超时费用" width="110" /><el-table-column label="支付状态" width="110"><template #default="{ row }">{{ paymentStatusText[row.paymentStatus] || row.paymentStatus }}</template></el-table-column><el-table-column label="订单状态" width="110"><template #default="{ row }">{{ orderStatusText[row.orderStatus] || row.orderStatus }}</template></el-table-column><el-table-column label="操作" min-width="300"><template #default="{ row }"><el-space wrap><el-button size="small" type="success" :disabled="row.paymentStatus === 'PAID'" @click="operateOrder(row.id, 'pay', true)">结算确认</el-button><el-button size="small" type="warning" :disabled="!canCancelOrder(row)" @click="operateOrder(row.id, 'cancel', true)">取消订单</el-button><el-button size="small" type="primary" :disabled="!canReturnOrder(row)" @click="operateOrder(row.id, 'return', true)">确认还车</el-button></el-space></template></el-table-column></el-table></section>
          <section v-if="adminView === 'info-feedbacks'" class="section-block"><div class="section-head"><h2>问答管理</h2></div><div class="feedback-list"><article v-for="item in feedbacks" :key="item.id" class="feedback-card"><div class="card-top"><div><h3>{{ item.title }}</h3><p class="tip">用户编号：{{ item.userId }}</p></div><el-tag :type="item.status === 'REPLIED' ? 'success' : 'warning'">{{ feedbackStatusText[item.status] || item.status }}</el-tag></div><p>{{ item.content }}</p><el-input v-model="replyDrafts[item.id]" type="textarea" :rows="3" placeholder="输入回复内容" /><div class="feedback-actions"><span class="tip">历史回复：{{ item.replyContent || '暂无回复' }}</span><el-button type="primary" @click="replyFeedback(item)">提交回复</el-button></div></article></div></section>
          <section v-if="adminView === 'info-stations'" class="section-block"><div class="section-head"><h2>营业网点管理</h2><el-button type="primary" @click="openDialog('station')">新增网点</el-button></div><el-table :data="stations" stripe><el-table-column prop="name" label="网点名称" min-width="160" /><el-table-column prop="city" label="城市" width="100" /><el-table-column prop="businessHours" label="营业时间" width="140" /><el-table-column prop="contactPhone" label="联系电话" width="140" /><el-table-column label="操作" width="160"><template #default="{ row }"><el-space><el-button link type="primary" @click="openDialog('station', row)">修改</el-button><el-button link type="danger" @click="deleteStation(row.id)">删除</el-button></el-space></template></el-table-column></el-table></section>
          <section v-if="adminView === 'info-brands'" class="section-block"><div class="section-head"><h2>品牌管理</h2><el-button type="primary" @click="openDialog('brand')">新增品牌</el-button></div><el-table :data="brands" stripe><el-table-column prop="name" label="品牌名称" min-width="180" /><el-table-column prop="sort" label="排序" width="100" /><el-table-column label="操作" width="160"><template #default="{ row }"><el-space><el-button link type="primary" @click="openDialog('brand', row)">修改</el-button><el-button link type="danger" @click="deleteBrand(row.id)">删除</el-button></el-space></template></el-table-column></el-table></section>
          <section v-if="adminView === 'info-types'" class="section-block"><div class="section-head"><h2>类型管理</h2><el-button type="primary" @click="openDialog('type')">新增类型</el-button></div><el-table :data="types" stripe><el-table-column prop="name" label="类型名称" min-width="180" /><el-table-column prop="sort" label="排序" width="100" /><el-table-column label="操作" width="160"><template #default="{ row }"><el-space><el-button link type="primary" @click="openDialog('type', row)">修改</el-button><el-button link type="danger" @click="deleteType(row.id)">删除</el-button></el-space></template></el-table-column></el-table></section>
          <section v-if="adminView === 'info-notices'" class="section-block"><div class="section-head"><h2>系统公告</h2><el-button type="primary" @click="openDialog('notice')">新增公告</el-button></div><el-table :data="notices" stripe><el-table-column prop="title" label="公告标题" min-width="200" /><el-table-column prop="content" label="公告内容" min-width="260" show-overflow-tooltip /><el-table-column prop="sort" label="排序" width="80" /><el-table-column label="操作" width="160"><template #default="{ row }"><el-space><el-button link type="primary" @click="openDialog('notice', row)">修改</el-button><el-button link type="danger" @click="deleteNotice(row.id)">删除</el-button></el-space></template></el-table-column></el-table></section>
          <section v-if="adminView === 'users-admins'" class="section-block"><div class="section-head"><h2>管理员信息</h2><el-button type="primary" @click="openDialog('admin')">新增管理员</el-button></div><el-table :data="adminList" stripe><el-table-column prop="username" label="用户名" min-width="160" /><el-table-column prop="realName" label="姓名" width="120" /><el-table-column prop="phone" label="手机号" width="140" /><el-table-column prop="status" label="状态" width="100"><template #default="{ row }">{{ row.status === 1 ? '正常' : '冻结' }}</template></el-table-column><el-table-column label="操作" width="160"><template #default="{ row }"><el-space><el-button link type="primary" @click="openDialog('admin', row)">修改</el-button><el-button link type="warning" @click="updateUser(row, { status: row.status === 1 ? 0 : 1 })">{{ row.status === 1 ? '冻结' : '启用' }}</el-button></el-space></template></el-table-column></el-table></section>
          <section v-if="adminView === 'users-customers'" class="section-block"><div class="section-head"><h2>用户信息管理</h2></div><el-table :data="customerList" stripe><el-table-column prop="username" label="用户名" /><el-table-column prop="realName" label="姓名" /><el-table-column prop="phone" label="手机号" /><el-table-column prop="verifyStatus" label="审核状态"><template #default="{ row }">{{ verifyStatusText[row.verifyStatus] || row.verifyStatus }}</template></el-table-column><el-table-column prop="verifyRemark" label="审核备注" min-width="180" /><el-table-column prop="frozenReason" label="冻结原因" min-width="180" /><el-table-column label="操作" min-width="320"><template #default="{ row }"><el-space wrap><el-button size="small" type="success" @click="updateUser(row, { verifyStatus: 'APPROVED', verifyRemark: '审核通过，可正常租赁', status: 1, frozenReason: '' })">通过审核</el-button><el-button size="small" type="warning" @click="updateUser(row, { verifyStatus: 'REJECTED', verifyRemark: '证件信息有误，请重新提交' })">驳回</el-button><el-button size="small" type="danger" @click="updateUser(row, { status: 0, frozenReason: '后台冻结账号' })">冻结</el-button><el-button size="small" @click="updateUser(row, { status: 1, frozenReason: '' })">解除冻结</el-button></el-space></template></el-table-column></el-table></section>
        </main>
      </section>
    </template>

    <el-dialog v-model="dialogVisible.banner" :title="editingId.banner ? '修改轮播图' : '新增轮播图'" width="640px" @close="closeDialog('banner')">
      <el-form label-position="top">
        <el-form-item label="标题"><el-input v-model="bannerForm.title" /></el-form-item>
        <el-form-item label="本地图片"><el-upload :show-file-list="false" accept=".jpg,.jpeg,.png,.webp,.gif" :http-request="uploadBannerImage"><el-button :loading="loading.bannerUpload">选择本地图片</el-button></el-upload><div v-if="bannerForm.imageUrl" class="upload-preview"><img :src="bannerForm.imageUrl" alt="轮播图预览" /></div></el-form-item>
        <el-form-item label="图片链接"><el-input v-model="bannerForm.imageUrl" /></el-form-item>
        <el-form-item label="跳转链接"><el-input v-model="bannerForm.linkUrl" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="bannerForm.sort" :min="1" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="closeDialog('banner')">取消</el-button><el-button type="primary" @click="saveBanner">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="dialogVisible.notice" :title="editingId.notice ? '修改公告' : '新增公告'" width="680px" @close="closeDialog('notice')">
      <el-form label-position="top">
        <el-form-item label="公告标题"><el-input v-model="noticeForm.title" /></el-form-item>
        <el-form-item label="公告内容"><el-input v-model="noticeForm.content" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="noticeForm.sort" :min="1" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="closeDialog('notice')">取消</el-button><el-button type="primary" @click="saveNotice">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="dialogVisible.station" :title="editingId.station ? '修改网点' : '新增网点'" width="760px" @close="closeDialog('station')">
      <div class="dialog-grid">
        <el-input v-model="stationForm.name" placeholder="网点名称" />
        <el-input v-model="stationForm.city" placeholder="城市" />
        <el-input v-model="stationForm.address" placeholder="详细地址" />
        <el-input v-model="stationForm.contactPhone" placeholder="联系电话" />
        <el-input v-model="stationForm.longitude" placeholder="经度" />
        <el-input v-model="stationForm.latitude" placeholder="纬度" />
        <el-input v-model="stationForm.businessHours" placeholder="营业时间" />
        <el-input v-model="stationForm.description" placeholder="网点说明" />
      </div>
      <template #footer><el-button @click="closeDialog('station')">取消</el-button><el-button type="primary" @click="saveStation">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="dialogVisible.brand" :title="editingId.brand ? '修改品牌' : '新增品牌'" width="480px" @close="closeDialog('brand')">
      <el-form label-position="top">
        <el-form-item label="品牌名称"><el-input v-model="brandForm.name" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="brandForm.sort" :min="1" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="closeDialog('brand')">取消</el-button><el-button type="primary" @click="saveBrand">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="dialogVisible.type" :title="editingId.type ? '修改类型' : '新增类型'" width="480px" @close="closeDialog('type')">
      <el-form label-position="top">
        <el-form-item label="类型名称"><el-input v-model="typeForm.name" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="typeForm.sort" :min="1" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="closeDialog('type')">取消</el-button><el-button type="primary" @click="saveType">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="dialogVisible.car" :title="editingId.car ? '修改车辆' : '新增车辆'" width="820px" @close="closeDialog('car')">
      <el-form label-position="top" class="car-dialog-form">
        <div class="dialog-grid dialog-grid-wide">
          <el-form-item label="品牌">
            <el-select v-model="carForm.brand" placeholder="请选择品牌"><el-option v-for="item in brands" :key="item.id" :label="item.name" :value="item.name" /></el-select>
          </el-form-item>
          <el-form-item label="车型类型">
            <el-select v-model="carForm.typeName" placeholder="请选择车型类型"><el-option v-for="item in types" :key="item.id" :label="item.name" :value="item.name" /></el-select>
          </el-form-item>
          <el-form-item label="车型名称">
            <el-input v-model="carForm.model" placeholder="请输入车型名称" />
          </el-form-item>
          <el-form-item label="车牌号">
            <el-input v-model="carForm.plateNumber" placeholder="请输入车牌号" />
          </el-form-item>
          <el-form-item label="颜色">
            <el-input v-model="carForm.color" placeholder="请输入颜色" />
          </el-form-item>
          <el-form-item label="变速箱">
            <el-select v-model="carForm.transmission"><el-option label="自动挡" value="自动挡" /><el-option label="手动挡" value="手动挡" /></el-select>
          </el-form-item>
          <el-form-item label="能源类型">
            <el-select v-model="carForm.energyType"><el-option label="燃油" value="燃油" /><el-option label="混动" value="混动" /><el-option label="纯电" value="纯电" /></el-select>
          </el-form-item>
          <el-form-item label="所属网点">
            <el-select v-model="carForm.stationId" placeholder="请选择所属网点"><el-option v-for="station in stations" :key="station.id" :label="station.name" :value="station.id" /></el-select>
          </el-form-item>
          <el-form-item label="座位数">
            <el-input-number v-model="carForm.seatCount" :min="2" />
          </el-form-item>
          <el-form-item label="日租金">
            <el-input-number v-model="carForm.dailyPrice" :min="100" />
          </el-form-item>
          <el-form-item label="押金">
            <el-input-number v-model="carForm.deposit" :min="500" />
          </el-form-item>
          <el-form-item label="车辆状态">
            <el-select v-model="carForm.status"><el-option label="可租赁" value="AVAILABLE" /><el-option label="维护中" value="MAINTENANCE" /><el-option label="已停用" value="DISABLED" /></el-select>
          </el-form-item>
          <el-form-item label="热门推荐">
            <el-switch v-model="carForm.hotFlag" :active-value="1" :inactive-value="0" active-text="是" inactive-text="否" />
          </el-form-item>
          <el-form-item label="本地图片">
            <el-upload :show-file-list="false" accept=".jpg,.jpeg,.png,.webp,.gif" :http-request="uploadCarImage">
              <el-button :loading="loading.carUpload">选择本地图片</el-button>
            </el-upload>
            <div v-if="carForm.coverImage" class="upload-preview"><img :src="carForm.coverImage" alt="车辆图片预览" /></div>
          </el-form-item>
          <el-form-item label="图片链接">
            <el-input v-model="carForm.coverImage" placeholder="可直接粘贴图片链接" />
          </el-form-item>
          <el-form-item label="车辆说明" class="form-span-full">
            <el-input v-model="carForm.description" type="textarea" :rows="3" placeholder="请输入车辆说明" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer><el-button @click="closeDialog('car')">取消</el-button><el-button type="primary" @click="saveCar">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="dialogVisible.admin" :title="editingId.admin ? '修改管理员' : '新增管理员'" width="640px" @close="closeDialog('admin')">
      <div class="dialog-grid">
        <el-input v-model="userForm.username" placeholder="用户名" :disabled="!!editingId.admin" />
        <el-input v-model="userForm.realName" placeholder="姓名" />
        <el-input v-model="userForm.phone" placeholder="手机号" />
        <el-input v-model="userForm.email" placeholder="邮箱" />
        <el-input v-model="userForm.password" placeholder="密码，不修改可留空" />
      </div>
      <template #footer><el-button @click="closeDialog('admin')">取消</el-button><el-button type="primary" @click="saveUser">保存</el-button></template>
    </el-dialog>
  </div>
</template>
