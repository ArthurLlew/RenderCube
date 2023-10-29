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
    "author": "Raven",
    "version": (0, 1),
    "blender": (3, 6, 0),
    "description": "Imports RenderCube data",
    "warning": "",
    "doc_url": "",
    "category": "Import-Export",
}


#######################
# Add-on Registration #
#######################


# Only needed if you want to add into a dynamic menu
def menu_func_import(self, context):
    self.layout.operator(importer.RenderCubeImporter.bl_idname, text='RenderCube (.rcube)')


# Register
def register():
    # Register
    bpy.utils.register_class(importer.RenderCubeImporter)
    
    # Add to the "file selector" menu (required to use F3 search for quick access)
    bpy.types.TOPBAR_MT_file_import.append(menu_func_import)


def unregister():
    # Remove from "file selector" menu
    bpy.types.TOPBAR_MT_file_import.remove(menu_func_import)
    
    # Unregister
    bpy.utils.unregister_class(importer.RenderCubeImporter)


# This allows you to run the script directly from Blender's Text editor
# to test the add-on without having to install it.
if __name__ == "__main__":
    register()