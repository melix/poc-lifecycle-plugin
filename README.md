## Module Lifecycle plugin POC
### Handling module lifecycle

This test project demonstrates a plugin enabling a custom lifecycle for modules. A module may be in 3 different
states:

* alive, meaning that the module is still relevant and can be used without problem
* deprecated, meaning that the module is no longer supported, and an upgrade would probably make sense
* blacklisted, meaning that the module should not be used, because it has been blacklisted, for whatever reason (can be
because of a vulnerability, a critical bug, ...)

It's perfectly valid to have multiple versions of a module to be alive at the same time, it only depends on the
maintained branches. So this plugin will:

* register an attribute called `org.gradle.lifecycle`, of type `Lifecycle`, which can have 3 different values: `ALIVE`,
`DEPRECATED` or `BLACKLISTED`.
* doesn't set any preferred lifecycle for configurations by default, so the standard behavior is to resolve like usual
* except for blacklisted modules, see below
* whenever the attribute is set on a consumer configuration, it tries to honor it. In particular:
  * if requesting `ALIVE` modules, only `ALIVE` modules will be returned. `DEPRECATED` and `BLACKLISTED` modules are no
longer allowed in the graph
  * if requesting `DEPRECATED` modules, we may return either `ALIVE` or `DEPRECATED` modules
  * if requesting `BLACKLISTED` modules, we will always fail (see below)

In short, the lifecycle attribute allows us to set the minimal accepted state. Saying `DEPRECATED` doesn't mean we want
deprecated modules, but that `DEPRECATED` is acceptable.

### Handling blacklisted modules

By default, this plugin _breaks the build_ whenever a blacklisted module is found in the graph. This is done to make
sure we are _reproducible by default_ and realize that the resulting graph contains blacklisted modules. It forces the
user to change its build script to avoid the problem (we could implement a variation of this to only issue warnings, but
it would weaken the semantics of blacklisting).

Alternatively, it is possible to choose a mode where blacklisted modules will be _rejected during selection_:

```
lifecycle {
   skipBlacklisted()
}
```

This is useful if the module is selected through a version range, in which case we would skip the blacklisted version
and find the next one available.

### Deprecated modules

The plugin will also warn the user about deprecated modules found in the graph:

> Configuration compileClasspath resolved a deprecated module: com.acme:testB:3

This is a call for action, consistent with the semantics of deprecated modules.

### Implementation notes

Failing blacklisted modules happens in an `afterResolve` hook. If, on the other hand, we use the "skip" mode, then it
becomes more complicated, and it may highlight a hole in the APIs. Today, rejection of dynamic modules can be expressed
in 2 ways:

* _via_ the legacy component selection rules, which are used during dynamic version resolution
* _via_ a constraint, in component metadata, where a module would reject versions of another module

Here, we have a problem if we only want to rely on 2: we can discover that a module is blacklisted (either because its
published metadata contains the information, which is unlikely, or because we have a rule that knows about it). But then,
it means that the component metadata rule needs a way for the module to exclude itself. Adding a constraint on itself
rejecting its own version doesn't work.

So the plugin works by 2 different things:

1. collect the rejections in a set via a component metadata rule: each time a module is seen, the rule is called, so we
can add the coordinates of a blacklisted module to the set
2. then _via_ a component selection rule, we can reject the version.

The reason we need to collect separately is that component selection rules do not give access to the attributes of the
component. This seems to be a case for _not deprecating_ component selection rules, but instead improving them to get
access to the attributes, so that we can do 2 only.

### Usage of the demo

- `gradle help` : lists all versions of the modules with their attributes
- `gradle` : resolves the dependencies, without any specific lifecycle status

### Configuration

- `-Pall=ALIVE` : Adds an attribute on the resolved configuration to ask for components which are minimally alive
- `-PforceA=ALIVE`: Overrides whatever lifecycle was asked for the specific dependency `A` to `ALIVE`
- `-PforceB=ALIVE`: Overrides whatever lifecycle was asked for the specific dependency `B` to `ALIVE`
- `-Pskip`: Enables blacklisted modules skipping during dynamic module version selection





