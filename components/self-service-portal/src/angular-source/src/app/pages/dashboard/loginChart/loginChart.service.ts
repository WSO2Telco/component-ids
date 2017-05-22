import {Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';
import {CommonService} from "../../../common.service";

@Injectable()
export class LoginChartService {

  constructor(private commonService:CommonService) {
  }

  getData() {
    return this.commonService.get('user/app_logins').map((res:Response) => res.json());
  }
}
