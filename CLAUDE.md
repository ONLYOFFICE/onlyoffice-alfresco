# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & tooling

Multi-module Maven aggregator (`onlyoffice-integration`) with two Alfresco AMP modules: `repo` (platform) and `share`.

```bash
mvn clean install            # builds both modules, produces the AMPs
mvn install -pl repo -am     # single module (plus parent)
mvn checkstyle:check         # style check, see caveat below
```

Artifacts (names fixed via `<finalName>`, independent of the Maven version suffix):
- `repo/target/onlyoffice-integration-repo.amp` → Alfresco platform WAR
- `share/target/onlyoffice-integration-share.amp` → Share WAR

Source/target level is Java 11 (`maven.compiler.source/target`); CI (`.github/workflows/*.yml`) builds with Temurin JDK 17.

There are **no tests** in this repository — no `src/test`, no surefire config. Verification is manual: install both AMPs into a running Alfresco + ONLYOFFICE Docs pair.

Stale instructions in `README.md`: it mentions `git submodule update --init --recursive` and `docker-compose up`, but there is no `.gitmodules` and no compose file. The CI workflows still call the submodule command (a no-op). There is also no `alfresco:run` harness — `alfresco-maven-plugin` is only pulled in as a dependency of `maven-assembly-plugin` to get the `amp` assembly format.

### Checkstyle

`checkstyle.xml` at the root is the style contract: 120-column limit, alphabetically sorted imports in `THIRD_PARTY → STANDARD_JAVA → STATIC` groups with blank lines between groups, `FinalParameters` (all method params must be `final` — this is why the codebase is full of `final` params), `MagicNumber`, `MissingSwitchDefault`, `HiddenField`, `TodoComment` (TODOs are violations), and a mandatory license header from `onlyoffice.header` (`Copyright (c) Ascensio System SIA <year>`). Suppressions live in `checkstyle-suppressions.xml`.

Caveat: `maven-checkstyle-plugin` is declared only in the parent's `<pluginManagement>` and never added to either module's `<build><plugins>`, so **it does not run during `mvn install`**. Match the rules by hand when writing code; a direct `mvn checkstyle:check` may fail to resolve `configLocation` from a submodule directory.

### Versioning & release

Version lives in the root `pom.xml` and is inherited by both modules — bumping it means editing the parent version plus the `<parent>` block in `repo/pom.xml` and `share/pom.xml`. Push to `master` triggers `create-tag.yml`, which reads the version from the **first** `X.Y.Z` in `CHANGELOG.md` and tags `vX.Y.Z`; the tag triggers `release.yml`, which builds the AMPs and cuts a GitHub release with the top CHANGELOG section as the body. So the CHANGELOG's leading version and the pom version must agree before merging to `master`.

Work happens on `develop`; `master` is release-only. Commits follow Conventional Commits (`feat:`, `fix:`, `refactor(scope):`, `build:`).

## Architecture

The integration is a thin Alfresco-specific adapter over **`com.onlyoffice:docs-integration-sdk`**. The SDK owns the protocol with ONLYOFFICE Document Server (config building, JWT, callback dispatch, convert service, format tables); this repo supplies Alfresco-backed implementations of the SDK's SPIs and exposes them as web scripts.

### Two deployment halves

- **`repo`** — package `com.parashift.onlyoffice` (historical package name; keep it). Spring beans are wired in `repo/src/main/resources/alfresco/extension/onlyoffice-context.xml`, which does a `<context:component-scan>` over the whole package, so web scripts are plain `@Component("webscript.onlyoffice.<name>.<method>")` classes matched to `*.desc.xml` descriptors under `resources/alfresco/templates/webscripts/onlyoffice/`. SDK managers/services are declared explicitly as beans in the same file (constructor-injected with each other, field-injected with Alfresco services via `@Autowired`).
- **`share`** — package `com.onlyoffice.web`. No component scan; every bean is declared in `share/src/main/resources/alfresco/web-extension/onlyoffice-module-context.xml`. UI wiring (actions, action groups, create-content menu items, metadata banners, JS/CSS dependencies) is in `resources/alfresco/onlyoffice-config.xml`.

