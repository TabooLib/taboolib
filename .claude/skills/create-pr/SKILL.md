---
name: create-pr
description: 创建补丁：收集需求信息，在主仓库创建 Issue，创建本地分支，进入规划模式。
user_invocable: true
---

# 创建补丁流程

## 执行步骤

当用户调用 `/create-pr` 时，严格按以下步骤执行：

### 第一步：收集信息

使用 `AskUserQuestion` 工具向用户收集以下信息（一次性提问，使用多个 question）：

1. **功能简述**（header: "功能"）— 一句话描述要做什么功能
2. **为什么要做**（header: "原因"）— 这个功能的背景和动机
3. **怎么做**（header: "方案"）— 大致的实现思路

每个问题都使用自由文本输入（提供 2 个引导性选项让用户参考，但用户通常会选择 Other 自行填写）。

### 第二步：创建 Issue

在 **主仓库** `TabooLib/taboolib` 上创建 Issue：

```bash
gh issue create --repo TabooLib/taboolib \
  --title "[feat] {功能简述}" \
  --body "$(cat <<'EOF'
## 为什么要做

{用户填写的原因}

## 怎么做

{用户填写的方案}
EOF
)"
```

从命令输出中提取 Issue 编号（如 `#542`）。

### 第三步：创建本地分支

基于当前分支创建新分支，分支名格式为 `feat/{简述关键词}-{issue编号}`：

```bash
git checkout -b feat/{keyword}-{issue_number}
```

- `keyword`：从功能简述中提取 1-3 个英文关键词，用连字符连接
- `issue_number`：不带 `#` 的纯数字

示例：`feat/entity-name-i18n-542`

### 第四步：进入规划模式

使用 `EnterPlanMode` 工具进入规划模式，对需求进行详细分析和实现方案规划。

## 注意事项

1. Issue 必须创建在主仓库 `TabooLib/taboolib`，不是个人 fork
2. 分支基于当前所在分支创建（通常是 `dev/6.2.0`）
3. 分支名只使用小写英文字母、数字和连字符
4. 进入规划模式后，充分分析代码库再制定方案
