# Endpoint Insights
Endpoint Insights is a performance and integration testing dashboard that we are building for our senior project.
Our client, VSP, is having us reimplement (from scratch) a dashboard that they used previously to schedule and execute api performance and integration testing.

## General Application Flow
### Creating and Running a Test
- User will authenticate against external identity provider
- User will configure a new test, giving it a name, description, specifying the URL of the git repo, a command to compile, and a command to run the test
- User will add the new test to a list of multiple tests and configure pass/fail thresholds for the tests
- User will specify a schedule to run a single or batch of tests
- When the test is scheduled to run, the git repo for the tests will be pulled, built, run, and the results interpreted and will populate the test info and optionally trigger alerts on pass/fail thresholds

## Testing
TBD

## Deployment
TBD

## Contributing
TBD

## Documentation

For detailed information about the system architecture, deployment, and authentication:

**[View Full Documentation](docs/index.md)**

## Contributors

- Brynn Crowley
- Nicholas Cooper
- Tyler Mains
- Caleb Brock
- Marcos Pantoja
- Daniel Carello
- Cardell Rankin
- Jino Enriquez
