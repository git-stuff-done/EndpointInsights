# Endpoint Insights Architecture

Below you'll find some information on the different entities within our application architecture and some explanations
on how they should work.

## 1. Endpoint Insights DB
This is our database in which we will store our data. This only should be accessed from our endpoint-insights-api application.

## 2. endpoint-insights-ui
This is the UI of the application. This only ever interfaces with the endpoint-insights-api by making REST calls.
Detailed information about which services and components that are used for this process are included in the architecture
diagram.


## 3. endpoint-insights-api
The API of this web application is responsible for two primary functions:
1. Servicing requests made from the endpoint-insights-ui application, and
2. Running jobs

The API, while stateless in handling requests from the UI, is not entirely stateless when running jobs. There is no need for persistent storage, but temporary storage is required.
This will be explained in further detail in the following section.

### Jobs
The secondary function of the API application is to run jobs. When a job is triggered either by a timer or a REST request
the job will immediately begin by following the process described in the Job Run Lifecycle section. It's important to understand
a few key aspects about jobs first for that lifecycle to make sense, though:

### Job properties
1. A job is an abstraction for a program
   1. An example of a job would be a JMeter application, Jbehave application, or Nightwatch e2e application.
2. A job entity contains a reference to its program's source code.
3. A job may only have one active job_run at a time. (i.e. you must wait until a current run of a job is finished before
   invoking another run of the job).

With these properties in mind, the lifecycle of a job run should be more clear.

### Job Run Lifecycle
1. Pull/Clone source belonging to this job using Git. This source code will now be on the API server (this is why the api is not entirely stateless)
2. Build artifacts from the source of this job (this will use npm or Maven to build)
3. Run the resultant artifact belonging to this job (using provided run script)
4. Interpret the results of the job run (parse xml & save important results)