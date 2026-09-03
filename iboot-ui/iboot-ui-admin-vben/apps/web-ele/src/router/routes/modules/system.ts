import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/system/notify-message',
    component: () => import('#/views/system/notify/my/index.vue'),
    name: 'MyNotifyMessage',
    meta: {
      title: '我的站内信',
      icon: 'ant-design:message-filled',
      hideInMenu: true,
    },
  },
  {
    path: '/system/my-notice',
    component: () => import('#/views/system/notice/my/index.vue'),
    name: 'MyNotice',
    meta: {
      title: '我的公告',
      icon: 'lucide:megaphone',
      hideInMenu: true,
    },
  },
];

export default routes;
