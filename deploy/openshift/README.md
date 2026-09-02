# OpenShift deployment (not yet active)

This tree is scaffolding for a future migration from the current plain-Docker
deploy path (`deploy/ci-cd/Jenkinsfile` → SSH → `deploy/scripts/deploy.sh`) to
OpenShift. **The Jenkins pipeline does not use anything under this directory
today.**

When the migration happens, the pipeline's Deploy stage would swap the
SSH+`docker run` step for something like:

```
oc apply -k deploy/openshift/overlays/${DEPLOY_ENV}
oc rollout status deployment/order-service -n order-service-${DEPLOY_ENV}
```

`base/` holds the shared BuildConfig/ImageStream/Deployment/Service/Route;
`overlays/{dev,qa,uat,prod}/` hold the per-env Kustomize patches (image tag,
replica count, resource limits, and a ConfigMap sourced from
`deploy/config/{env}/application.properties`). `prod` intentionally has no
`secretGenerator` — production secrets are created out-of-band by platform
engineering, not from files in this repo.
