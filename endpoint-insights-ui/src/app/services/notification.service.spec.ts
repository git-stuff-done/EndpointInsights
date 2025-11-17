import { TestBed } from '@angular/core/testing';
import { NotificationService} from './notification.service';
import { ToastService } from './toast.service';

describe('NotificationService', () => {
    let service: NotificationService;
    let toastSpy: jasmine.SpyObj<ToastService>;

    beforeEach(() => {
        toastSpy = jasmine.createSpyObj('ToastService', ['onSuccess', 'onError']);
        TestBed.configureTestingModule({
            providers: [
                NotificationService,
                { provide: ToastService, useValue: toastSpy }
            ]
        });
        service = TestBed.inject(NotificationService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should emit a banner notification', (done) => {
        service.banner$.subscribe(value => {
            expect(value).toEqual({ type: 'info', message: 'Hello' });
            done();
        });
        service.showBanner('Hello');
    });

    it('should clear the banner', (done) => {
        service.banner$.subscribe(value => {
            expect(value).toBeNull();
            done();
        });
        service.clearBanner();
    });

    it('should call toastService.onSuccess for success type', () => {
        service.showToast('Yay', 'success');
        expect(toastSpy.onSuccess).toHaveBeenCalledWith('Yay', undefined);
    });

    it('should call toastService.onError for error type', () => {
        service.showToast('Oops', 'error', 2000);
        expect(toastSpy.onError).toHaveBeenCalledWith('Oops', 2000);
    });

    it('should fallback to onSuccess for info/warning', () => {
        service.showToast('InfoMsg', 'info');
        expect(toastSpy.onSuccess).toHaveBeenCalledWith('[INFO] InfoMsg', undefined);

        service.showToast('WarnMsg', 'warning', 3000);
        expect(toastSpy.onSuccess).toHaveBeenCalledWith('[WARNING] WarnMsg', 3000);
    });
});
