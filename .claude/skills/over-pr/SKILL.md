---
name: over-or
description: 完成补丁：推送分支到个人仓库，向主仓库提交 PR。
user_invocable: true
---

# 完成补丁流程

## 执行步骤

当用户调用 `/over-or` 时，表示当前任务已完成，严格按以下步骤执行：

### 第一步：检查状态

```bash
git status
git log --oneline dev/6.2.0..HEAD
```

确认：

- 工作区干净（无未提交的修改）
- 当前分支有新的 commit（相对于基础分支）

如果有未提交的修改，先提醒用户处理。

### 第二步：推送到个人仓库

将当前分支推送到个人 fork 仓库（origin）：

```bash
git push -u origin {当前分支名}
```

### 第三步：创建 PR

使用 `gh` 向主仓库 `TabooLib/taboolib` 提交 PR：

```bash
gh pr create --repo TabooLib/taboolib \
  --base dev/6.2.0 \
  --head FxRayHughes:{当前分支名} \
  --title "{PR标题}" \
  --body "$(cat <<'EOF'
{PR描述}

Closes TabooLib/taboolib#{issue编号}
EOF
)"
```

**PR 标题**：与对应 Issue 标题一致（如 `[feat] 功能简述`）。

**PR 描述**：包含以下内容：

1. 简要说明做了什么改动
2. 列出主要修改的文件/模块
3. 关联 Issue（使用 `Closes TabooLib/taboolib#编号`）

Issue 编号从当前分支名中提取（分支名末尾的数字）。

### 第四步：输出结果

向用户展示：

- PR 链接
- 关联的 Issue 链接
- 等待主仓库维护者审核合并

## 注意事项

1. PR 的 `--base` 是主仓库的目标分支（通常 `dev/6.2.0`）
2. PR 的 `--head` 必须带上个人用户名前缀 `FxRayHughes:` (需要使用gh进行获取)
3. 如果工作区不干净，不要强行推送，提醒用户先处理
4. 从分支名提取 Issue 编号（如 `feat/xxx-542` → `542`）
