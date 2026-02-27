describe('Application Smoke Tests', function() {
    it('loads and redirects to IdP for authentication', function(browser) {
        browser
            .navigateTo('http://localhost:8080')
            .waitForElementVisible('body')
            .assert.urlContains('auth.crowleybrynn.com')
    })
})