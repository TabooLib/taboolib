---
name: taboolib-incision
description: Maintain and document the TabooLib incision module. Use when working on runtime weaving, Scalpel DSL, @Surgeon annotations, dispatcher/registry flow, remap/version matching, bytecode site matching, Incision-Test fixtures, or incision user/design documentation in this repository.
---

# TabooLib Incision

Use this skill when changing `module/incision`, `Incision-Test`, or the incision docs.

Assume the caller may not have the incision source code locally. Explain public behavior directly instead of saying "read the implementation" unless source inspection is required for the task.

## What Incision Is

Treat incision as a runtime weaving module with two public entry styles:

- DSL style: `Scalpel { ... }`
- Annotation style: `@Surgeon` + advice annotations

Default guidance:

- Prefer annotation mode first.
- Use DSL only for special cases such as temporary runtime patches, scoped patches, thread-local patches, event-driven arming, or other dynamic lifecycle requirements.
- If a requirement can be expressed cleanly with `@Surgeon` annotations, do not move it to DSL.

The public mental model is:

- A **Suture** is one installed patch handle.
- A **Theatre** is the runtime context visible inside advice.
- A **Resume** controls whether `Splice` continues into downstream advice or the original method.
- A **Site** describes a bytecode insertion or replacement location.

## Public Capability Summary

Use these feature buckets when explaining or editing the module:

| Bucket | Public surface | Typical use |
| --- | --- | --- |
| Entry/exit weaving | `lead`, `trail`, `@Lead`, `@Trail` | logging, counters, lightweight checks |
| Around weaving | `splice`, `@Splice` | short-circuit, argument replacement, result takeover |
| Mid-method hook | `@Graft`, `@Bypass`, `@Trim`, `Site` | invoke/field/new interception and value mutation |
| Whole-method override | `excise`, `@Excise` | replace a method body completely |
| Runtime lifecycle | `Suture`, `heal`, `suspend`, `resume` | enable/disable and rollback |
| Filtering | `Scope`, `InsnPattern`, `where`, `Version` | narrow targets and gate by environment |
| Kotlin/NMS adaptation | `KotlinTarget`, remap/version flow | companion, `@JvmStatic`, NMS/Bukkit targets |

Selection rule:

- Prefer `@Surgeon` plus annotations for normal long-lived module behavior.
- Prefer DSL only when the patch must be created, armed, suspended, resumed, or destroyed dynamically from code.

## Start Here

Read these files first:

1. `module/incision/README.md`
2. `module/incision/src/main/kotlin/taboolib/module/incision/dsl/Scalpel.kt`
3. `module/incision/src/main/kotlin/taboolib/module/incision/loader/SurgeonScanner.kt`
4. `module/incision/src/main/kotlin/taboolib/module/incision/runtime/TheatreDispatcher.kt`
5. `E:\Code\TabooLib\Incision-Test\README.md`

Use `Incision-Test` as the executable spec. If behavior and docs disagree, treat the tests plus current source as ground truth, then update docs.

If the task is documentation-only, the minimum required references are:

1. `module/incision/README.md`
2. `module/incision/src/main/kotlin/taboolib/module/incision/annotation/*`
3. `E:\Code\TabooLib\Incision-Test\README.md`
4. `E:\Code\TabooLib\Incision-Test\src\main\kotlin\top\maplex\incisiontest\CaseDocs.kt`

## File Map

Use this map to narrow the change quickly:

| Area | Main files |
| --- | --- |
| DSL entry and lifecycle | `dsl/Scalpel.kt`, `dsl/SutureImpl.kt`, `api/Suture.kt` |
| Annotation scanning | `loader/SurgeonScanner.kt`, `annotation/*` |
| Runtime dispatch | `runtime/TheatreDispatcher.kt`, `runtime/AdviceChain.kt`, `runtime/SurgeryRegistry.kt` |
| Site matching / bytecode placement | `weaver/SiteWeaver.kt`, `weaver/site/*`, `weaver/site/matcher/*` |
| Descriptor / remap / version | `api/DescriptorCodec.kt`, `remap/*`, `api/VersionMatcher.kt` |
| Diagnostics | `diagnostic/Forensics.kt`, `diagnostic/Trauma.kt` |
| Platform bootstrap | `gate/GateBootstrapper.kt`, `loader/*Backend*` |
| Test suite | `E:\Code\TabooLib\Incision-Test\src\main\kotlin\top\maplex\incisiontest\AllCases.kt` |
| Test docs | `E:\Code\TabooLib\Incision-Test\README.md`, `E:\Code\TabooLib\Incision-Test\src\main\kotlin\top\maplex\incisiontest\CaseDocs.kt` |

