import {Component} from '@angular/core';
import {Http, Response, Headers, RequestOptions} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {AppSettings} from '../app.settings';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';

@Component({
  selector: 'auth',
})
export class AuthService {
  constructor(private http: Http) {
  }

  public isLoggedIn():Observable<boolean>{
      let access_token = localStorage.getItem('access_token');

      if(access_token != null){
        return this.http.get(AppSettings.BASE_API + 'auth/validate?access_token=' + access_token)
        .map((res:Response) => res.ok)
        .catch(() => {
          return new Observable<boolean>( observer => {
            observer.next(false);
            observer.complete();
          });
        });
      }else{
        return new Observable<boolean>( observer => {
          observer.next(false);
          observer.complete();
        });
      }
  }
}
