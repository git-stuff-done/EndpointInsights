describe('Batch Management Tests', function() {
    const testData = {
        batchId: null,
        batchName: `test_batch_${Date.now()}`,
        email: 'test@example.com'
    };

    before(function(browser) {
        const baseUrl = process.env.APP_URL || browser.launch_url || 'http://localhost:8080';
        browser
            .authenticateWithAuthelia(baseUrl)
            .assert.urlContains(':8080')
            .waitForElementVisible('body');
    });

    it('navigates to batches page', function(browser) {
        browser
            .click('a[href*="batches"]')
            .assert.urlContains('/batches')
            .assert.elementPresent('#batches-table');
    });

    it('creates a new batch successfully', function(browser) {
        browser
            .click('button[data-test="new-batch"]')
            .waitForElementVisible('[data-test-id="batch-name-input"]')

            .setValue('[data-test-id="batch-name-input"]', testData.batchName)

            .click('[data-test-id="batch-frequency-select"]')
            .pause(10)
            .click('xpath', '//mat-option[contains(., "Weekly")]')
            .keys(browser.Keys.ESCAPE)
            .pause(10)

            .click('[data-test-id="schedule-days-select"]')
            .pause(10)
            .click('xpath', '//mat-option[contains(., "Monday")]')
            .click('xpath', '//mat-option[contains(., "Wednesday")]')
            .click('xpath', '//mat-option[contains(., "Friday")]')
            .sendKeys('[data-test-id="schedule-days-select"]', browser.Keys.TAB)
            .pause(10)

            .setValue('[data-test-id="schedule-time-picker"]', '14:30')

            .setValue('[data-test-id="batch-email-input"]', testData.email)
            .pause(10)
            .click('[data-test-id="add-email-button"]')
            .pause(10)

            .assert.textContains('.job-row', testData.email)

            .setValue('[data-test-id="search-available-tests-input"]', 'test')
            .pause(10)
            .click('[data-test-id="add-test-button"]')
            .pause(10)

            .click('[data-test-id="save-dialog-button"]')
            .pause(100)
            .waitForElementVisible('[data-test-id="toast-message"]', 3000)
            .assert.textContains('[data-test-id="toast-message"]', 'Successfully saved batch item')
    });

    it('finds the newly created batch in the table', function(browser) {
        browser
            .setValue('[data-test-id="search-batches-input"]', testData.batchName)
            .pause(10)

            .assert.elementPresent(`[data-test-id="batch-row-${testData.batchName}"]`)

            .execute(function(batchName) {
                const row = document.querySelector(`[data-test-id="batch-row-${batchName}"]`);
                return row?.querySelector('.cell.id')?.textContent.trim();
            }, [testData.batchName], function(result) {
                testData.batchId = result.value;
                console.log('Captured batch ID:', testData.batchId);
            });
    });

    it('opens batch for editing', function(browser) {
        browser
            .click(`[data-test-id="edit-batch-button-${testData.batchName}"]`)
            .waitForElementVisible('[data-test-id="batch-name-input"]')

            .getValue('[data-test-id="batch-name-input"]', function(result) {
                this.assert.equal(result.value, testData.batchName);
            })

            .assert.textContains('[data-test-id="batch-frequency-select"]', 'Weekly')
            .assert.textContains('.job-row', testData.email);
    });

    it('updates batch values successfully', function(browser) {
        browser
            .click('[data-test-id="batch-frequency-select"]')
            .pause(10)
            .click('xpath', '//mat-option[contains(., "Daily")]')
            .pause(10)

            .clearValue('[data-test-id="schedule-time-picker"]')
            .setValue('[data-test-id="schedule-time-picker"]', '09:00')

            .setValue('[data-test-id="batch-email-input"]', 'another@example.com')
            .click('[data-test-id="add-email-button"]')
            .pause(10)

            .click('[data-test-id="save-dialog-button"]')
            .pause(1000);
    });

    it('verifies updated values persisted', function(browser) {
        browser
            .click(`[data-test-id="edit-batch-button-${testData.batchName}"]`)
            .waitForElementVisible('[data-test-id="batch-name-input"]')

            .assert.textContains('[data-test-id="batch-frequency-select"]', 'Daily')

            .elements('xpath', '//div[@class="job-row"]//span[contains(., "@")]', function(result) {
                this.assert.equal(result.value.length, 2, 'Should have 2 email recipients');
            })

            .click('[data-test-id="cancel-dialog-button"]')
            .pause(10);
    });

    it('runs the batch', function(browser) {
        browser
            .click(`[data-test-id="run-batch-button-${testData.batchName}"]`)
            .pause(1000)
            .waitForElementVisible('[data-test-id="toast-message"]', 3000)
            .assert.textContains('[data-test-id="toast-message"]', 'Run started with id:')

    });

    it('deletes the test batch successfully', function(browser) {
        browser
            .click(`[data-test-id="delete-batch-button-${testData.batchName}"]`)
            .pause(10)

            .waitForElementVisible('[data-test-id="confirm-delete-button"]')
            .click('[data-test-id="confirm-delete-button"]')
            .pause(100)
            .waitForElementVisible('[data-test-id="toast-message"]', 3000)
            .assert.textContains('[data-test-id="toast-message"]', 'Successfully deleted batch');
    });

    it('verifies batch is no longer in the list', function(browser) {
        browser
            .pause(10)
            .clearValue('[data-test-id="search-batches-input"]')
            .setValue('[data-test-id="search-batches-input"]', testData.batchName)
            .pause(10)

            .elements('css selector', `[data-test-id="batch-row-${testData.batchName}"]`, function(result) {
                this.assert.equal(result.value.length, 0, 'Batch should be deleted');
            });
    });

    after(function(browser) {
        browser.end();
    });
});