import {Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';
import {AppSettings} from "../../../app.settings";

@Injectable()
export class LoginChartService {

  constructor(private http:Http) {
  }

  getData() {
    let token = localStorage.getItem('access_token');
    return this.http.get(AppSettings.BASE_API + 'user/app_logins?access_token='+ token).map((res:Response) => res.json());
  }
}
