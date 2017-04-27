import {Component} from '@angular/core';
import {Http} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {AppSettings} from '../app.settings';

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
        .map(response => response.ok);
      }else{
        return new Observable<boolean>( observer => {
          observer.next(false);
          observer.complete();
        });
      }
  }
}
