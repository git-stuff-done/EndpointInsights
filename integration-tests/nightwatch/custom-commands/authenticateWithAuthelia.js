module.exports = class AuthenticateWithAuthelia {
    async command(startUrl = 'http://localhost:8080') {
        const username = process.env.TEST_USER_USERNAME;
        const password = process.env.TEST_USER_PASSWORD;

        if (!username || !password) {
            throw new Error('TEST_USER_USERNAME and TEST_USER_PASSWORD environment variables must be set');
        }

        this.api
            .navigateTo(startUrl)
            .waitForElementVisible('[data-test-id="sso-login-button"]')
            .click('[data-test-id="sso-login-button"]')
            .waitForElementVisible('body')
            .assert.urlContains('auth.crowleybrynn.com')
            
            .waitForElementVisible('#username-textfield')
            .setValue('#username-textfield', username)
            .setValue('#password-textfield', password)
            
            .click('#sign-in-button')
            
            .waitForElementVisible('body')
            .assert.urlContains('/consent/openid/decision')
            
            .waitForElementVisible('#openid-consent-accept')
            .click('#openid-consent-accept')

            .waitForElementVisible('[data-test-id="dashboard-title"]', 10000);

        return this;
    }
};