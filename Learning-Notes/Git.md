# Git Notes

## Git LFS

```bash
git lfs install
git lfs track ${filename}
# git lfs track ${dir}/*
```

## Git ignore

+ 以 `/` 开始的条目从根目录开始匹配，否则从当前 .gitignore 所在目录开始匹配
+ 以 `/` 结束的条目匹配目录，不匹配文件，否则匹配目录和文件

##### Last-modified date: 2019.12.13, 9 p.m.