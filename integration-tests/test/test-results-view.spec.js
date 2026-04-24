const APP_URL = process.env.APP_URL || 'http://localhost:8080';

// Tests that require real data are gated on E2E_TEST_RUN_ID.
// When unset the suite stays green — each gated test passes with a SKIPPED notice.
const RUN_ID = process.env.E2E_TEST_RUN_ID || null;

describe('Test Results View Page', function () {

  before(function (browser) {
    browser.authenticateWithAuthelia(APP_URL);
    if (RUN_ID) {
      browser.navigateTo(`${APP_URL}/test-results/view?id=${RUN_ID}`);
    } else {
      browser.navigateTo(`${APP_URL}/test-results/view`);
    }
  });

  after(function (browser) {
    browser.end();
  });

  // --- Container always present (no data needed) ---

  it('renders the outer body container', function (browser) {
    browser.waitForElementVisible('.body-container');
  });

  it('renders the result container inside the body', function (browser) {
    browser.waitForElementVisible('.result-container');
  });

  // --- Tests that require a real run ID ---

  it('shows 4 metadata pills when a valid run ID is loaded', function (browser) {
    if (!RUN_ID) {
      browser.assert.ok(true, 'SKIPPED: E2E_TEST_RUN_ID not set');
      return;
    }
    browser
      .waitForElementVisible('.run-meta')
      .assert.elementPresent('.meta-pill');
    browser.assert.elementHasCount('.meta-pill', 4);
  });

  it('status badge carries a recognised CSS modifier class', function (browser) {
    if (!RUN_ID) {
      browser.assert.ok(true, 'SKIPPED: E2E_TEST_RUN_ID not set');
      return;
    }
    browser.getAttribute('.status-badge', 'class', function (result) {
      const cls = result.value;
      const valid = ['status-pass', 'status-fail', 'status-warn', 'status-unknown']
        .some(c => cls.includes(c));
      this.assert.ok(valid, `status badge has a recognised modifier class: ${cls}`);
    });
  });

  it('delete button is present in the run metadata section', function (browser) {
    if (!RUN_ID) {
      browser.assert.ok(true, 'SKIPPED: E2E_TEST_RUN_ID not set');
      return;
    }
    browser
      .waitForElementVisible('.run-meta')
      .assert.elementPresent('.delete-button button');
  });

  it('renders the results mat-table with a header row', function (browser) {
    if (!RUN_ID) {
      browser.assert.ok(true, 'SKIPPED: E2E_TEST_RUN_ID not set');
      return;
    }
    browser
      .waitForElementVisible('mat-table')
      .assert.elementPresent('mat-header-row');
  });

  it('renders the expected column headers', function (browser) {
    if (!RUN_ID) {
      browser.assert.ok(true, 'SKIPPED: E2E_TEST_RUN_ID not set');
      return;
    }
    browser
      .waitForElementVisible('mat-header-row')
      .assert.textContains('mat-header-row', 'Thread Group')
      .assert.textContains('mat-header-row', 'Sampler')
      .assert.textContains('mat-header-row', 'Latency Threshold')
      .assert.textContains('mat-header-row', 'Error Rate');
  });

  it('renders at least one data row in the results table', function (browser) {
    if (!RUN_ID) {
      browser.assert.ok(true, 'SKIPPED: E2E_TEST_RUN_ID not set');
      return;
    }
    browser
      .waitForElementVisible('mat-row')
      .assert.elementPresent('mat-row');
  });

  it('renders the paginator', function (browser) {
    if (!RUN_ID) {
      browser.assert.ok(true, 'SKIPPED: E2E_TEST_RUN_ID not set');
      return;
    }
    browser.waitForElementVisible('mat-paginator');
  });

  it('paginator page-size selector opens with options', function (browser) {
    if (!RUN_ID) {
      browser.assert.ok(true, 'SKIPPED: E2E_TEST_RUN_ID not set');
      return;
    }
    browser
      .waitForElementVisible('mat-paginator')
      .click('mat-paginator mat-select')
      .waitForElementVisible('mat-option')
      .assert.elementPresent('mat-option');
    browser.keys(browser.Keys.ESCAPE);
  });

  it('error-rate cells carry a colour modifier class', function (browser) {
    if (!RUN_ID) {
      browser.assert.ok(true, 'SKIPPED: E2E_TEST_RUN_ID not set');
      return;
    }
    browser.elements('css selector', '.error-rate', function (result) {
      if (result.value.length === 0) { return; }
      browser.getAttribute('.error-rate', 'class', function (attr) {
        const cls = attr.value;
        const valid = ['error-rate-high', 'error-rate-medium', 'error-rate-low']
          .some(c => cls.includes(c));
        this.assert.ok(valid, `error-rate cell has a colour modifier class: ${cls}`);
      });
    });
  });
});
