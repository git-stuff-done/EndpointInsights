describe('Application Smoke Tests', function() {
    it('authenticates with identity provider', function(browser) {
        const baseUrl = process.env.APP_URL || browser.launch_url || 'http://localhost:8080';
        const port = new URL(baseUrl).port || '8080';

        browser
            .authenticateWithAuthelia(baseUrl)
            .assert.urlContains(`localhost:${port}`)
            .assert.elementPresent('#dashboard-title')
    })
})