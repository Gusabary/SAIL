# Shell Scripting Notes

+ `#!/bin/bash` means this script should be interpreted by *bash* even if you are using *zsh* or something else.

+ Shell parses the arguments before passing them to the program (the command you use), so this two commands differ:

  ```bash
  echo Hello    World
  echo "Hello    World"
  ```

+ For assignment of value to a variable, it should write like `VAR=value`, and to use the variable, write `$VAR`.

+ `read` command can be used to read from a standard input (of course redirected pipe is ok)

+ Running a script directly with like `./` actually runs it in a spawned new shell (remember the `/bin/bash` at the beginning of the script? yep, you create a new bash and run the script in it). So some variables in the original shell won't have effect in the spawned one, except you use `export` to set those variables.

  To run the script in the current shell, we can *source* it with `.`: `. ./demo.sh`.

+ `:` always evaluates to true, so `while :` means `while true`

+ `[` is essentially a symbolic link to binary `test` (we can use `type [` and `which [` to check that), so in the script, we should write like `if [ $a = "hi" ]`, (note that spaces between `[`, a command should be surrounded by a pair of space).

  *But what does the `[[` mean?*

+ Shell script also has *shorthand syntax*: `[ $X -ne 0 ] && echo "X isn't zero" || echo "X is zero"`

+ `if` and `then` should not appear in the same line, unless they are separated by a `;`. Reversely, statements before and after `&&` should appear in the same line, unless they are separated by a `\`.

+ To compare strings, we can use `=` (`==` is not portable) and `!=`; To compare integers, we can use `-eq`, `-lt` and so on.

+ There are some built-in variables:

  + `$#` stores the number of arguments
  + `$@` stores all arguments as a list
  + `$*` stores all arguments as a whole string (avoid using this)
  + `$?` stores the exit value of last command (well-behaved program should exit with zero on success)
  + `$$` stores the PID of current process
  + `$!` stores the PID of last run background process

+ We can use shift to *drop* the first argument, so `$#` will decrease by one, `$@` will shrink and `${x+1}` will become `$x`

+ We can set `IFS` (internal field separator) to use another delimiter. Remember to set it back after use, whose default value is `SPACE`, `TAB` and `NEWLINE`.

+ To get a default value if a variable is undefined or null, use `${var:-default value}`. And to not only get but set it to the return value if it's undefined or null, use `${var:=default value}`.

+ Wrap the argument of `echo` to preserve new line in it:

  ```shell
  file=`cat a.c`
  echo "$file"
  ```

+ To run an external command, use the backtick or `$(command)` syntax:

  ```shell
  HTML_FILES=`find / -name "*.html" -print`
  HTML_FILES=$(find / -name "*.html" -print)
  ```

+ The return value of a function, just like exit code, should be an integer between 0 ~ 255 (256 will wrap around to 0).

+ A function will be called in a sub-shell if its output is piped somewhere, e.g. `func 1 | tee a.log`. That is because `tee` should be started up before invocation to `func` while the script invoking `func` started before `tee`, so `func` has to start in a new sub-shell.

+ Use `expr` to perform an arithmetic operation.

+ To factor out some useful functions as a lib, we can create a shell script (say, called `util.lib`) without `#!/bin/bash` in the header including those functions and use `. ./util.lib` to source it.

##### Last-modified date: 2020.12.11, 10 p.m.