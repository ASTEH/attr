# Attract To Sound — HUD customization

This repository contains a source overlay for Attract To Sound 7.0.2, Minecraft 1.21.1, NeoForge.

It is based on upstream branch 7.0.2-1.21.1-neoforge, pinned to commit beabac3bed5baee4a0f179b47965766824dc307b. The upstream gameplay and AI code are not rewritten; this repository replaces only the client HUD keybind/config files and adds the in-game editor.

## What's changed

- Smaller default awareness HUD scale (75%) and slightly smaller world mob markers (80%).
- A bindable Configure awareness HUD key in Minecraft's Controls screen.
- An in-game editor with a live preview of the awareness arc.
- Drag the awareness HUD arc with the mouse to place it around the screen. The editor automatically chooses the left or right anchor and stores the offsets.
- Adjust HUD scale, opacity, and the size of mob alert markers.
- Toggle visibility, environmental noise, scent, and wind-shield indicators.
- Reset the HUD position to the bottom-left corner.
- Settings are stored in the existing client config at config/soundattract/awareness.toml.

## Apply to a local upstream checkout

Clone the upstream source branch. Then run this repository's PowerShell script, pointing it at the cloned folder:

    git clone --branch 7.0.2-1.21.1-neoforge https://github.com/Sylsatra/attract_to_sound.git
    .\apply-patch.ps1 -TargetPath "C:\path\to\attract_to_sound"

Build the patched project as usual:

    .\gradlew build

In game, open Options -> Controls -> Key Binds, find Sound Attract, and bind Configure awareness HUD. Join a world and press that key. Drag the HUD arc to move it; use the controls at the top to adjust its appearance.

## Build artifact

The GitHub Actions workflow in this repository fetches the pinned upstream source, applies the overlay, builds with Java 21, and uploads the mod JAR as an Actions artifact.

This is intentionally an overlay repository, not a second full copy of the upstream mod. The large optional dependency JARs remain sourced from the original repository during the automated build.
