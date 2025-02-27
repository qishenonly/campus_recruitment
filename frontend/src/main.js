import { createApp } from 'vue'
import router from './router'
import { createPinia } from 'pinia'
import { Button, Search, Form, Field, CellGroup, Checkbox } from 'vant'
import 'vant/lib/index.css'
import App from './App.vue'
import './style.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Button)
app.use(Search)
app.use(Form)
app.use(Field)
app.use(CellGroup)
app.use(Checkbox)

app.mount('#app')