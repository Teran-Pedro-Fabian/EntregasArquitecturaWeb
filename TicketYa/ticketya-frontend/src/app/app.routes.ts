import { Routes } from '@angular/router';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login)
  },
  {
    path: 'organizador',
    canActivate: [roleGuard('ORGANIZADOR')],
    loadComponent: () =>
      import('./features/organizer/dashboard/organizer-dashboard').then((m) => m.OrganizerDashboard)
  },
  {
    path: 'acceso',
    canActivate: [roleGuard('PERSONAL_ACCESO', 'ADMINISTRADOR')],
    loadComponent: () =>
      import('./features/access/access-page/access-page').then((m) => m.AccessPage)
  },
  {
    path: 'admin',
    canActivate: [roleGuard('ADMINISTRADOR')],
    loadComponent: () => import('./features/admin/admin-page/admin-page').then((m) => m.AdminPage)
  },
  {
    path: 'preferencias',
    canActivate: [roleGuard('CLIENTE')],
    loadComponent: () =>
      import('./features/preferences/preferences-page').then((m) => m.PreferencesPage)
  },
  {
    path: 'recomendaciones',
    canActivate: [roleGuard('CLIENTE')],
    loadComponent: () =>
      import('./features/recommendations/recommendations-page').then((m) => m.RecommendationsPage)
  },
  {
    path: 'notificaciones',
    canActivate: [roleGuard('CLIENTE')],
    loadComponent: () =>
      import('./features/notifications/notifications-page').then((m) => m.NotificationsPage)
  },
  {
    path: 'mis-entradas',
    canActivate: [roleGuard('CLIENTE')],
    loadComponent: () =>
      import('./features/tickets/my-tickets/my-tickets').then((m) => m.MyTicketsPage)
  },
  {
    path: 'eventos/:id/compra',
    canActivate: [roleGuard('CLIENTE', 'ADMINISTRADOR')],
    loadComponent: () =>
      import('./features/purchase/purchase-flow/purchase-flow').then((m) => m.PurchaseFlow)
  },
  {
    path: 'eventos/:id',
    loadComponent: () =>
      import('./features/events/detail/event-detail').then((m) => m.EventDetailPage)
  },
  {
    path: 'eventos',
    loadComponent: () =>
      import('./features/events/catalog/events-catalog').then((m) => m.EventsCatalog)
  },
  { path: '', loadComponent: () => import('./features/home/home').then((m) => m.Home) },
  { path: '**', redirectTo: '' }
];
