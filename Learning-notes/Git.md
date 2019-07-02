## Git learning notes

+ git 有关分支的操作

  + `git checkout -b dev` 新建并切换至 dev 分支

  + `git push --set-upstream origin dev` 将 dev 分支关联至远端

  + `git merge dev` 在当前分支下 merge dev 分支

    `git merge` 是在本地 merge，用 `git push` 会同步到远端，但是没有 PR 记录。

  + 版本分叉时会有 merge 记录（在 commit log 里）

    + 如果有冲突（一般是对同一个文件进行不同更新），需要手动解决
    + 如果没有，简单合并

    版本单纯滞后时不会有 merge 记录。

  + .git 会根据当前分支显示不同的目录。（神奇）

+ 注意在远端 default branch 要设为 master （repo Settings -> Branches 里）

+ git workflow (an example):

  第一次工作：

  1. `git clone` 远端仓库到本地（然后进入该目录）

  2. `git checkout dev` 从 master 分支切换至 dev 分支

  3. `git checkout -b mybranch` 以 dev 分支为基础新建并切换至个人工作分支，

     个人工作分支只存在于本地，不用上传到远端。

  4. 在个人分支下工作，`git add` + `git commit` （不用 `git push`）

  5. 工作完成，用 `git checkout dev` 切换回 dev 分支（此时个人分支版本领先于 dev 分支）

  6. `git pull` 在本地 dev 分支先将远端 dev 分支的更新内容 pull 下来，

     否则由于远端 dev 分支可能被他人更新，本地 dev 分支版本分叉，会出现 push 不了的情况。

  7. `git merge mybranch` 在本地 dev 分支 merge 个人工作分支，

     此时如果第 6 步 pull 下来的更新和刚刚 merge 进来的更新修改了同一个文件，可能会发生冲突，需要手动解决。

  8. `git push` 将本地 dev 分支的最新修改同步至远端 dev 分支。

  9. 在远端，dev 分支周期性地 PR 进 master 分支。

  之后工作：

  1. `git checkout dev` 切换至 dev 分支

  2. `git pull` 先将本地 dev 与远端 dev 同步

  3. 可以 `git checkout -b mynewbranch` 新建新的个人工作分支，

     也可以 `git checkout mybranch` 切换到原来的个人工作分支，然后 `git merge dev` 使其与本地 dev 分支同步

  4. 第 4 步之后和第一次工作时一样

+ 标签 / tag

  + `git tag` 查看所有标签

  + 标签分轻量级的和含注解的，

    创建轻量级标签，使用 `git tag v1.0`，

    创建含注解标签，使用 `git tag -a v1.0 -m"message"`

  + 后期加注标签：`git tag -a v1.0 -m"message" 9fceb02`, `9fceb02` 是代指 commit ID 的前几位，可以用 `git log` 查看历史 commit，获取想要打上标签的那个 commit 的 ID。

  + 推送标签：

    推送单个：`git push v1.0` ，

    推送所有：`git push --tags `

##### Last-modified date: 2019.7.2, 10 p.m.

