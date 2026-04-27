import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/index.vue'),
      meta: { public: true }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/login/index.vue'),
      meta: { public: true }
    },
    {
      path: '/',
      name: 'Layout',
      component: () => import('@/views/LayoutView.vue'),
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('@/views/HomeView.vue'),
          meta: { title: 'Home', public: true }
        },
        {
          path: '/departments',
          name: 'Departments',
          component: () => import('@/views/DepartmentView.vue'),
          meta: { title: 'Departments' }
        },
        {
          path: '/doctors/:departmentId',
          name: 'Doctors',
          component: () => import('@/views/DoctorView.vue'),
          meta: { title: 'Doctors' }
        },
        {
          path: '/doctor/:id',
          name: 'DoctorDetail',
          component: () => import('@/views/DoctorDetail.vue'),
          meta: { title: 'Doctor Detail' }
        },
        {
          path: '/appointment/:doctorId',
          name: 'Appointment',
          component: () => import('@/views/AppointmentView.vue'),
          meta: { title: 'Appointment', requiresAuth: true, requiresPatient: true }
        },
        {
          path: '/appointments',
          name: 'MyAppointments',
          component: () => import('@/views/MyAppointmentView.vue'),
          meta: { title: 'My Appointments', requiresAuth: true, requiresPatient: true }
        },
        {
          path: '/patients',
          name: 'Patients',
          component: () => import('@/views/PatientView.vue'),
          meta: { title: 'Patients', requiresAuth: true, requiresPatient: true }
        },
        {
          path: '/profile',
          name: 'Profile',
          component: () => import('@/views/Profile.vue'),
          meta: { title: 'Profile', requiresAuth: true }
        },
        {
          path: '/admin/statistics',
          name: 'Statistics',
          component: () => import('@/views/admin/Statistics.vue'),
          meta: { title: 'Statistics', requiresAuth: true, requiresAdmin: true }
        },
        {
          path: '/admin/schedules',
          name: 'ScheduleManage',
          component: () => import('@/views/admin/ScheduleManage.vue'),
          meta: { title: 'Schedule Manage', requiresAuth: true, requiresAdmin: true }
        },
        {
          path: '/doctor/portal',
          name: 'DoctorPortal',
          component: () => import('@/views/doctor/DoctorPortal.vue'),
          meta: { title: 'Doctor Portal', requiresAuth: true, requiresDoctor: true }
        }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
  } else if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next('/')
  } else if (to.meta.requiresDoctor && !userStore.isDoctor) {
    next('/')
  } else if (to.meta.requiresPatient && !userStore.isPatient) {
    next('/')
  } else {
    next()
  }
})

export default router
