import { createApp } from 'vue'
import router from './router'
import { createPinia } from 'pinia'
import { 
  Button, 
  Search, 
  Form, 
  Field, 
  CellGroup, 
  Checkbox,
  Dialog,
  Icon
} from 'vant'
import 'vant/lib/index.css'
import 'amfe-flexible'
import App from './App.vue'
import './style.css'

// 设置 rem 基准值
const setRem = () => {
  const baseSize = 75
  const scale = document.documentElement.clientWidth / 750
  document.documentElement.style.fontSize = baseSize * Math.min(scale, 2) + 'px'
}

// 初始化
setRem()
// 改变窗口大小时重新设置 rem
window.onresize = setRem

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Button)
app.use(Search)
app.use(Form)
app.use(Field)
app.use(CellGroup)
app.use(Checkbox)
app.use(Dialog)
app.use(Icon)

app.mount('#app')