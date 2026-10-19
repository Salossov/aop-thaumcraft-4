# Thaumcraft Recipe Addon (JEI/EMI)

[Русский](README.ru.md)

Optional client integration for AOP: Thaumcraft 4. The source module is `emi-jei-integration-aoptc4`; its runtime mod ID remains `thaumcraft_viewers` and its artifact is `thaumcraft-viewers-0.7.3.jar`.

The core works without this module's JAR, JEI or EMI. For integration, install the core and its required dependencies, this JAR, and a supported viewer. JEI/EMI are not embedded. If both are installed, the native EMI adapter takes precedence.

| Component | Supported version |
| --- | --- |
| Minecraft / NeoForge | 1.21.1 / 21.1.252+ within 21.1 |
| AOP: Thaumcraft 4 | 0.7.1+; included in the 1.0.0 verification |
| JEI | `[19.8.4.110,20)` |
| EMI | `[1.1.13,1.2)` |

Run from the repository root in PowerShell:

```powershell
.\gradlew.ps1 :emi-jei-integration-aoptc4:build
.\gradlew.ps1 :emi-jei-integration-aoptc4:check -PviewerCompatibility=minimum
.\gradlew.ps1 :emi-jei-integration-aoptc4:check -PviewerCompatibility=middle
.\gradlew.ps1 :emi-jei-integration-aoptc4:check -PviewerCompatibility=latest
```

Output: `emi-jei-integration-aoptc4/build/libs/thaumcraft-viewers-0.7.3.jar`. The addon version is independent of the core version.

Common catalog and presentation code lives under `src/main/java/thaumcraft/viewers/`; isolated adapters are in `jei/` and `emi/`. Common code uses the public Thaumcraft API. Metadata and EMI plugin discovery are under `src/main/resources/`. Configuration is `thaumcraft_viewers-client.toml`.

See [addon API documentation](../docs/addons/README.en.md) for recipe displays, aspect discovery, research links and lossless server-authorized arcane grid transfer. Original Thaumcraft belongs to Azanor; see [NOTICE](../NOTICE.md). Moving the module does not change the project's usage conditions.
