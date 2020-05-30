# Maven learning notes

+ maven 有三种 repository：local，central 以及 remote。maven 在寻找依赖的时候也是沿着 local - central - remote 的顺序找的，如果在后两者中找到的话会将依赖下载到 local repository 以便后续查找。

  - local repository 就是本地仓库，目录结构按照 groupId/artifactId/version 来组织。
  - central repository 是由 maven 社区维护的仓库，本地仓库没有依赖时会在这里找。
  - remote repository 一般用于存放组织内部的依赖，central repository 中找不到的依赖会在这里找。

+ 对于 SNAPSHOT 的依赖，即使本地仓库中已经存在，也还是会去远端下载。这样在开发阶段时就不用频繁地更改版本号，也能拉取到最新的版本，

+ maven 的构建过程分为 life cycle，phase 以及 goal。每个 life cycle 包含若干 phase，每个 phase 包含若干 goal。当执行 life cycle 时会执行其中所有 phase，执行 phase 时会执行其中所有 goal 以及在 life cycle 中该 phase 之前的所有 phase。

  在执行 maven 命令时，可以将 life cycle 或 phase 作为参数传入：

  ```shell
  mvn clean	 # life cycle
  mvn install  # phase
  ```

  也可以将 phase 和 goal 以冒号相连作为参数传入：

  ```shell
  mvn dependency:copy-dependencies  # phase:goal
  ```

+ maven 有三个内置的 build life cycle：

  + default，用来处理编译和打包的任务；
  + clean，用来处理删除清理的任务；
  + site，用来处理生成文档以及网站的任务。

  default 作为一整个 life cycle 不能直接被执行，clean 和 site 可以。

+ default life cycle 中包含一些常见的 phase，例如 compile，test，package 等等。

+ 用 maven-assembly-plugin 可以将所有依赖打在一个包里，生成一个 Fat JAR

  *[reference](<http://tutorials.jenkov.com/maven/maven-build-fat-jar.html>)*

##### Last-modified date: 2020.5.30, 7 p.m.