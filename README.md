RenderCube (Beta):
==============================
![Image alt](https://github.com/ArthurLlew/RenderCube/raw/1.20.1-fabric/preview.png)

This _Minecraft_ toolchain is designed to export
_Minecraft_ builds into _Blender_ even if the game is
using other mods. The toolchain consists of a
_Minecraft_ mod (export) and a _Blender_ addon (import).

Setup:
------------------------------
1) Drop _Minecraft_ mod into relevant mod directory
2) Install _Blender_ addon
(see [Add-ons - Blender Manual](https://docs.blender.org/manual/en/latest/editors/preferences/addons.html))

Usage:
------------------------------

Currently, the mod has two functions with keybindings:
1) Render _(default key = R )_
2) Dump Textures _(default key = \\ )_

(1) opens a GUI that provides export functionality.
(2) dumps all texture atlases used by the game at the moment
of pressing the button.

Currently, the _render_ menu has two render modes: _relative
position render_ and _absolute position render_.
In both modes you have to provide a set of two coordinate
points. In the first mode input coordinates are added
to the coordinates of the player in order to obtain
respective world positions in the current dimension
(eg. Overworld, Nether, etc.).
In the second mode input coordinates are considered to
represent world positions in the current dimension
(eg. Overworld, Nether, etc.).

After you hit the _Render_ button, do not close the menu
(otherwise, you will abort the operation). Once _rendering_ is
done, navigate to your game folder (_.../.minecraft_) where you
will notice a new directory called _rendercube_. There you will
find files with extension _.rcube_ (contains geometry) and
another directory, called _texture_atlases_. The latter
contains dumped texture atlases.

After successful export, open _Blender_ and use a new
import option named _RenderCube (.rcube)_ and navigate
to exported _.rcube_ files.

**Note:** you can select multiple files at once.

Once meshes are loaded (may take some time)
you will see as many new objects as there were files to
import. All of them will already have proper materials.

**Note:** when using minecraft textures don't forget to 
change _Texture interpolation_ from _Linear_ to _Closest_.

Important notes:
------------------------------
1) Some optimization mods (like _entity culling_) that
   tweak rendering may cause the mod to malfunction.
2) Most non-block entities use different texture atlases
   or maybe even single textures (which are not
   automatically exported).
3) In importer there is an option (checked by default)
   to reuse materials already existing in the scene (is
   performed name wise, omitting '.\<numbers\>' at the
   end). You can turn this off, if you wish to export
   geometry with different texture atlases in use.
4) Minecraft uses _'overlapping'_ faces (faces located
   very close to one another). For example, that is
   true for grass blocks. They have extra outer faces
   on sides (they hold biome colored layer). Such
   faces will render black. You can fix it via
   selecting all faces that cause issue with the
   help of the _UV Editing_ Blender menu, setting
   _Transform Orientations_ to _Normal_ (so that
5) all transforms happen along normals), setting
   _Transform Pivot Point_ to _Individual Origins_
   and then finally pressing _G_ and _Z_ (move along
   Z-axis) to move them away a bit. For me
   distance 0.01-0.001 works just fine. It also advised
   to isolate such faces into a different object.