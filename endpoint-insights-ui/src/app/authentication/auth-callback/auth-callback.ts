import { Component } from '@angular/core';
import {AuthenticationService} from "../../services/authentication.service";

@Component({
  selector: 'app-auth-callback',
  imports: [],
  templateUrl: './auth-callback.html',
  styleUrl: './auth-callback.scss'
})
export class AuthCallback {

  constructor(private authService: AuthenticationService) {

  }

}