## User-Facing API Reference

Use this section when the user needs usage guidance without reading source.

### 1. `@SurgeryDesk`

Use `@SurgeryDesk` on a Kotlin `object` that owns DSL patches.

```kotlin
@SurgeryDesk
object DemoDesk
```

Rules:

- Allow DSL patch declaration only inside a Kotlin `object`.
- Reject ordinary classes.
- Expect runtime call-site validation for `transient`, `scoped`, `threadLocal`, `armOn`, `disarmOn`, and `exclusive`.

### 2. `Scalpel` DSL Entry Points

Explain these entry points explicitly:

Use this section as the fallback path, not the default recommendation.

| API | Purpose | Notes |
| --- | --- | --- |
| `Scalpel { ... }` | create a persistent patch | usually used as a delegated property returning `Suture` |
| `Scalpel.deferred { ... }` | create a lazy patch | install later instead of eagerly |
| `Scalpel.transient { ... }` | create a temporary patch | returns `Suture`; caller usually wraps with `.use {}` or calls `heal()` |
| `Scalpel.scoped { ... }` | patch only inside a code block | auto-heal when scope ends |
| `Scalpel.threadLocal { ... }` | patch per-thread | default disabled; activate on demand |
| `Scalpel.armOn(...)` | event-driven arming | returns an `ArmTrigger`; user decides when to arm/disarm |
| `Scalpel.disarmOn(...)` | inverse event-driven mode | starts armed, later disarms |
| `Scalpel.exclusive(...) { ... }` | exclusive execution block | suspend other ARMED sutures on the same targets during the block |
| `Scalpel.find(id)` | find one patch by id | runtime query |
| `Scalpel.list(...)` | list patches | can filter by holder or target |
| `Scalpel.healAll(scope?)` | bulk remove patches | returns removed count |

Recommend DSL only when one of these is true:

- the patch lifetime is shorter than the process lifetime
- the patch must exist only inside one block or one thread
- the patch is toggled by runtime events or admin actions
- the patch is experimental, diagnostic, or temporary

### 3. `ScalpelBuilder` Methods

Inside `Scalpel { ... }`, the public methods are:

| Method | Meaning | Handler shape |
| --- | --- | --- |
| `priority(p)` | set default priority for subsequent declarations | no handler |
| `lead(descriptor) { theatre -> }` | run before entering target method | `Theatre -> Unit` |
| `trail(descriptor) { theatre -> }` | run on exit | `Theatre -> Unit` |
| `splice(descriptor) { theatre -> ... }` | around advice | `Theatre -> Any?` |
| `bypass(descriptor) { theatre -> ... }` | replace a single call site or method-level bypass path | `Theatre -> Any?` |
| `excise(descriptor) { theatre -> ... }` | replace whole method | `Theatre -> Any?` |
| `on(vararg descriptors) { ... }` | batch-attach the same advice to many descriptors | nested block |
| `... where { theatre -> ... }` | add a predicate to the last advice | boolean predicate |

Typical DSL example:

```kotlin
@SurgeryDesk
object DemoDesk {

    val patch by Scalpel {
        priority(100)

        lead("top.example.Target#greet(java.lang.String)java.lang.String") { theatre ->
            println(theatre.args[0])
        }

        splice("top.example.Target#greet(java.lang.String)java.lang.String") { theatre ->
            theatre.resume.proceed("patched-name")
        } where { theatre ->
            theatre.args.firstOrNull() == "Alice"
        }
    }
}
```

### 4. Descriptor Format

Describe descriptors directly because callers may not have helpers:

```text
owner#method(arg1,arg2,...)returnType
```

Examples:

- `org.bukkit.entity.Player#kickPlayer(java.lang.String)void`
- `top.example.Target#greet(java.lang.String)java.lang.String`
- `net.minecraft.server.MinecraftServer#getPlayerCount()int`

