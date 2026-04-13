describe('Application Smoke Tests', function() {
    it('authenticates with identity provider', function(browser) {
        const baseUrl = process.env.APP_URL || browser.launch_url || 'http://localhost:8080';

        browser
            .authenticateWithAuthelia(baseUrl)
            .assert.urlContains(':8080')
            .assert.elementPresent('#dashboard-title')
    })
})