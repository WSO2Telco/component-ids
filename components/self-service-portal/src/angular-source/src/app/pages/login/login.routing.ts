import { Routes, RouterModule }  from '@angular/router';

import {Login} from './login.component';
import { ModuleWithProviders } from '@angular/core';

// noinspection TypeScriptValidateTypes
export const routes: Routes = [
  { path: '', component: Login },
  { path: '1/:access_token', component: Login },
  { path: '0/:error', component: Login }
];

export const routing: ModuleWithProviders = RouterModule.forChild(routes);
