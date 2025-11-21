# Git tips for this project

## Pulling updates on a feature or PR branch
If you see an error like:

```
There is no tracking information for the current branch.
```

the branch does not know which remote branch to pull from. Configure tracking once, then pull normally:

```bash
git branch --set-upstream-to=origin/<branch-name>
# example for the current PR branch
# git branch --set-upstream-to=origin/pr-7

git pull   # now works without extra arguments
```

If the branch does not exist on the remote yet, push it first with:

```bash
git push -u origin <branch-name>
```

After tracking is set, future `git pull` and `git push` commands work without additional flags.