Rules:

- Use Java/Kotlin source type names in the public declaration form.
- Expect parsing to fail if `#` or parentheses are missing.
- Companion and `@JvmStatic` targets may need extra expansion with `@KotlinTarget`.

### 5. `Theatre`

Inside any advice, `Theatre` exposes:

| Member | Meaning |
| --- | --- |
| `target` | target method coordinate |
| `self` | instance receiver; `null` for static methods |
| `args` | mutable argument array |
| `resume` | proceed/skip controller for around-style advice |
| `override(value)` | shortcut for `resume.skip(value)` |
| `throwable` | throwable on exception exit, mostly useful in `Trail` |

### 6. `Resume`

Inside `Splice`, explain every operation:

| Method | Meaning |
| --- | --- |
| `proceed()` | continue with downstream advice or original method |
| `proceed(vararg args)` | continue but replace arguments |
| `proceedResult(value)` | send a new current result downstream |
| `skip(value)` | stop the chain and use `value` as final result |
| `proceeded` | whether explicit continuation already happened |

Critical rule:

- `Splice` must explicitly call one of `proceed`, `proceed(args)`, `proceedResult`, or `skip/override`. Missing this causes `ResumeMissing`.

### 7. `Suture`

Explain the runtime patch handle clearly:

| API | Meaning |
| --- | --- |
| `id` | stable patch id |
| `targets` | target method list covered by this patch |
| `state` | `ARMED`, `TRIGGERED`, `SUSPENDED`, `HEALED`, `INACTIVE_UNRESOLVED` |
| `holder` | owning `@SurgeryDesk` object |
| `heal()` | permanently remove |
| `suspend()` | temporarily disable without tearing bytecode back down |
| `resume()` | re-enable a suspended patch |
| `close()` | same as `heal()` |

## Annotation Reference

Use these sections when the user asks "which annotation should I use?"

### `@Surgeon`

Use on a Kotlin `object` that contains annotation-based advice methods.

```kotlin
@Surgeon(priority = 50)
object DemoSurgeon
```

Rules:

- Allow only Kotlin `object`.
- Treat `priority` as the default for methods in the object.
- Allow method-level `@Operation` to override class-level priority.
- Prefer this mode over DSL for normal product code and long-lived patches.

### `@Lead`

Use for method-entry hooks.

```kotlin
@Lead(
    scope = "method:top.example.Target#greet(java.lang.String)java.lang.String"
)
fun before(theatre: Theatre) { ... }
```

Use when:

- you need logging, counters, argument inspection, or cheap pre-checks
- you do not need to replace the original method

### `@Trail`

Use for method-exit hooks.

```kotlin
@Trail(
    scope = "method:top.example.Target#greet(java.lang.String)java.lang.String",
    onThrow = true
)
fun after(theatre: Theatre) { ... }
```

Use when:

- you need return-path cleanup
- you need exception-path observation via `theatre.throwable`

### `@Splice`

Use for around control.

```kotlin
@Splice("method:top.example.Target#greet(java.lang.String)java.lang.String")
fun around(theatre: Theatre): Any? {
    return theatre.resume.proceed("patched")
}
```

Use when:

- you need to short-circuit
- you need to replace arguments before continuing
- you need to replace or wrap the final result

Never forget:

- explicitly continue or short-circuit
- keep the returned value compatible with the target method contract

### `@Bypass`

Use to replace a single call site inside a host method.

```kotlin
@Bypass(
    method = "top.example.Host#work()int",
    site = Site(anchor = Anchor.INVOKE, target = "top.example.Helper#sum()int")
)
fun redirect(theatre: Theatre): Any? = 42
```

Use when:

- only one invoke/field access/new site should be replaced
- whole-method replacement would be too broad

### `@Graft`

Use to append logic before or after a site without replacing it.

```kotlin
@Graft(
    method = "top.example.Host#work()int",
    site = Site(anchor = Anchor.INVOKE, target = "top.example.Helper#sum()int", shift = Shift.BEFORE)
)
fun beforeCall(theatre: Theatre) { ... }
```

Use when:

- you want a probe, side effect, or trace
- the original call must still execute

### `@Trim`

Use to mutate values in flight.

