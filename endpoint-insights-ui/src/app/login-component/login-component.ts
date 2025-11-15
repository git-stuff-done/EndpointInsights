import { Component } from '@angular/core';
import {AuthenticationService} from "../services/authentication.service";

@Component({
  selector: 'app-login',
  imports: [],
  templateUrl: './login-component.html',
  styleUrl: './login-component.scss'
})
export class LoginComponent {

  constructor(private authService: AuthenticationService) {}

  public login():void {
    this.authService.login();
  }

}
