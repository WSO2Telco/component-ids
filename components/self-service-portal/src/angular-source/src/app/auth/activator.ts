import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/catch';

@Injectable()
export class ProtectedRouteCanActivate implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate() {
    //return true;
    return this.authService.isLoggedIn();
  }
}
