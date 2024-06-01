RenderCube (Beta):
==============================
This Minecraft tool aims to provide a way for exporting
Minecraft builds into _Blender_ even if the game uses
other mods. The tool consists of a Minecraft mod and a
_Blender_ addon, used to load exported geometry.

Usage:
------------------------------

Currently, the mod has two functions with keybindings:
1) Render _(default key = R )_
2) Dump Textures _(default key = \\ )_

(1) opens a GUI that provides export functionality.
(2) dumps all texture atlases used by the game at the moment
of pressing the button.

Currently, the _render_ menu has two render modes: _relative
position render_ and _absolute position render_. In the first
mode coordinates of the player are added to the input
coordinates in order to obtain respective world positions. In
the second mode coordinates are considered to already
represent world coordinates in the current dimension
(eg. Overworld, Nether, etc.).

After you hit the _Render_ button, do not close the menu
(otherwise, you will abort the operation). Once _rendering_ is
done, navigate to your game folder (_.../.minecraft_) where you
will notice a new directory called _rendercube_. There you will
find files with extension _.rcube_ (contains geometry) and
another directory, called _texture_atlases_. The latter
contains dumped texture atlases.

**Note:** some optimization mods (like _entity culling_) that
tweak rendering may cause the mod to malfunction.

After successful export, open _Blender_ and activate the
provided addon. Use a new option in the import menu, named
_RenderCube (.rcube)_ and navigate to exported _.rcube_ files.

**Note:** you can select multiple files at once.

Once meshes are loaded (may take some time)
you will see as many new objects as there were files to
import. All of them will already have proper materials.

### **Important notes:**
1) Most non-block entities use different texture atlases
   or maybe even single textures (which are not
   automatically exported).
3) In importer there is an option (checked by default)
   to reuse materials already existing in the scene (is
   performed name wise, omitting .<numbers> at the end).
   You can turn this off, if you wish to export geometry
   with different texture atlases in use.
4) Minecraft uses _'overlapping'_ faces (faces being very
   close to one another). For example, that will happen with grass
   blocks. They have extra outer faces on sides (they hold biome
   colored layers). Such faces will render black. You can fix it
   via selecting all faces that cause issue with the help of the
   _UV Editing_ Blender menu, setting _Transform Orientations_ to
   _Normal_ (so that all transforms happen along normals), setting
   _Transform Pivot Point_ to _Individual Origins_ and then
   finally pressing _G_ and _Z_ (move along Z-axis) to move them
   away just a bit. For me distance 0.01-0.001 works just fine.