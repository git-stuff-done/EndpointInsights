/**
 * Page object for the Test Results view page (/test-results/view).
 *
 * Usage:
 *   const page = browser.page['test-results'].view();
 *   page.navigateToRun('some-uuid').waitForMetadata();
 */

const commands = {
  /**
   * Navigates to the view page with a run ID as a query parameter.
   * This is the most reliable approach for direct navigation since
   * window.history.state is not preserved across fresh navigations.
   */
  navigateToRun(runId) {
    return this.navigateTo(`http://localhost:8080/test-results/view?id=${runId}`);
  },

  waitForMetadata() {
    return this.waitForElementVisible('@runMeta', 10000);
  },

  waitForTable() {
    return this.waitForElementVisible('@matTable', 10000);
  }
};

module.exports = {
  url: 'http://localhost:8080/test-results/view',

  commands: [commands],

  elements: {
    bodyContainer:   { selector: '.body-container' },
    resultContainer: { selector: '.result-container' },
    runMeta:         { selector: '.run-meta' },
    metaPills:       { selector: '.meta-pill' },
    statusBadge:     { selector: '.status-badge' },
    matTable:        { selector: 'mat-table' },
    headerRow:       { selector: 'mat-header-row' },
    dataRows:        { selector: 'mat-row' },
    paginator:       { selector: 'mat-paginator' },
    errorRateCells:  { selector: '.error-rate' },
  }
};
