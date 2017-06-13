import { Injectable } from '@angular/core';
import {AppSettings} from "./app.settings";
import {Http} from '@angular/http';

@Injectable()
export class CommonService {

  constructor(private http: Http) {
  }

  get(path :string, queryString?: string){
    let access_token = localStorage.getItem('access_token');
    return this.http.get(AppSettings.BASE_API + path + '?access_token=' + access_token + '&' + queryString);
  }

  post(path :string, queryString: string){
    let access_token = localStorage.getItem('access_token');
    return this.http.get(AppSettings.BASE_API + path + '?access_token=' + access_token + '&' + queryString);
  }
}
