const APP_URL = 'http://localhost:8080';

describe('Test Results List Page', function () {

  before(function (browser) {
    browser
      .injectAuthToken()
      .navigateTo(`${APP_URL}/test-results`);
  });

  after(function (browser) {
    browser.end();
  });

  // --- Layout / static structure ---

  it('renders the page container and title', function (browser) {
    const page = browser.page['test-results'].list();
    page
      .waitForElementVisible('@pageContainer')
      .assert.textContains('@pageTitle', 'Test Results');
  });

  it('renders the search field', function (browser) {
    const page = browser.page['test-results'].list();
    page
      .waitForElementVisible('@searchWrapper')
      .assert.elementPresent('@searchInput');
  });

  // --- Table load ---

  it('loading spinner disappears and results table appears', function (browser) {
    const page = browser.page['test-results'].list();
    page
      .waitForElementNotPresent('@loadingState', 10000)
      .waitForTable()
      .assert.elementPresent('@headerRow');
  });

  it('renders the expected column headers', function (browser) {
    browser
      .waitForElementVisible('tr[mat-header-row]')
      .assert.textContains('th[mat-header-cell]:nth-child(1)', 'Batch Name')
      .assert.textContains('th[mat-header-cell]:nth-child(2)', 'Test Name')
      .assert.textContains('th[mat-header-cell]:nth-child(7)', 'Status');
  });

  it('renders at least one data row', function (browser) {
    browser
      .waitForElementVisible('tr[mat-row]')
      .assert.elementPresent('tr[mat-row]');
  });

  it('renders status pills on data rows', function (browser) {
    browser
      .waitForElementVisible('.status-pill')
      .assert.elementPresent('.status-pill');
  });

  it('status pills carry a recognised CSS modifier class', function (browser) {
    browser.getAttribute('.status-pill', 'class', function (result) {
      const cls = result.value;
      const valid = ['status-pass', 'status-fail', 'status-warn', 'status-unknown']
        .some(c => cls.includes(c));
      browser.assert.ok(valid, `status pill has a recognised CSS modifier class: ${cls}`);
    });
  });

  // --- Search / filter ---

  it('shows the no-results row when search term matches nothing', function (browser) {
    const page = browser.page['test-results'].list();
    page.search('__nonexistent_xyz_123__');
    browser
      .waitForElementVisible('.no-data-row')
      .assert.textContains('.no-data-row', 'No results match');
  });

  it('restores data rows when search is cleared', function (browser) {
    const page = browser.page['test-results'].list();
    page
      .clearSearch()
      .waitForTable();
    browser.assert.not.elementPresent('.no-data-row');
  });

  it('?runId= query param pre-populates the search input', function (browser) {
    browser
      .navigateTo(`${APP_URL}/test-results?runId=abc123`)
      .waitForElementVisible('.search-field input')
      .getValue('.search-field input', function (result) {
        this.assert.equal(result.value, 'abc123');
      });
  });

  // --- Navigation ---

  it('clicking view result button navigates to /test-results/view', function (browser) {
    browser
      .navigateTo(`${APP_URL}/test-results`)
      .waitForElementVisible('tr[mat-row]')
      .strictClick('tr[mat-row]:first-of-type button[aria-label="View test result"]')
      .assert.urlContains('/test-results/view');
  });
});
