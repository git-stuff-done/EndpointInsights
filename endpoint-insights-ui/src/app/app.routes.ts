import {Routes} from '@angular/router';
import {DashboardComponent} from './dashboard-component/dashboard-component';
import {LoginComponent} from './login-component/login-component';
import {BatchComponent} from "./batch-component/batch-component";
import {TestOverview} from "./pages/test-overview/test-overview";
import {TestResultsPageComponent} from "./pages/Test-Results-Page/tests-results-page";
import {AuthCallback} from "./authentication/auth-callback/auth-callback";
import {PageNotFoundComponent} from "./page-not-found-component/page-not-found-component";
import {authGuard} from "./auth-guard";

export const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: 'auth/callback', component: AuthCallback, pathMatch: 'full'},
    { path: '', component: DashboardComponent, pathMatch: 'full', canActivate: [authGuard] },
    { path: 'batches', component: BatchComponent, canActivate: [authGuard] },
    { path: 'tests', component: TestOverview, canActivate: [authGuard] },
    { path: 'test-results', component: TestResultsPageComponent, canActivate: [authGuard]},
    { path: '**', component: PageNotFoundComponent },
];
