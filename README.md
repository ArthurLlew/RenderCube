RenderCube (alpha):
==============================
This mod allows to export parts of minecraft world (even with
mods) into _Blender_ . This repository also contains _Blender_
importer addon, that imports exported geometry.

Usage:
------------------------------

There are currently two commands with keybindings:
1) Render
2) Dump Textures

(1) exports a small cubic region around the player. (2) dumps
all texture atlases used by game at the moment of pressing a
button.

The first one is aimed primary for exporting large parts of the
world. Exported part should be <= 450 cubes by both X and Z axis.
If you ask to render more, appropriate error will be shown in
console.

After you hit _Render_ key, try not to move, because it may
slow down the operation. It may take some time. Once it's done,
navigate to you game folder (_.../.minecraft_) where you will
notice new directory called _rendercube_. There you will find
files with extension _.rcube_ (contains geometry) and another
directory, called _texture_atlases_. The latter contains dumped
texture atlases.

After successful export open _Blender_ and activate provided
addon. Use new option in import menu, named _RenderCube
(.rcube)_ and navigate to exported _.rcube_ files. 

**Note:** you can select multiple files at once.

After it will load meshes (may take up to 5-10 minutes)
you will see as many new objects as there were files to
import. They will all already have proper materials.

**Note:** most non-block entities will have different texture
atlases or even single textures (which are not automatically
exported) in use. So, you should figure out (mostly, by names
of atlases and textures) which one is which.

**Note:**  there is an option (checked by default) to check
if a material, that is about to be created, already exists
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
2) Currently, mod does not support mods like _chisel and bits_
which use custom renders.
3) Some optimization mods (like _entity culling_) that tweak
rendering may cause the mod to malfunction. Luckily, _Optifine_
is not included in this list.

Credits:
------------------------------
For the version 1.18.2 I borrowed some code for texture atlas
export from this repository:
https://github.com/mezz/TextureDump/tree/06e730da6f16a064637135ae32d2a4bf34585698