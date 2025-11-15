import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from "../../services/authentication.service";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../../environment";

@Component({
  selector: 'app-auth-callback',
  imports: [],
  templateUrl: './auth-callback.html',
  styleUrl: './auth-callback.scss'
})
export class AuthCallback implements OnInit {

  private static readonly AUTH_TOKEN_COOKIE = "authToken";

  constructor(private authService: AuthenticationService, private httpClient: HttpClient) {

  }

  ngOnInit() {
    const token = window.cookieStore.get(AuthCallback.AUTH_TOKEN_COOKIE);
    token.then(t => {
      if (t?.value) {
        this.authService.setToken(t.value);
        this.authService.loadUserInfo();
      }

    }).catch(e => console.log(e));
  }

}
