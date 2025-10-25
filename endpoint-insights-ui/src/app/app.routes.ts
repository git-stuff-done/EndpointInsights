import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard-component/dashboard-component';

export const routes: Routes = [
    { path: '', component: DashboardComponent, pathMatch: 'full' },
    { path: 'batches', loadComponent: () => import('./batch-component/batch-component').then(m => m.BatchComponent) },
    { path: '**', loadComponent: () => import('./page-not-found-component/page-not-found-component').then(m => m.PageNotFoundComponent) },
];
