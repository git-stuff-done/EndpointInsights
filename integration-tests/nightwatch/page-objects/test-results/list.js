/**
 * Page object for the Test Results list page (/test-results).
 *
 * Usage:
 *   const page = browser.page['test-results'].list();
 *   page.waitForTable().search('myTest');
 */

const commands = {
  waitForTable() {
    return this.waitForElementVisible('@resultsTable', 10000);
  },

  /**
   * Types a search term into the filter input.
   * Pauses 300ms after typing to allow the 200ms debounce to settle.
   */
  search(term) {
    return this
      .waitForElementVisible('@searchInput')
      .clearValue('@searchInput')
      .setValue('@searchInput', term)
      .pause(300);
  },

  /**
   * Clears the search input and waits for the debounce.
   */
  clearSearch() {
    return this
      .clearValue('@searchInput')
      .pause(300);
  }
};

module.exports = {
  url: 'http://localhost:8080/test-results',

  commands: [commands],

  elements: {
    pageContainer:      { selector: 'main.test-results-page' },
    pageTitle:          { selector: 'h1.page-title' },
    searchWrapper:      { selector: '.search-wrapper' },
    searchInput:        { selector: '.search-field input' },
    resultsTable:       { selector: 'table.results-table' },
    headerRow:          { selector: 'tr[mat-header-row]' },
    dataRows:           { selector: 'tr[mat-row]' },
    loadingState:       { selector: '.loading-state' },
    errorState:         { selector: '.error-state' },
    noDataRow:          { selector: '.no-data-row' },
    statusPill:         { selector: '.status-pill' },
    statusPass:         { selector: '.status-pill.status-pass' },
    statusFail:         { selector: '.status-pill.status-fail' },
    statusWarn:         { selector: '.status-pill.status-warn' },
    statusUnknown:      { selector: '.status-pill.status-unknown' },
    batchNameCell:      { selector: '.batch-name' },
    testNameCell:       { selector: '.test-name' },
    runIdCell:          { selector: '.mono.truncate' },
    viewButton:         { selector: 'button[aria-label="View test result"]' },
    firstRowViewButton: { selector: 'tr[mat-row]:first-of-type button[aria-label="View test result"]' },
  }
};
