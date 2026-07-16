###########
# Imports #
###########


# Blender
import bpy
# Package import
import importlib
# Custom lib
if "load_modules" in locals():
    importlib.reload(importer)
else:
    from . import importer


###############
# Add-on info #
###############


bl_info = {
    "name": "RenderCubeImporter",
    "author": "Arthur Llew",
    "version": (2, 0),
    "description": "RenderCube captured geometry importer.",
}


#######################
# Add-on Registration #
#######################


# Adds new option to import menu
def menu_func_import(self, context):
    self.layout.operator(importer.RenderCubeImporter.bl_idname, text='RenderCube (.rcube)')


# Addon registration
def register():
    # Register importer
    bpy.utils.register_class(importer.RenderCubeImporter)
    
    # Add to the "file selector" menu (required to use F3 search for quick access)
    bpy.types.TOPBAR_MT_file_import.append(menu_func_import)


# Addon unregistering
def unregister():
    # Remove from "file selector" menu
    bpy.types.TOPBAR_MT_file_import.remove(menu_func_import)
    
    # Unregister
    bpy.utils.unregister_class(importer.RenderCubeImporter)


# For testing directly from Blender's Text editor
if __name__ == "__main__":
    register()