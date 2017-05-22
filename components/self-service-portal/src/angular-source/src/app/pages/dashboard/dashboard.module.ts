import { NgModule }      from '@angular/core';
import { CommonModule }  from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgaModule } from '../../theme/nga.module';

import { Dashboard } from './dashboard.component';
import { routing }       from './dashboard.routing';

import { PinReset } from './pinReset';
import { PieChart } from './pieChart';
import { LoginChart } from './loginChart';
import { Feed } from './feed';
import { FeedService } from './feed/feed.service';
import { PieChartService } from './pieChart/pieChart.service';
import { LoginChartService } from './loginChart/loginChart.service';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    NgaModule,
    routing
  ],
  declarations: [
    PinReset,
    PieChart,
    LoginChart,
    Feed,
    Dashboard
  ],
  providers: [
    FeedService,
    PieChartService,
    LoginChartService
  ]
})
export class DashboardModule {}
