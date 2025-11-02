import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard-component/dashboard-component';
import { LoginComponent } from './login-component/login-component';
export const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: '', component: DashboardComponent, pathMatch: 'full' },
    { path: 'batches', loadComponent: () => import('./batch-component/batch-component').then(m => m.BatchComponent) },
    { path: 'tests', loadComponent: () => import('./pages/test-overview/test-overview').then(m => m.TestOverview) },
    { path: '**', loadComponent: () => import('./page-not-found-component/page-not-found-component').then(m => m.PageNotFoundComponent) },
];
