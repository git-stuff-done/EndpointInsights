import { Routes } from '@angular/router';
import {PageNotFoundComponent} from "./page-not-found-component/page-not-found-component";
import {DashboardComponent} from "./dashboard-component/dashboard-component";

export const routes: Routes = [
    { path: '', component: DashboardComponent },
    { path: '**', component: PageNotFoundComponent }
];