```kotlin
@Trim(
    method = "top.example.Target#greet(java.lang.String,int)java.lang.String",
    kind = Trim.Kind.ARG,
    index = 0
)
fun trimArg(theatre: Theatre): Any? = "patched"
```

Kinds:

- `ARG`: rewrite one argument
- `RETURN`: rewrite a return value
- `VAR`: rewrite a local variable slot

Use when:

- you want value mutation without taking over the whole control flow

### `@Excise`

Use to replace an entire method body.

```kotlin
@Excise("method:top.example.Target#greet(java.lang.String)java.lang.String")
fun overwrite(theatre: Theatre): Any? = "fixed"
```

Rules:

- allow only one `Excise` per target
- expect the highest compatibility risk among advice types

### `@Operation`

Use on any advice method to refine metadata.

```kotlin
@Operation(priority = 100, enabled = false, id = "my-op")
```

Fields:

- `priority`: higher runs earlier
- `enabled`: if `false`, requires later manual resume
- `id`: stable suffix for management and diagnostics

### `@Version`

Use to gate advice by runtime version.

```kotlin
@Version(start = "1.20", end = "1.21.1")
```

Rules:

- default matcher uses Minecraft/NMS version semantics
- empty `start` means no lower bound
- empty `end` means no upper bound
- custom `matcher` is a matcher class FQCN

### `@KotlinTarget`

Use when the logical target also exists as:

- a companion instance method
- an `@JvmStatic` bridge

```kotlin
@KotlinTarget(companionInstance = true, jvmStaticBridge = true)
```

### `@Site`

Use with `@Graft`, `@Bypass`, and `@Trim` to describe the exact bytecode location.

Fields:

| Field | Meaning |
| --- | --- |
| `anchor` | `HEAD`, `TAIL`, `RETURN`, `INVOKE`, `FIELD_GET`, `FIELD_PUT`, `NEW`, `THROW` |
| `target` | owner/method/descriptor of the anchor site |
| `shift` | `BEFORE` or `AFTER` |
| `ordinal` | nth occurrence; `-1` means all |
| `offset` | move extra instructions after choosing the anchor |

### `@Scope`

Use as a selector DSL.

Syntax summary:

- `class:com.foo.Bar`
- `method:Foo#bar(*)`
- `field:Foo#name:String`
- `&`, `|`, `!`

### `@InsnPattern` and `@Step`

Use when descriptor/scope/site are not enough and a real bytecode sequence matters.

```kotlin
@InsnPattern(
    steps = [
        Step(opcode = Op.ICONST_5),
        Step(opcode = Op.ISTORE),
        Step(opcode = Op.ILOAD),
        Step(opcode = Op.IADD)
    ]
)
```

Step fields:

- `opcode`
- `owner`
- `name`
- `desc`
- `cst`
- `repeat`

Warn the user that this works on compiled bytecode, not source text.

## Decision Guide

Use this quick chooser when the user does not know which API to pick:

| Need | Preferred tool |
| --- | --- |
| run before method body | `Lead` |
| run after method body | `Trail` |
| decide whether to continue | `Splice` |
| replace one invoke/field/new site | `Bypass` |
| add logic around one site but still keep the site | `Graft` |
| rewrite arg/return/local value | `Trim` |
| replace the whole method | `Excise` |
| patch from code at runtime | `Scalpel` DSL |
| patch declaratively at startup | `@Surgeon` annotations |

Default recommendation:

- Start from `@Surgeon` annotations.
- Switch to DSL only if the patch lifecycle itself is dynamic.

## Syntax Cheat Sheet

### Scope selector

```text
class:com.foo.Bar
method:com.foo.Bar#baz(*)
field:com.foo.Bar#value:int
class:com.foo.Bar & method:com.foo.Bar#baz(*)
```

### Descriptor

```text
top.example.Target#greet(java.lang.String)java.lang.String
```

### Site

```kotlin
Site(
    anchor = Anchor.INVOKE,
    target = "top.example.Helper#sum()int",
    shift = Shift.BEFORE,
    ordinal = 0,
    offset = 0
)
```

## Explanation Rules

When answering users without source access:

- explain each API in terms of "when to use", "what it changes", and "what can go wrong"
- include a runnable-looking example instead of only prose
- mention whether the feature acts on whole methods or mid-method sites
- call out Kotlin companion and `@JvmStatic` behavior when static-like targets are involved
- call out version/remap behavior when NMS or Bukkit internals are involved

