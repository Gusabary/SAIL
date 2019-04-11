## Git learning notes

+ git 有关分支的操作

  + `git checkout -b dev` 新建并切换至 dev 分支

  + `git push --set-upstream origin dev` 将 dev 分支关联至远端

  + `git merge dev` 在当前分支下 merge dev 分支

    `git merge` 是在本地 merge，用 `git push` 会同步到远端，但是没有 PR 记录。

    直接在远端 PR 会有记录。

  + .git 会根据当前分支显示不同的目录。（神奇）

+ 注意在远端 default branch 要设为 master （repo Settings -> Branches 里）

##### Last-modified date: 2019.4.11, 10 p.m.

