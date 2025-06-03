import { createRouter, createWebHistory } from "vue-router";
import Home from "./views/Home.vue";
import Login from "./views/Login.vue";
import Register from "./views/Register.vue";
import Chat from "./views/Chat.vue";
import NotFound from "./components/NotFound.vue";

const routes = [
  { path: "/login", component: Login },
  { path: "/register", component: Register },
  { path: "/chat", component: Chat },
  { path: "/", component: Home },
  { path: "/:pathMatch(.*)*", component: NotFound },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// router.beforeEach((to, from, next) => {
//   const publicPages = ['/login', '/register']
//   const authRequired = !publicPages.includes(to.path)
//   const token = localStorage.getItem('jwt')
//   if (authRequired && !token) {
//     return next('/login')
//   }
//   next()
// })

export default router;
