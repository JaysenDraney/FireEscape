# 0003. Screenshot review in CI uses companion branches and Roborazzi diffs

Status: Accepted

## Context

ADR 0002 chose Roborazzi for screenshot tests. We want to see the screenshots for every change, in the pull request, without a device. The repository is public, so standard GitHub-hosted runners are free. Artifact storage is limited on the Free plan (500 MB, shared with Packages). I could not confirm whether public repositories count against that allowance, so we treat it as the worst case.

Job summaries cannot show images reliably, and artifacts are zip files that cannot be previewed.

## Decision

We use the pattern from Roborazzi's [compare-on-comment sample](https://github.com/takahirom/roborazzi-compare-on-github-comment-sample), with these workflows:

1. **Store.** On pushes to `main`, record screenshots and upload them as an artifact. This is the baseline.
2. **Compare.** On pull requests, download the baseline from `main`, run `compareRoborazziDebug`, and upload the images, the Roborazzi HTML report and the test results as artifacts with a 14-day retention. Also save the PR number.
3. **Comment.** After Compare finishes (`workflow_run`), push the `*_compare.png` images to a `companion_<branch>` branch and post or update one PR comment with a table of the images.

The diff tool is Roborazzi's own compare task. It writes a side-by-side `[name]_compare.png` for each change. The HTML report is published as an artifact. We do not add a second diff tool until the Roborazzi output proves insufficient.

Fork pull requests are supported by keeping the two trust levels apart. The Compare workflow runs the PR's code with a read-only token. The Comment workflow runs from the base repository, has write permissions and only handles artifacts.

## Changes from the sample

- Validate the PR number from the artifact: it must be numeric and must appear in `workflow_run.pull_requests`.
- Pass branch names only through environment variables and restrict file names to a safe character set.
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
- Not yet verified: fork PR behavior and how the sample behaves with TV-sized screenshots. We verify both when the workflows first run.
