RenderCube (alpha):
==============================
This mod allows to export parts of minecraft world (even with
mods) into blender. This repository also contains blender import
util, that imports exported world parts into blender.

Usage:
------------------------------

There are currently 2 commands:
1) rendercube render cube \<position>
2) rendercube render region \<position 1> \<position 2>

First one is mainly used for debugging one single cube (cube can
contain block, entity or/and liquid).

The second one is aimed primary for exporting large parts of the
world. Exported part should be <= 450 cubes by X and Z axis. If
you ask to render more, appropriate error will be shown in
console.

After you hit _Enter_, try not to move, because it may lag or
crash the game. It may take up to 5-10 minutes to render world
portion of maximum allowed size (depending on geometry
complexity and your computer power). Once it's done, navigate 
to you game folder (_.../.minecraft_) and you will notice new 
directory named _rendercube_. There you will see files with
extension _.rcube_ (contains geometry) and some _.png_
files (these are minecraft texture atlases).

After successful export open _Blender_ and activate provided
addon. You then will see new option in import menu, named
_RenderCube (.rcube)_. Click it and navigate to exported
_.rcube_ files. 

**Note:** you can select multiple files at once.

After it will load meshes (may take up to 5-10
minutes of loading) you will see as many new
objects as there were files to import. They will all have
proper materials already setup.

**Note:** most entities will have different texture atlases
or even single textures (which are not automatically exported)
in use. So, you should figure out (mostly, by names of atlases
and textures) which one is which.

**Note:**  there is an option (checked by default) to check
if a material, that is about to be created, exists already
in a scene (is performed name wise). You can turn this off,
if you export mesh with different texture atlases into scene
with existing materials.

### **Important notes:**
1) Minecraft uses overlapping faces. This
means, that some objects cannot be rendered in cycles and in
eevee  require extra tweaks. This is, for example, true for
grass blocks. They have extra outer faces on sides (they hold
biome colored layer). Such faces will render black. To fix this
in eevee you can open UV editing menu, select all faces, that
cause issue (find place in texture atlas where one is situated
and then select all of them in the same place; most of the
time they are obviously visible, because most faces tend to
occupy one square texture part in atlas), and separate them all
into another object. Then duplicate material on that object and
assign material _Blend Mode_ to _Alpha Blend_.
2) Mod currently does not support mods like _chisel and bits_
which use custom renders.

Credits:
------------------------------
There is some texture atlas export code usage from this
repository:
https://github.com/mezz/TextureDump/tree/06e730da6f16a064637135ae32d2a4bf34585698