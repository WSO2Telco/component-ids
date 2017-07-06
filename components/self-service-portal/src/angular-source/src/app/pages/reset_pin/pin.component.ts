import {Component} from '@angular/core';
import {FormGroup, AbstractControl, FormBuilder, Validators} from '@angular/forms';
import {EmailValidator, EqualPasswordsValidator} from '../../theme/validators';
import {Http, Response} from '@angular/http';

import 'style-loader!./pin.scss';
import {AppSettings} from "../../app.settings";

@Component({
  selector: 'pin-reset',
  templateUrl: './pin.html',
})
export class PinReset {

  public form:FormGroup;
  public current:AbstractControl;
  public newPin:AbstractControl;
  public repeatNewPin:AbstractControl;
  public newPins:FormGroup;

  public submitted:boolean = false;

  constructor(fb:FormBuilder, private http: Http) {

    this.form = fb.group({
      'current': ['', Validators.compose([Validators.required, Validators.minLength(4)])],
      'newPins': fb.group({
        'newPin': ['', Validators.compose([Validators.required, Validators.minLength(4)])],
        'repeatNewPin': ['', Validators.compose([Validators.required, Validators.minLength(4)])]
      }, {validator: EqualPasswordsValidator.validate('newPin', 'repeatNewPin')})
    });

    this.current = this.form.controls['current'];
    this.newPins = <FormGroup> this.form.controls['newPins'];
    this.newPin = this.newPins.controls['newPin'];
    this.repeatNewPin = this.newPins.controls['repeatNewPin'];
  }

  public onSubmit(values:Object):void {
    this.submitted = true;
    if (this.form.valid) {
      let current_pin = values['current'];
      let new_pin = values['newPins']['newPin'];
      let token = localStorage.getItem('access_token');

      this.http.post(AppSettings.BASE_API + 'user/pin_reset', 'access_token='+ token + '&current=' + current_pin + '&new_pin=' + new_pin).map((response:Response) => {
                console.log(response.json());
                // handle response here
                //response.json();
            }).subscribe();
    }
  }
}
