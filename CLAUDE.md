# CommonsLangAndIO

Pure-Java 17 utility library (`org.omnaest.utils`). No framework dependencies, no logging in main code — designed to be a lean dependency for other projects in the workspace.

## Build

```cmd
mvn clean install
mvn test -Dtest=MyTestClass#myMethod
```

## Architecture

Every domain follows the same pattern:

- **Static `*Utils` facade** — the public entry point (`StreamUtils.of(...)`, `FileUtils.toFile(...)`)
- **Inner interfaces** — define the fluent/builder API as nested types within the facade class
- **`.internal` subpackages** — hold concrete implementations; callers never reference these directly

Example: `FileUtils` exposes `FileReaderLoader`, `FileNavigator`, `CommitableFile` as inner interfaces; implementations live in `org.omnaest.utils.file.storage.*`.

## Code style

- **No Lombok** — classes are written by hand (only 2× `@Data`, 2× `@Builder` in the whole project)
- **No logging** — library code; logging is left to callers
- **Factory pattern everywhere** — `of()` (101 usages), `newInstance()` (83), `from()` (38)
- **Functional-first** — 12× `@FunctionalInterface`; custom extensions beyond JDK: `TriFunction`, `TriConsumer`, `BidirectionalFunction`, `UnaryBiFunction`, `OptionalFunction`
- **Stream-heavy** — 200+ `.stream().map().collect()` chains; `StreamUtils` is the largest class
- **Checked exceptions wrapped** — `RuntimeIOException`, `RuntimeFileNotFoundException` wrap IO exceptions at boundaries; ~153 lines of `throws` declarations in file/IO code

## Package map

| Package | What lives here |
|---|---|
| `org.omnaest.utils` | 46 top-level `*Utils` facades |
| `bitset` | Binary, hex, tri-state, enum-backed bit manipulation |
| `buffer` | `CyclicBuffer` with windowed access |
| `counter` | `ProgressCounter`, `DurationProgressCounter` with consumer callbacks |
| `duration` | `DurationCapture` for timing operations |
| `element` | Bi/tri/lar (left-and-right) value holders; cached, transactional, thread-local variants |
| `events` | `DistributingEventHandlerManager` |
| `exception` | Exception handler chain; `RuntimeIOException`, `RuntimeFileNotFoundException` |
| `file` | `CommitableFile` (transactional file writes), `HashTextFileIndex`, `BlockFileStorage` |
| `functional` | Custom functional interfaces (`TriFunction`, `BidirectionalFunction`, etc.) |
| `list` | CRUD list, cyclic list, enum-backed list, projection/aggregation builders |
| `lock` | `LockMap`, `SynchronizedAtLeastOneTimeExecutor` |
| `map` | CRUD map, atomic counter maps, `SupplierMap`, `MediatedMap` |
| `optional` | `BiOptional`, `NullOptional` |
| `stream` | `SupplierStream`, `StreamDecorator`, `Streamable` |
| `supplier` | `OptionalSupplier`, `EnumSupplier`, `SupplierConsumer` |

## Key classes

- **`StreamUtils`** — largest class; stream pipelines, merging, batching, windowing, parallelism
- **`FileUtils`** — file reading/writing with fluent builder; batch reads, encoding, streaming
- **`MatcherUtils`** — regex matching with token interpretation, region replacement, fluent match API
- **`BeanUtils`** — reflection-based bean access, property flattening, map↔bean mapping
- **`ProxyUtils`** — dynamic proxy builder with method handler registration
- **`ExceptionUtils`** — wraps checked-exception operations for use in lambdas

## Dependencies (compile scope)

- `commons-io`, `commons-lang`, `commons-lang3`, `commons-text` — Apache Commons
- `guava` — Guava utilities
- `parallel-collectors` — parallel stream collection
- `jakarta.xml.bind-api` — JAXB API

Test scope: JUnit 5 + JUnit 4 (via `CommonsTest`), Mockito, log4j2 + slf4j (via `CommonsLog`).
