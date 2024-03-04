RenderCube (alpha):
==============================
This mod implements a method of exporting parts of the
Minecraft world into _Blender_. Method even works with
mods (most of them, at least). This repository also
contains _Blender_ importer addon, that loads exported
geometry.

Usage:
------------------------------

Currently, the mod has two functions with keybindings:
1) Render _(default key = R )_
2) Dump Textures _(default key = \\ )_

(1) opens a GUI that provides controls for world export.
(2) dumps all texture atlases used by the game at the moment
of pressing the button.

After you hit the _Render_ button, do not close the menu.
Otherwise, you will abort the operation. Once it's done,
navigate to your game folder (_.../.minecraft_) where you will
notice a new directory called _rendercube_. There you will find
files with extension _.rcube_ (contains geometry) and another
directory, called _texture_atlases_. The latter contains dumped
texture atlases.

After successful export, open _Blender_ and activate the
provided addon. Use a new option in the import menu, named
_RenderCube (.rcube)_ and navigate to exported _.rcube_ files. 

**Note:** you can select multiple files at once.

Once meshes are loaded (may take some time)
you will see as many new objects as there were files to
import. All of them have proper materials already.

### **Important notes:**
1) Most non-block entities will use different texture
atlases or even single textures (which are not automatically
exported). You should figure out (mostly, by names
of atlases and textures) which one is which.
2) In importer there is an option (checked by default)
to reuse materials already existing in the scene (is
performed name wise, omitting .<numbers> at the end).
You can turn this off, if you wish to export geometry
with different texture atlases in use.
3) Minecraft uses _'overlapping'_ faces (faces being very
close to one another). For example, that will happen with grass
blocks. They have extra outer faces on sides (they hold biome
colored layers). Such faces will render black. You can fix it
via selecting all faces that cause issue with the help of the
_UV Editing_ Blender menu, setting _Transform Orientations_ to
_Normal_ (so that all transforms happen along normals), setting
_Transform Pivot Point_ to _Individual Origins_ and then
finally pressing _G_ and _Z_ (move along Z-axis) to move them
away just a bit. For me distance 0.0001 works just fine.
4) Currently, the rendering method does not support mods
like _chisel and bits_ which use custom renders.
5) Some optimization mods (like _entity culling_) that tweak
rendering may cause the mod to malfunction. Luckily, _Optifine_
is not included in that list.

Credits:
------------------------------
For the version 1.18.2 I borrowed some code for texture atlas
export from this repository:
https://github.com/mezz/TextureDump/tree/06e730da6f16a064637135ae32d2a4bf34585698