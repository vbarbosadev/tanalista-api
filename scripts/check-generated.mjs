import { execFileSync } from "node:child_process";

// Regeneration is authoritative. Include untracked files, not just tracked diffs.
execFileSync("buf", ["generate"], { cwd: "protos", stdio: "inherit" });
execFileSync("git", ["diff", "--exit-code", "--", "services/gen"], { stdio: "inherit" });
const untracked = execFileSync("git", [
  "ls-files", "--others", "--exclude-standard", "services/gen",
], { encoding: "utf8" });
if (untracked.trim()) {
  throw new Error(`Generated files must be committed:\n${untracked}`);
}