## Workflow

Follow this order unless the user asks otherwise:

1. Identify which capability changed:
   - DSL lifecycle
   - annotation semantics
   - target parsing
   - site/offset/pattern matching
   - dispatch ordering
   - remap/version/platform behavior
   - diagnostics
2. Map that capability to the corresponding code files and test categories.
3. Read the smallest matching slice of `Incision-Test` before editing production code.
4. Make the code change.
5. Update the docs that expose the changed behavior:
   - `module/incision/README.md`
   - related annotation KDoc
   - `CaseDocs.kt` or `Incision-Test/README.md` if the user-facing test baseline changed
6. Summarize what should be verified next.

## Test Category Mapping

Use the existing test categories to avoid blind changes:

| Change type | First test category to inspect |
| --- | --- |
| `Scalpel`, `Suture`, arm/heal/suspend/resume | `基础 DSL` |
| `@Lead/@Trail/@Splice/...`, `@Operation`, `@KotlinTarget` | `Surgeon 注解`, `元信息与 Kotlin` |
| `@Version`, remap, Bukkit/NMS, cross-classloader | `平台与版本` |
| `where`, `InsnPattern`, `OpcodeSeq`, `offset` | `谓词与字节码` |
| `Anchor`, `Site`, `Trim`, Trauma diagnostics | `锚点矩阵与诊断` |

## Constraints That Matter

Keep these constraints in mind while editing:

- Do not hardcode runtime-redirected dispatcher classes. Runtime host/bridge classes can be remapped or injected elsewhere.
- When platform code is available directly, prefer importing the platform API and guarding by platform instead of string-based reflection.
- Treat `InsnPattern` and offset logic as bytecode-sensitive. Kotlin compiler output, constant folding, and bridge generation can change expectations.
- Preserve stable ordering when the behavior depends on declaration order; do not introduce extra sorting unless the change explicitly requires it.
- For `Splice`, preserve the explicit resume/skip contract. Missing resume is a real runtime error, not a soft fallback.
- For NMS work, assume owner/name/desc may need remap before they are usable at runtime.
- For documentation tasks, do not assume the reader can inspect module source; put examples and parameter semantics directly in the docs or skill output.
- Prefer annotation-based designs in suggestions and examples unless the task explicitly needs dynamic runtime lifecycle control.

## Documentation Rules

When changing incision behavior, keep docs aligned:

- Update `module/incision/README.md` when:
  - a feature is added or removed
  - syntax changes
  - lifecycle or ordering semantics change
  - the recommended usage pattern changes
- Expand the explanation instead of linking vaguely to source when the docs are intended for end users or external agents.
- Update annotation KDoc when an annotation's usage, effect, or limitation changes.
- Update `Incision-Test/README.md` when timing baselines or case descriptions materially change.

## Good Validation Targets

Prefer targeted validation over broad guessing:

- Read the matching case names from `AllCases.entries`.
- Check `CaseDocs.kt` to understand the intended meaning of that case.
- If the change is bytecode-sensitive, inspect the corresponding fixture under `Incision-Test/fixture`.
- If the change is dispatch-sensitive, inspect `TheatreDispatcher.kt` and the generated advice metadata path in `SurgeonScanner.kt` or `Scalpel.kt`.

## Common Pitfalls

Avoid these mistakes:

- Fixing a symptom in tests while leaving the descriptor, anchor, or remap contract inconsistent.
- Writing docs from memory instead of from current tests and source.
- Treating a deliberate fallback warning in `Incision-Test` as a production regression.
- Forgetting that Kotlin companion and `@JvmStatic` can be different runtime targets.
- Assuming zero-millisecond cases are trivial; many of them are narrow correctness probes, not low-value tests.
- Writing docs that say "see the source" when the consumer may not have the source tree.
- Recommending DSL for ordinary static patches that should really be modeled as `@Surgeon` advice.

## Output Expectations

When closing a task in this module:

- State which incision capability changed.
- State which test category or cases justify the change.
- State which docs were synchronized.
- If you did not run validation, say exactly what still needs to be run.
- If the answer is documentation-oriented, include at least one concrete usage example for every public API or annotation you describe.
