import {Component} from '@angular/core';

import {LoginChartService} from "./loginChart.service";
import * as Chart from 'chart.js';

import 'style-loader!./loginChart.scss';
import {BaThemeConfigProvider, colorHelper} from '../../../theme';

@Component({
  selector: 'login-chart',
  templateUrl: './loginChart.html'
})

// TODO: move chart.js to it's own component
export class LoginChart {

  public doughnutData: Array<Object>;
  public data_error:Boolean = false;
  public totalcount:Number = 0;
  public colorpos:String = 'sa';

  constructor(private loginChartService:LoginChartService , private _baConfig:BaThemeConfigProvider) {
      this.doughnutData = [];
       let dashboardChartColors = this._baConfig.get().colors.chartcolor;

      loginChartService.getData().subscribe(
              data => {
                let i = 0;
                let appLoginData = data['data']['items'];
                for (let appLogin of appLoginData) {
                  this.totalcount += appLogin.login_count;

                  this.doughnutData.push({
                      value: appLogin.login_count,
                      color: dashboardChartColors['color' + i],
                     // highlight: colorHelper.shade(dashboardChartColors.i, 15),
                      label: appLogin.application_id,
                      login_count: appLogin.login_count,
                      order: i++,
                  });

                }

                this._loadDoughnutCharts();
              },
              err => { this.data_error = true }
          );
  }

  private _loadDoughnutCharts() {
    let el = jQuery('.chart-area').get(0) as HTMLCanvasElement;
    new Chart(el.getContext('2d')).Doughnut(this.doughnutData, {
      segmentShowStroke: false,
      percentageInnerCutout : 45,
      responsive: true
    });
  }
}