Share never talks to Document Server and holds no settings of its own: `OnlyofficeSettingsQuery` calls the repo web script `/parashift/onlyoffice/onlyoffice-settings` via `ScriptRemote` and caches the answer in **static fields with a 10-second TTL**. All doclib evaluators (`IsEditable`, `IsViewable`, `IsConvertible`, `IsCorrectDownloadAs`) read that cache — a settings change takes up to 10s to show up in Share menus.

### Web script surface (repo)

All under `/alfresco/s/parashift/onlyoffice/`:

| URL | Class | Auth | Role |
| --- | --- | --- | --- |
| `prepare` | `Prepare` | user | Builds the editor config; also creates blank/new documents |
| `prepare-quick-share` | `PrepareQuickShare` | none | Same for public shared links |
| `callback` | `CallBack` | **none** | Document Server → Alfresco status callbacks |
| `download/{type}` | `Download` | user | `file` and `diff` payloads for Document Server |
| `download-as` | `DownloadAs` | user | Convert-and-download |
| `editor-api/{type}` | `EditorApi` | user | In-editor operations: `insert`, `save-as`, `favorite`, `from-docx`, `reference-data` |
| `history/{type}` | `History` | user | `info` / `data` for version history in the editor |
| `convert` | — (`ConvertAction`) | user | The `onlyoffice-convert` action executer |
| `convertertest` | `ConverterTest` | guest | Reachability probe used by settings validation |

Admin settings page is a separate web script family at `/alfresco/s/onlyoffice/onlyoffice-config` (`Config` GET / `ConfigCallback` POST / `ConfigValidation`), rendered from `onlyoffice-config.get.html.ftl`.

`callback` is unauthenticated by design — trust comes from the JWT in the security header, verified by `callbackService.verifyCallback(callback, authorizationHeader)`. It then runs `processCallback` inside a `RetryingTransactionHelper` transaction. Do not add an auth requirement there.

### Editor page flow (Share)

Surf page `onlyoffice-edit` (`site-data/pages/onlyoffice-edit.xml` → template-instance → `templates/com/parashift/onlyoffice-edit.ftl` → global component `onlyoffice-edit`). Its controller `site-webscripts/com/parashift/onlyoffice-edit.get.js` does `remote.call("/parashift/onlyoffice/prepare?...")`, unpacks the JSON into the Freemarker model (`documentServerApiUrl`, `editorConfig`, history/favorite URLs, …), and the FTL loads the Document Server API script and instantiates `DocEditor`. When called with `parentNodeRef`+`new` instead of `nodeRef`, `prepare` creates the node first and the page redirects to itself with the new `nodeRef`.

### SDK SPI implementations (`repo/.../sdk/`)

- `SettingsManagerImpl` — settings are Alfresco **attributes** keyed `onlyoffice.<name>`, falling back to the `global-properties` bean (i.e. `alfresco-global.properties`). Writes always go to attributes, so the settings page overrides the properties file. Any new setting is automatically readable/writable through this prefix; no schema to declare.
- `UrlManagerImpl` — every URL Document Server sees. `getFileUrl`/`getTestConvertUrl`/`getHistoryDiffUrl` append `alf_ticket` from `AuthenticationService.getCurrentTicket()`. `getAlfrescoUrl()` prefers the `PRODUCT_INNER_URL` setting (the Docker-network-internal address) over `UrlUtil.getAlfrescoUrl(sysAdminParams)`; note the two branches append `/` vs `/alfresco/` differently.
- `DocumentManagerImpl` — document key strategy: while a node is **not** locked in the editor the key is derived (`<uuid>_<versionLabel>`, `_embedded` suffix for embedded mode); while it **is** locked, the key is the random value stored in the `od:documentKey` property, so the key stays stable across a co-editing session.
- `ApacheHttpclientDocumentServerClientImpl` — HttpClient5 transport, plus `getFileWithoutCloseOutputStream` (the extra method on the local `sdk.client.DocumentServerClient` interface) for streaming into an Alfresco `ContentWriter` without closing its stream.
- `CallbackServiceImpl` — the core state machine (see below).
- `SettingsValidationServiceImpl` — powers the settings page's "check connection".

