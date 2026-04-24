describe('Application Smoke Tests', function() {
    it('authenticates with identity provider', function(browser) {
        const baseUrl = process.env.APP_URL || browser.launch_url || 'https://d2wravsw1nwfu2.cloudfront.net';

        browser
            .authenticateWithAuthelia(baseUrl)
            .assert.elementPresent('[data-test-id="dashboard-title"]')
    })
})