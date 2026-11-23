# 0003. Screenshot review in CI uses companion branches and Roborazzi diffs

Status: Accepted

## Context

ADR 0002 chose Roborazzi for screenshot tests. We want to see the screenshots for every change, in the pull request, without a device. The repository is public, so standard GitHub-hosted runners are free. Artifact storage is limited on the Free plan (500 MB, shared with Packages). I could not confirm whether public repositories count against that allowance, so we treat it as the worst case.

Job summaries cannot show images reliably, and artifacts are zip files that cannot be previewed.

## Decision

We use the pattern from Roborazzi's [compare-on-comment sample](https://github.com/takahirom/roborazzi-compare-on-github-comment-sample), adapted to committed reference images (ADR 0002). There are three workflows:

1. **Screenshot compare** (`pull_request`). Runs the tests and `verifyRoborazziDebug` against the PR's own committed references, so a PR that changes the UI must re-record them. It then swaps in `main`'s references and runs `compareRoborazziDebug`, and collects screenshots that are new in the PR. It uploads the images, the Roborazzi HTML report and the test results as artifacts with a 14-day retention, and saves the PR number.
2. **Screenshot comment** (`workflow_run`, after compare). Pushes the images to a `companion/pr-<number>` branch and posts or updates one PR comment that shows them.
3. **Screenshot cleanup** (`pull_request_target`, on close). Deletes the companion branch.

The sample's separate "store" workflow is not needed, because `main`'s reference images are in the repository.
The diff tool is Roborazzi's own compare task. It writes a side-by-side `[name]_compare.png` for each change. The HTML report is published as an artifact. We do not add a second diff tool until the Roborazzi output proves insufficient.

Fork pull requests are supported by keeping the two trust levels apart. The Compare workflow runs the PR's code with a read-only token. The Comment workflow runs from the base repository, has write permissions and only handles artifacts.

## Changes from the sample

- Validate the PR number from the artifact: it must be numeric, and the PR must be open and point at the commit that produced the artifact.
- Name companion branches by PR number, never by the fork's branch name. Restrict image file names to a safe character set. Pass event values to scripts only through environment variables.
- Delete the companion branch when the PR closes. The sample's cleanup is disabled.
- Pin third-party actions to commit SHAs.
- Use our own Gradle modules and Java version.

## Alternatives considered

- **Argus** ([taiga-family/argus](https://github.com/taiga-family/argus)). A single action that pins diff images to a PR comment. It needs `diff-paths` set for Roborazzi's file names and it shows diffs only. We prefer the Roborazzi sample, which is built for Roborazzi. We may revisit Argus if the sample is hard to maintain.
- **Hosted services** (Argos, Chromatic, Percy). They add an outside account and are built around web testing. We found no confirmation of Android support.
- **Per-PR GitHub Pages previews.** The repository's `pages.yml` already deploys the live site, so previews could collide with it.
- **Job summary images.** They do not render reliably.

## Consequences

- Reviewers see changed screenshots inline on each PR. Unchanged screenshots produce no output.
- The workflow grants `contents: write` and `pull-requests: write` to a job that handles artifacts from untrusted PRs. The changes above are required for that reason.
- Companion branches add images to the repository until they are deleted.
- Not yet verified: fork PR behavior and how the comment looks with 1920x1080 screenshots. We verify both when the workflows first run.
