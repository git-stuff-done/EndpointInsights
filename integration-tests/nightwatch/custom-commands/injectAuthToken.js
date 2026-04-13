/**
 * Injects a JWT into localStorage so the Angular AuthenticationService
 * picks it up on bootstrap and the auth guard passes.
 *
 * Usage:
 *   browser.injectAuthToken()
 *
 * Requires env var E2E_AUTH_TOKEN to be set to a valid, non-expired JWT
 * with claims: preferred_username, email, groups, exp.
 *
 * The command navigates to the app origin first so localStorage is scoped
 * to the correct origin before writing.
 */

module.exports = {
  command: function () {
    const token = process.env.E2E_AUTH_TOKEN;
    if (!token) {
      throw new Error(
        'E2E_AUTH_TOKEN environment variable is not set. ' +
        'Provide a valid non-expired JWT to run authenticated tests.'
      );
    }
    return this
      .navigateTo('http://localhost:8080')
      .execute(function (jwt) {
        localStorage.setItem('auth-token-storage-ei-endpoints', jwt);
      }, [token]);
  }
};
