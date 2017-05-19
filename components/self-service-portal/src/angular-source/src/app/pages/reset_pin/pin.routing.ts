import { Routes, RouterModule }  from '@angular/router';

import { PinReset } from './pin.component';

// noinspection TypeScriptValidateTypes
const routes: Routes = [
  {
    path: '',
    component: PinReset
  }
];

export const routing = RouterModule.forChild(routes);
