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