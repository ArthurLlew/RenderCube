RenderCube
=========================================================
![Image alt](https://github.com/ArthurLlew/RenderCube/raw/neoforge-1.21.1/preview.jpg)

This toolchain is designed to export
_**Minecraft**_ builds into _**Blender**_ even if the game has
any mods installed. The toolchain consists of a
Minecraft mod (export) and a Blender addon (import).

For detailed introduction one can refer to [this](
https://www.ardacraft.me/resources/complete-guide-to-rendering-modded-minecraft-builds-in-blender) amazing guide.

Setup
---------------------------------------------------------
1) Drop _**Minecraft mod**_ into relevant mod directory
2) Install _**Blender addon**_
   (see [Add-ons - Blender Manual](https://docs.blender.org/manual/en/latest/editors/preferences/addons.html))

Mod Config
---------------------------------------------------------
There are several aspects that can be tweaked via
_**config/rendercube.json**_:
1) **Render distance** _(default = 400)_: how big
   can the render region get across X/Z.
2) **Use Minecraft Ambient Occlusion** _(default = false)_:
   whether rendering should capture Minecraft native ambient
   occlusion.
3) **Block Consumer Configs**: this list of
   _**\<type, condition, filename, culling>**_,
   used to control face culling and redirect captured
   geometry from pre-defined files into a separate file with
   a name provided in the entry; may contain either:
   * _**\<EMISSION, emission level, filename, culling>**_:
     _emission level_ -- integer from 0 to 15 (block emission
     level).
   * _**\<CLASS, class string, filename, culling>**_:
     _class string_ -- full _**Java**_ class name (examples
     can be found in default config).
   * _**\<BLOCK, block registry string, filename, culling>**_:
     _block registry string_ -- <nobr>"\<namespace>:\<id>"</nobr>
     (e.g. <nobr>"minecraft:oak_log"</nobr>).

Export
---------------------------------------------------------
1) Open render menu _( default key = **R** )_.
2) Input min and max block coordinates describing render region
   box.
3) Hit _**Render**_ button (the screen will be locked until
   rendering is complete or error is encountered).
4) Exported geometry will be located in
   _**path_to_game_folder/.minecraft/rendercube/date_time_of_export/**_
   in the form of multiple _**.rcube**_ files. They can be batch
   loaded into _**Blender**_ using provided addon.
5) Press _**F3+S**_ to dump texture atlases loaded by game into
   _**screenshots/debug**_ directory
   (https://minecraft.fandom.com/wiki/Texture_atlas).

### Export Modes
1) **Player Relative Render.** Block coordinates of
   the render region is a sum of input and player
   coordinates.
2) **World Relative Render.** Input coordinates are
   treated as block coordinates of the render region.
3) **LODs Render (Player Relative).** Same as _**Player
   Relative Render**_ for capturing geometry LODs.
4) **LODs Render (World Relative).** Same as _**Player
   Relative Render**_ for capturing geometry LODs.

### Export Options
1) **Do not cull faces on boarder.** By default, the exporter
   culls (hides) block faces based on vanilla rules or config
   entries. If this option is checked, the exporter will
   ignore culling on render region boarder, allowing for
   diorama-like capture.
2) **Split render per chunk.** By default, the exporter
   captures entire render region as one single object.
   If this option is checked, the exporter slices render
   region based on chunk boarders.
3) **LOD level.** This slider sets LODs level (LOD column
   width in blocks): 0 -- width = 1; 1 -- width = 2;
   2 -- width = 4; 3 -- width = 8; 4 -- width = 16
   (whole chunk).

### LODS
Currently, an experimental feature. Allows to render chunks
with decreased level of detail for use as terrain background
in a scene.

Import
---------------------------------------------------------
1) Open _**Blender**_.
2) Make sure provided addon is activated.
3) Use new import option named _**RenderCube (.rcube)**_ to
   load _**.rcube**_ file(s). 
4) Once meshes are loaded (may take some time) there will be
   as many new objects as there were files selected.
5) All objects will have pre-configured materials.

### Import Options
1) **Search for existing materials.** If not checked, the
   importer always creates a new material for a new object.
   If checked, the importer searches for already existing
   materials in the scene (is performed name wise, omitting
   '.\<numbers\>' at the end) and using them instead of
   creating duplicates. Useful for exporting different
   pieces of the same _**Minecraft**_ world.
2) **Unified material.** By default, the importer assigns
   unique materials to each object based on their name
   (which comes from filename). If the option contains
   any text, the importer will use it as material name
   (creating duplicates with '.\<numbers\>' at the end
   by default). option can be paired with **Search for
   existing materials** option to assign a single
   unified material to all imported objects.
3) **Offset.** Offsets geometry by provided vector.

Limitations and tips
---------------------------------------------------------
1) Some optimization mods (not including Sodium, Rubidium
   and their addons!) that tweak rendering may cause errors.
2) No _**Java**_ exceptions encountered while rendering should
   crash the game. All exceptions that interrupt rendering
   are logged.
3) If any mod uses some custom rendering pipeline the
   exported geometry might not contain respective objects.
   Currently, the only supported mod with custom rendering
   is Little Tiles (https://modrinth.com/mod/littletiles).
4) Most non-block entities use different texture atlases
   or even single textures (which are not automatically
   exported and should be manually located in game or mod
   resources).
5) When using _**Minecraft**_ textures don't forget to
   change _Texture interpolation_ from _Linear_ to _Closest_.
6) _**Minecraft**_ uses _'overlapping'_ faces (faces located
   very close to one another). For example, that is
   true for grass blocks. They have extra outer faces
   on sides (they contain biome colored layer). Such
   faces will render black. One can fix it via
   selecting all faces that cause issue with the
   help of the _UV Editing_ Blender menu, setting
   _Transform Orientations_ to _Normal_ (so that
   all transforms happen along normals), setting
   _Transform Pivot Point_ to _Individual Origins_
   and then finally pressing _G_ and _Z_ (move along
   Z-axis) to move them away for a tiny amount.