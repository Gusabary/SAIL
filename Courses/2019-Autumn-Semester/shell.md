# Shell

+ 在 Shell 脚本中没有多行注释，只有单行注释

+ 定义变量和使用变量：

  ```shell
  name=tbc
  echo $name
  ```

+ `${0}` 文件名称，`${1}` 第一个参数，`${2}` 第二个参数，以此类推。

+ 或运算 `-o` 和 `||` 的区别：

  ```shell
  if [ $a -lt 200 -o $b -gt 200 ]
  if [ $a -lt 200 ] || [$b -gt 200 ]
  ```

  与运算 `-a` 和 `&&` 同理

+ 文件测试运算符：

  ```shell
  file="/Users/zhaoruisheng/Desktop"
  if [ -d $file ]
  ```

  + `-d` 是否为目录
  + `-f` 是否为普通文件
  + `-r` 是否可读
  + `-w` 是否可写
  + `-x` 是否可执行
  + `-s` 是否有内容
  + `-e` 是否存在

+ 条件语句：

  ```shell
  if [条件]
  then
    代码
  elif [条件]
  then
    代码
  else
    代码
  fi
  ```

+ 循环语句：

  ```shell
  for ((a = 1; a < 5; a++ ))
  do 
    echo $a
    for ((b = 1; b < 5; b++ ))
    do
      echo $b
    done
  done
  ```

  break 数字：表示退出几层循环

##### Last-modified date: 2019.9.29, 2 p.m.