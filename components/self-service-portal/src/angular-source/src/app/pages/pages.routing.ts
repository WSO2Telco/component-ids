import { Routes, RouterModule }  from '@angular/router';
import { Pages } from './pages.component';
import { ModuleWithProviders } from '@angular/core';
import { ProtectedRouteCanActivate } from '../auth/activator';
// noinspection TypeScriptValidateTypes

// export function loadChildren(path) { return System.import(path); };

export const routes: Routes = [
  {
    path: '',
    loadChildren: 'app/pages/login/login.module#LoginModule'
  },
  {
    path: 'login',
    loadChildren: 'app/pages/login/login.module#LoginModule'
  },
  {
    path: 'pages',
    component: Pages,
    canActivate: [ProtectedRouteCanActivate],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', loadChildren: 'app/pages/dashboard/dashboard.module#DashboardModule' },
      { path: 'reset_pin', loadChildren: 'app/pages/reset_pin/pin.module#PinResetModule' }
    ]
  }
];

export const routing: ModuleWithProviders = RouterModule.forChild(routes);
