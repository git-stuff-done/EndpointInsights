import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatExpansionPanelHarness } from '@angular/material/expansion/testing';
import { MatDialog } from '@angular/material/dialog';

import { TestResultsCardComponent } from './test-results-card.component';
import { TestRecord } from '../../models/test-record.model';

describe('TestResultsCardComponent', () => {
    let fixture: ComponentFixture<TestResultsCardComponent>;
    let component: TestResultsCardComponent;

    const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);

    // convenience setter
    function setTest(t: Partial<TestRecord>) {
        component.test = {
            id: 'test-1',
            name: 'Login API',
            status: 'PASS',
            // defaults; individual tests overwrite
            latencyMsP50: 42,
            latencyMsP95: 120,
            latencyMsP99: 210,
            volume1m: 530,
            volume5m: 2510,
            errorRatePct: 0.4,
            thresholds: {
                latencyMs: { warn: 200, fail: 400 },
                errorRatePct: { warn: 1.0, fail: 2.5 },
                volumePerMin: { warn: 100, fail: 20 },
            },
            httpBreakdown: [{ code: 200, count: 2478 }, { code: 401, count: 8 }],
            ...t,
        };
        fixture.detectChanges();
    }

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                NoopAnimationsModule,      // make expansion instant/predictable
                MatExpansionModule,        // use metadata (often pulled via a component, but explicit is fine)
                TestResultsCardComponent,         // standalone component (brings its deps)
            ],
        }).compileComponents();

        TestBed.overrideProvider(MatDialog, { useValue: matDialogSpy });

        fixture = TestBed.createComponent(TestResultsCardComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        setTest({}); // uses defaults
        expect(component).toBeTruthy();
    });

    it('renders the name in the banner', () => {
        setTest({ name: 'Test Name' });
        const nameEl = fixture.nativeElement.querySelector('.name') as HTMLElement;
        expect(nameEl?.textContent).toContain('Test Name');
    });

    it('shows status chip text and applies PASS styling class', () => {
        setTest({ status: 'PASS' });
        const host = fixture.nativeElement.querySelector('mat-card') as HTMLElement; // .test-results-card on host
        const chip = fixture.nativeElement.querySelector('mat-chip') as HTMLElement;

        // host banner class
        expect(host.className).toContain('pass');
        // chip class
        expect(chip.className).toContain('chip-pass');
        // chip text
        expect(chip.textContent?.trim()).toBe('PASS');
    });

    it('applies FAIL styling when status=FAIL', () => {
        setTest({ status: 'FAIL' });
        const host = fixture.nativeElement.querySelector('mat-card') as HTMLElement;
        const chip = fixture.nativeElement.querySelector('mat-chip') as HTMLElement;
        expect(host.className).toContain('fail');
        expect(chip.className).toContain('chip-fail');
        expect(chip.textContent?.trim()).toBe('FAIL');
    });

    it('renders banner KVs with full labels', () => {
        setTest({ latencyMsP95: 123, volume1m: 456, errorRatePct: 7.89 });
        const text = (fixture.nativeElement as HTMLElement).textContent || '';
        expect(text).toContain('P95 Latency');
        expect(text).toContain('Volume (last minute)');
        expect(text).toContain('Error Rate');
        expect(text).toContain('123 ms');
        expect(text).toContain('456');
        // pct is formatted by a component; tolerate rounding like "7.89%"
        expect(text).toMatch(/7\.?8?9?%/);
    });

    it('expands when header is clicked and shows description', async () => {
        setTest({ description: 'Auth flow responsiveness.' });
        const loader = TestbedHarnessEnvironment.loader(fixture);
        const panel = await loader.getHarness(MatExpansionPanelHarness);

        // initially collapsed
        expect(await panel.isExpanded()).toBeFalse();

        await panel.expand();
        fixture.detectChanges();

        expect(await panel.isExpanded()).toBeTrue();

        // description should now be visible
        const desc = fixture.nativeElement.querySelector('.description') as HTMLElement;
        expect(desc?.textContent).toContain('Auth flow responsiveness.');
    });

    it('adds .is-expanded class to host when expanded', async () => {
        setTest({});
        const loader = TestbedHarnessEnvironment.loader(fixture);
        const panel = await loader.getHarness(MatExpansionPanelHarness);

        const host = fixture.nativeElement.querySelector('mat-card') as HTMLElement;
        expect(host.classList.contains('is-expanded')).toBeFalse();

        await panel.expand();
        fixture.detectChanges();

        expect(host.classList.contains('is-expanded')).toBeTrue();
    });

    it('renders Thresholds table with Name | Warn | Fail columns', async () => {
        setTest({
            thresholds: {
                latencyMs: { warn: 200, fail: 400 },
                errorRatePct: { warn: 1.5, fail: 3.5 },
                volumePerMin: { warn: 100, fail: 20 },
            },
        });
        const loader = TestbedHarnessEnvironment.loader(fixture);
        const panel = await loader.getHarness(MatExpansionPanelHarness);
        await panel.expand();
        fixture.detectChanges();

        const text = (fixture.nativeElement as HTMLElement).textContent || '';
        expect(text).toContain('Thresholds');
        expect(text).toContain('Name');
        expect(text).toContain('Warn Threshold');
        expect(text).toContain('Fail Threshold');

        // spot-check a couple of values
        expect(text).toContain('Latency (ms)');
        expect(text).toContain('200 ms');
        expect(text).toContain('400 ms');

        expect(text).toContain('Error Rate (%)');
        expect(text).toMatch(/1\.5%/);
        expect(text).toMatch(/3\.5%/);
    });

    it('renders HTTP responses table rows from httpBreakdown', async () => {
        setTest({ httpBreakdown: [{ code: 200, count: 5 }, { code: 500, count: 2 }] });
        const loader = TestbedHarnessEnvironment.loader(fixture);
        const panel = await loader.getHarness(MatExpansionPanelHarness);
        await panel.expand();
        fixture.detectChanges();

        const rows = fixture.nativeElement.querySelectorAll('table.http-table tr[mat-row]');
        // 2 data rows expected
        expect(rows.length).toBe(2);
        const text = (fixture.nativeElement as HTMLElement).textContent || '';
        expect(text).toContain('200');
        expect(text).toContain('5');
        expect(text).toContain('500');
        expect(text).toContain('2');
    });

    it('loads logs on first open', fakeAsync(() => {
        setTest({});
        expect(component.logsText).toBe('');

        component.onOpened();
        tick(0);
        fixture.detectChanges();

        expect(component.logsText.length).toBeGreaterThan(0);
      }));

      it('does not reload logs if already loaded', fakeAsync(() => {
        setTest({});
        component.onOpened();
        tick(0);
        fixture.detectChanges();

        const first = component.logsText;

        component.onClosed();
        component.onOpened();
        tick(0);
        fixture.detectChanges();

        expect(component.logsText).toBe(first);
      }));

      it('openLogsModal opens a dialog', () => {
        setTest({});
        // ensure something exists
        component.logsText = 'preview';
        (component as any).fullLogsText = 'full'; // if you added full logs

        // call your method (with or without param depending on your signature)
        component.openLogsModal();

        expect(matDialogSpy.open).toHaveBeenCalled();
      });
});
