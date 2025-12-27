RenderCube
==============================
![Image alt](https://github.com/ArthurLlew/RenderCube/raw/neoforge-1.21.1/preview.png)

This _Minecraft_ toolchain is designed to export
_Minecraft_ builds into _Blender_ even if the game has
any mods installed. The toolchain consists of a
_Minecraft_ mod (export) and a _Blender_ addon (import).

Setup
------------------------------
1) Drop _Minecraft_ mod into relevant mod directory
2) Install _Blender_ addon
   (see [Add-ons - Blender Manual](https://docs.blender.org/manual/en/latest/editors/preferences/addons.html))

Export
------------------------------
1) Open render menu _( default key = R )_.
2) Select render mode by opening relative tab. In both
   modes you have to provide a set of two positions
   in the current dimension (eg. Overworld, Nether, etc.).
   In the first mode input coordinates are added
   to the player coordinates in order to obtain
   respective world positions. In the second mode input
   coordinates are considered to represent world positions.
3) Hit _Render_ button (the screen will be locked until
   rendering is complete or error is encountered).
4) Press F3+S to dump texture atlases loaded by game into
   "screenshots/debug" directory
   (https://minecraft.fandom.com/wiki/Texture_atlas).

Once _rendering_ is done, yuo can navigate to Minecraft
folder (_.../.minecraft_) where you will notice a new
directory called _rendercube_. There you will find
subdirectories named after date and time when you exported
geometry. Inside each subdirectory there will be files
with extension _.rcube_. Those can be loaded into
_Blender_ using provided addon.

Import
------------------------------
1) Open _Blender_.
2) Make sure provided addon is activated.
3) Use new import option named _RenderCube (.rcube)_ to
   load _.rcube_ file(s).

Once meshes are loaded (may take some time)
you will see as many new objects as there were files to
import. All of them will already have proper materials.

Limitations and tips
------------------------------
1) Some optimization mods that tweak rendering may cause
   the mod to malfunction.
2) If any mod uses some custom rendering pipeline the
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
6) When using minecraft textures don't forget to
   change _Texture interpolation_ from _Linear_ to _Closest_.
7) Minecraft uses _'overlapping'_ faces (faces located
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
   Z-axis) to move them away a bit.