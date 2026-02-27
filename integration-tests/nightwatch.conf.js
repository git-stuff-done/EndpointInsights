// Refer to the online docs for more details:
// https://nightwatchjs.org/gettingstarted/configuration/
//

//  _   _  _         _      _                     _          _
// | \ | |(_)       | |    | |                   | |        | |
// |  \| | _   __ _ | |__  | |_ __      __  __ _ | |_   ___ | |__
// | . ` || | / _` || '_ \ | __|\ \ /\ / / / _` || __| / __|| '_ \
// | |\  || || (_| || | | || |_  \ V  V / | (_| || |_ | (__ | | | |
// \_| \_/|_| \__, ||_| |_| \__|  \_/\_/   \__,_| \__| \___||_| |_|
//             __/ |
//            |___/

module.exports = {
    // An array of folders (excluding subfolders) where your tests are located;
    // if this is not specified, the test source must be passed as the second argument to the test runner.
    src_folders: ['test'],

    // See https://nightwatchjs.org/guide/concepts/page-object-model.html
    page_objects_path: ['nightwatch/page-objects'],

    // See https://nightwatchjs.org/guide/extending-nightwatch/adding-custom-commands.html
    custom_commands_path: ['nightwatch/custom-commands'],

    // See https://nightwatchjs.org/guide/extending-nightwatch/adding-custom-assertions.html
    custom_assertions_path: ['nightwatch/custom-assertions'],

    // See https://nightwatchjs.org/guide/extending-nightwatch/adding-plugins.html
    plugins: [],

    // See https://nightwatchjs.org/guide/concepts/test-globals.html
    globals_path: '',

    // Set this to true to disable bounding boxes on terminal output. Useful when running in some CI environments.
    disable_output_boxes: false,

    webdriver: {},

    test_workers: {
        enabled: true
    },

    test_settings: {
        default: {
            disable_error_log: false,
            launch_url: 'http://localhost:8080',

            screenshots: {
                enabled: false,
                path: 'screens',
                on_failure: true
            },

            desiredCapabilities: {
                browserName: 'chrome'
            },

            webdriver: {
                start_process: true,
                server_path: ''
            },

        },

        chrome: {
            desiredCapabilities: {
                browserName: 'chrome',
                'goog:chromeOptions': {
                    // More info on Chromedriver: https://sites.google.com/a/chromium.org/chromedriver/
                    args: [
                        '--no-sandbox',
                        '--ignore-certificate-errors',
                        '--allow-insecure-localhost',
                        '--headless=new',
                        '--disable-dev-shm-usage',
                        '--disable-gpu'
                    ]
                }
            },

            webdriver: {
                start_process: true,
                server_path: '',
                cli_args: [
                    // --verbose
                ]
            }
        },

    },

};
