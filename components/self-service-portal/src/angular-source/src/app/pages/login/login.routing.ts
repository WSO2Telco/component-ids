import { Routes, RouterModule }  from '@angular/router';

import {Login, Clear} from './login.component';
import { ModuleWithProviders } from '@angular/core';

// noinspection TypeScriptValidateTypes
export const routes: Routes = [
  { path: '', component: Login },
  { path: '1/:access_token', component: Login },
  { path: '0/:error', component: Login },
  { path: 'clear', component: Clear },
];

export const routing: ModuleWithProviders = RouterModule.forChild(routes);
