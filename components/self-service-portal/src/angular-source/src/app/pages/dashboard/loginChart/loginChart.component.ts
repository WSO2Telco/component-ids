import {Component} from '@angular/core';

import {LoginChartService} from "./loginChart.service";
import * as Chart from 'chart.js';

import 'style-loader!./loginChart.scss';

@Component({
  selector: 'login-chart',
  templateUrl: './loginChart.html'
})

// TODO: move chart.js to it's own component
export class LoginChart {

  public doughnutData: Array<Object>;
  public data_error:Boolean = false;

  constructor(private loginChartService:LoginChartService) {
      this.doughnutData = [];

      loginChartService.getData().subscribe(
              data => {
                let i = 0;
                let appLoginData = data['data']['items'];
                for (let appLogin of appLoginData) {

                  this.doughnutData.push({
                      value: appLogin.login_count,
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
      percentageInnerCutout : 64,
      responsive: true
    });
  }
}
