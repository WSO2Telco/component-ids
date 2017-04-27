import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './auth.service';
import 'rxjs/add/operator/toPromise';

@Injectable()
export class ProtectedRouteCanActivate implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate() {
     return true; //this.authService.isLoggedIn();
  }
}
