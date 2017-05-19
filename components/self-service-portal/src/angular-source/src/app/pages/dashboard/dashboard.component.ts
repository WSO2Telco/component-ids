import {Component} from '@angular/core';
import {Http, Response} from '@angular/http';

import {AppSettings} from "../../app.settings";

@Component({
  selector: 'dashboard',
  styleUrls: ['./dashboard.scss'],
  templateUrl: './dashboard.html'
})
export class Dashboard {

  public showPinReset = false;

  constructor(private http: Http) {

    let token = localStorage.getItem('access_token');
    this.http.get(AppSettings.BASE_API + 'user/loa?access_token='+ token).map((response:Response) => {
              let data = response.json();
              this.showPinReset = data.data.loa == "2";
              console.log(this.showPinReset);
          }).subscribe();
  }

}
