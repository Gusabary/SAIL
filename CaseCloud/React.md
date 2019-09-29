# React learning notes

## Hook

+ `useState` 会返回一对值：**当前**状态和一个让你更新它的函数，`useState` 唯一的参数就是初始 state：

  ```react
  const [count, setCount] = useState(0);
  ...
  <button onClick={() => setCount(count + 1)}>
      Click me
  </button>
  ```

+ 当你调用 `useEffect` 时，就是在告诉 React 在完成对 DOM 的更改后运行你的“副作用”函数，副作用函数还可以通过返回一个函数来指定如何“清除”副作用：

  ```react
  useEffect(() => {
      ChatAPI.subscribeToFriendStatus(props.friend.id, handleStatusChange);
  
      return () => {
          ChatAPI.unsubscribeFromFriendStatus(props.friend.id, handleStatusChange);
      };
  });
  ```

## Route

+ 当 component 为子 Router 时，不要用 exact：

  ```react
  <Route path={`${prefix}/spec/:issueId/voting-group`} component={VotingGroupRouter} />
  ```

  例如，`/a` 有三个 Route，分别是 `/a/b`, `/a/c`, `/a/d`，其中 `/a/d` 又有两个 Route，`/a/d/e`, `/a/d/f` ，在 `/a` 的 Router 中应写成这样：

  ```react
  <Route exact path={`${prefix}/b`} component={bComponent} />
  <Route exact path={`${prefix}/c`} component={cComponent} />
  <Route path={`${prefix}/d`} component={dRouter} />
  ```

  因为如果 `/d` 也指定 exact 的话，当访问 `/a/d/e` 时，从 `/a` 是 route 不到 `/a/d` 的，也就 route 不到 `/a/d/e`。

## CSS

+ 与 RN 不同的是，想要让一个容器内的盒子（使用 flex 属性）弹性排列，需要在容器中指定 `display: flex`，且 `flexDirection` 默认值为 `row`。
+ 当容器内的盒子溢出时，使用 `flexWrap: wrap` 解决这一问题。
+ 与 RN 不同的是，想让一个盒子显示边界要指定 `borderStyle: "solid"` 而不是 `borderWidth: 1`。

## Formik

+ 点 submit 没有反应，onSubmit 回调函数不执行，很有可能是 schema 写的有问题，因为 formik 在执行 onSubmit 回调函数之前会先检查一下是否满足 schema 的要求，如果不满足就不执行回调。

  schema 写的有问题是指字段名写错或者多写了某个字段。

## Other

+ `git checkout ${branch}` 切换完分支后需要重新 `yarn start` ，否则可能出现编译不通过的问题。

##### Last-modified date: 2019.9.27, 11 p.m.