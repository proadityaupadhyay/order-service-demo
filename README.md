# order-service

Mavenized TIBCO BWCE integration + its CI/CD pipeline, in one repo.

- **`app/`** (owned by `@integration-team`) — the BWCE module/application. Real
  TIBCO Studio artifacts (`.bwp`, `.bwm`, manifests, substvars) are marked as
  placeholders; a minimal Maven-buildable Java stand-in
  (`OrderService.module` / `OrderService.application`) exercises the same
  build shape end-to-end so the pipeline in `deploy/` can be proven out
  before the real BW project is dropped in.
- **`deploy/`** (owned by `@platform-team`) — the Jenkins pipeline, deploy
  scripts, per-env runtime config, and (unused today, kept for later)
  OpenShift manifests.

## Two config layers

- **Design-time** (`app/OrderService.application/configuration/{env}.substvar`)
  — baked into the build by a Maven profile (`-Pdev`/`-Pqa`/`-Puat`/`-Pprod`),
  selected by the Jenkinsfile based on the branch being built.
- **Runtime** (`deploy/config/{env}/application.properties`) — injected at
  deploy time via `docker run --env-file`, independent of which image was
  built.

## CI/CD flow

GitHub push → Jenkins multibranch pipeline (script path
`deploy/ci-cd/Jenkinsfile`) → resolve env from branch → `mvn package` (build +
test, profile-selected substvar baked in) → `docker build` (re-packages
inside the image for a clean artifact) → push to the private registry → SSH
+ rsync to the remote Docker host → `deploy/ci-cd/deploy/deploy-<env>.sh` →
`deploy/scripts/smoke-test.sh`.

See `../jenkins-cicd/` (sibling repo) for the Jenkins server itself
(`docker-compose.yml` + custom image), and its README for the Jenkins-side
setup (credentials, multibranch job, webhook).

## Run locally

```
deploy/scripts/run-local.sh dev
curl http://localhost:8080/health
curl "http://localhost:8080/order?subtotal=100"
```

Optionally start the downstream stub first:

```
java app/mock-service/src/MockService.java
```

## Branch → environment mapping

Currently: `dev`→dev, `qa`→qa, `uat`→uat, `main`/`master`/`prod`→prod (see
`Resolve Environment` stage in the Jenkinsfile) — adjust to your actual
branching convention.

## Still TODO once real values are known

- `REGISTRY`, `IMAGE_NAME`, `REMOTE_HOST` in `deploy/ci-cd/Jenkinsfile`
- Jenkins credentials `registry-credentials` and `remote-docker-ssh-key`
- Replace the placeholder TIBCO files in `app/OrderService.module` and
  `app/OrderService.application` with the real Studio-exported project, and
  the Dockerfile's Maven build with the real TIBCO BW Maven plugin build
  (needs a TIBCO Maven repo + license)
- Remote Docker host: Docker installed, deploy user's SSH key authorized,
  in the `docker` group, and able to pull from the private registry