### Locking, document keys, and the content model

`onlyoffice-docs-model.xml` defines aspect `od:editingInOnlyofficeDocs` with property `od:documentKey`; constants in `model/OnlyofficeDocsModel`.

`EditorLockManager` is the single place that manipulates editor locks. It always pairs a `READ_ONLY_LOCK` with the aspect, wraps changes in `behaviourFilter.disableBehaviour()`, and ensures versioning is enabled first. Key invariants:

- `lockInEditor(nodeRef, TIMEOUT_CONNECTING_EDITOR /* 60s */)` is taken optimistically in `Prepare` with a freshly generated random key, and promoted to `TIMEOUT_INFINITY` once Document Server actually connects.
- `isLockedInEditor` = locked **and** has the aspect; `isLockedNotInEditor` = locked by something else (checkout, another user) — that case must abort.
- `isValidDocumentKey` compares the callback key against `od:documentKey`; a mismatch is always a hard failure. Every callback handler re-checks lock state + key before touching content.
- `changeLockOwner` re-locks as another user via `AuthenticationUtil` when the original lock owner disconnects but others keep editing.
- `NodeEventHandler` binds `onRemoveAspect` for `cm:lockable`: if an admin force-unlocks a node that is still being edited, it removes the aspect and sends `INFO` + `DROP` commands to Document Server to evict the clients.

Callback handlers in `CallbackServiceImpl` each `setFullyAuthenticatedUser(action.getUserid())` and run per-action — Alfresco usernames are used directly as Document Server user ids. `handlerSave`/`handlerForcesave` convert back to the original extension when the editor saved a different filetype (`convert()` builds a key by SHA-256 of the file URL), write a new version through `NodeManager.createNewVersion` (MAJOR unless the `minorVersion` setting is on), persist `changes.json`/`diff.zip` via `HistoryManager`, and post an activity feed entry. Forcesave versions are marked with the `onlyoffice:forcesave` version property so the next real save can delete their history artifacts.

### Formats

Supported/editable/convertible format lists come from the SDK's `DocumentManager` (`getFormats`, `getLossyEditableMap`, `getDefaultConvertExtension`), not from this repo. Format-support changes usually mean bumping the `docs-integration-sdk` version in the root pom, not editing code. `Config` adds a couple of local defaults on top (e.g. `txt`/`csv` lossy-editable when the `LOSSY_EDIT` setting is unset).

### i18n

Every user-visible string is in `.properties` bundles duplicated across 8 locales (`de es fr it pt ru uk` + default) in three places: `repo/.../alfresco/messages/onlyoffice*`, `repo/.../web-extension/messages/admin-console*` (settings page), `repo/.../webscripts/onlyoffice/prepare.get*` and `config/onlyoffice-config.get*` (web-script-scoped), and `share/.../alfresco/messages/onlyoffice*`. Adding a key means adding it to all locale variants of the relevant bundle. Bundles are registered through `ResourceBundleBootstrapComponent` beans in the two Spring context files.

### Resource filtering

`src/main/resources` is filtered with Maven properties (that's how `module.properties` gets the version), but `ftl`, `css`, `js`, `ico` and the Office template extensions are excluded from filtering in the parent pom's `maven-resources-plugin` config. If you add a resource type containing `${...}` literals, add its extension to `<nonFilteredFileExtensions>`.
