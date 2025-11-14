import {Routes} from '@angular/router';
import {DashboardComponent} from './dashboard-component/dashboard-component';
import {LoginComponent} from './login-component/login-component';
import {BatchComponent} from "./batch-component/batch-component";
import {TestOverview} from "./pages/test-overview/test-overview";
import {AuthCallback} from "./authentication/auth-callback/auth-callback";
import {PageNotFoundComponent} from "./page-not-found-component/page-not-found-component";

export const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: '', component: DashboardComponent, pathMatch: 'full' },
    { path: 'batches', component: BatchComponent },
    { path: 'tests', component: TestOverview },
    { path: 'auth/callback', component: AuthCallback, pathMatch: 'full'},
    { path: '**', component: PageNotFoundComponent },
];
