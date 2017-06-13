import {Component} from '@angular/core';
import 'style-loader!./loaUpgrade.scss';
import {AppSettings} from "../../../app.settings";

@Component({
  selector: 'loa-upgrade',
  templateUrl: './loaUpgrade.html'
})
export class LoaUpgrade {

  redirectToLoa3() {
    let msisdn = localStorage.getItem('msisdn');
    window.location.href = AppSettings.getAuthUrl(msisdn, "3");
  }
}
