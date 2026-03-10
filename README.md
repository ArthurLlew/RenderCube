RenderCube
==============================
![Image alt](https://github.com/ArthurLlew/RenderCube/raw/neoforge-1.21.1/preview.png)

This toolchain is designed to export
_**Minecraft**_ builds into _**Blender**_ even if the game has
any mods installed. The toolchain consists of a
Minecraft mod (export) and a Blender addon (import).

For detailed introduction one can refer to [this](
https://www.ardacraft.me/resources/complete-guide-to-rendering-modded-minecraft-builds-in-blender) amazing guide.

Setup
------------------------------
1) Drop _**Minecraft mod**_ into relevant mod directory
2) Install _**Blender addon**_
   (see [Add-ons - Blender Manual](https://docs.blender.org/manual/en/latest/editors/preferences/addons.html))

Config
------------------------------
There are several aspects that can be tweaked via
_**config/rendercube.json**_:
1) **Render distance** _(default = 400)_: how big
   can the render region be made across X and Z.
2) **Use Minecraft Ambient Occlusion** _(default = false)_:
   whether rendering should capture Minecraft builtin ambient
   occlusion.
3) **Block Consumer Configs**: this list may contain either
   _**<CLASS, class string, filename, culling rule>**_ or
   _**<BLOCK, block registry string, filename, culling rule>**_;
   these entries will redirect geometry from specified classes or
   blocks into a file with a filename provided in the entry;
   _**culling rule**_ controls culling (hiding) of faces
   overlapped by the neighboring block; _**class string**_ example
   can be found in default config; _**block registry string**_ is
   something like "minecraf:oak_log" ("\<namespace>:\<id>").

Export
------------------------------
1) Open render menu _( default key = **R** )_.
2) Select render mode by opening relative tab. In both
   modes you have to provide a set of two positions
   in the current dimension (eg. Overworld, Nether, etc.).
   In the first mode input coordinates are added
   to the player coordinates in order to obtain
   respective world positions. In the second mode input
   coordinates are considered to represent world positions.
3) Hit _**Render**_ button (the screen will be locked until
   rendering is complete or error is encountered).
4) Exported geometry will be located in
   _**path_to_game_folder/.minecraft/rendercube/date_time_of_export/**_
   in the form of multiple _**.rcube**_ files. They can be batch
   loaded into _**Blender**_ using provided addon.
5) Press _**F3+S**_ to dump texture atlases loaded by game into
   _**screenshots/debug**_ directory
   (https://minecraft.fandom.com/wiki/Texture_atlas).

Import
------------------------------
1) Open _**Blender**_.
2) Make sure provided addon is activated.
3) Use new import option named _**RenderCube (.rcube)**_ to
   load _**.rcube**_ file(s).

Once meshes are loaded (may take some time)
you will see as many new objects as there were files to
import. All of them will already have proper materials.

Limitations and tips
------------------------------
1) Some optimization (not including Sodium, Rubidium and
   their addons) mods that tweak rendering may cause
   errors when rendering.
2) No _Java_ exceptions encountered while rendering should
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
5) In importer there is an option (checked by default)
   to reuse materials already existing in the scene (is
   performed name wise, omitting '.\<numbers\>' at the
   end). You can turn this off, if you wish to export
   geometry with different texture atlases in use.
6) When using _Minecraft_ textures don't forget to
   change _Texture interpolation_ from _Linear_ to _Closest_.
7) _Minecraft_ uses _'overlapping'_ faces (faces located
   very close to one another). For example, that is
   true for grass blocks. They have extra outer faces
   on sides (they hold biome colored layer). Such
   faces will render black. You can fix it via
   selecting all faces that cause issue with the
   help of the _UV Editing_ Blender menu, setting
   _Transform Orientations_ to _Normal_ (so that
   all transforms happen along normals), setting
   _Transform Pivot Point_ to _Individual Origins_
   and then finally pressing _G_ and _Z_ (move along
   Z-axis) to move them away for a tiny amount.