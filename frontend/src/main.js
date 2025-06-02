// src/main.js
import "./assets/main.css";

import { createApp } from "vue";
import App from "./App.vue";
import router from "./http.js";

import { createPinia } from "pinia";

const app = createApp(App);

const pinia = createPinia();
app.use(pinia);
app.use(router);

app.mount("#app");
