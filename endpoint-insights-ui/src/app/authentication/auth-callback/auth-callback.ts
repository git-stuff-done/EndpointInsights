import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from "../../services/authentication.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-auth-callback',
  imports: [],
  templateUrl: './auth-callback.html',
  styleUrl: './auth-callback.scss'
})
export class AuthCallback implements OnInit {


  constructor(private authService: AuthenticationService, private router: Router) {

  }

  ngOnInit() {
    this.authService.loadTokenFromCookie();
  }

}
