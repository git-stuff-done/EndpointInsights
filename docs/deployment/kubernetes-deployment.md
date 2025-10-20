# Kubernetes Deployment

## Deployment Strategy

We are planning to setup automatic deployments once the application is in a sufficiently advanced state that we have something to deploy.

### Environments
- **Development:** Auto-deploy from develop branch
- **Production:** Manual promotion from main branch

## Deployment Manifests

These are kept in a separate repository along with minimal helm templates.
Dev/Prod deployments will be managed in GitOps using ArgoCD

### Database
- **Development:** Single PostgreSQL cluster shared among all developers. (Backed by Cloudnative Postgres for automated backup and restore).
- **Production:** Cloudnative Postgres Cluster (2 replicas, daily backups, automated restore)

## Deployment Process

1. Image built and published to ghcr.io by `Jenkinsfile.docker`
2. Image tagged with git SHA
3. Kubernetes manifests updated with new image tag
4. Development environment updates automatically with new image tag. Production requires manual intervention.

## Rollback Procedure
Use ArgoCD to deploy application to previous state.

## Healthchecks
- Liveness probe: TBD
- Readiness probe: TBD

## Monitoring
- Metrics: TBD
- Logging: TBD
