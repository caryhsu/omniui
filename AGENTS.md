# AGENTS.md

專案 AI agent 的行為指引與開發慣例。

---

## Git Workflow

- **Feature branch → main 必須用 PR**：完成 feature branch 後，用 `gh pr create` 建立 PR，讓使用者自己 review + merge，不直接 `git merge` 到 main。
