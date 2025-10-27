import {Injectable} from "@angular/core";
import {MatSnackBar} from "@angular/material/snack-bar";
import {TOAST_TIMEOUT} from "../common/constants";
import {ToastComponent} from "../batch-component/components/toast-notification/toast.component";

@Injectable({providedIn: 'root'})

export class ToastService{

    constructor(private toast: MatSnackBar) {}

    onSuccess(message: string, duration: number = TOAST_TIMEOUT){
        this.toast.openFromComponent(ToastComponent, {
            horizontalPosition: "right",
            verticalPosition: "top",
            duration,
            panelClass: ['toast-success'],
            data: {type: 'Success:', message}
        })
    }

    onError(message: string, duration: number = TOAST_TIMEOUT){
        this.toast.openFromComponent(ToastComponent, {
            horizontalPosition: "right",
            verticalPosition: "top",
            duration,
            panelClass: ['toast-error'],
            data: {type: 'Error:', message}
        })
    }

}


