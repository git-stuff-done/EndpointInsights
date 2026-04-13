describe('Application Smoke Tests', function() {
    it('authenticates with identity provider', function(browser) {
        browser
            .authenticateWithAuthelia('http://localhost:8080')
            .assert.urlContains('localhost:4200')
            .assert.elementPresent('#dashboard-title')
    })
})