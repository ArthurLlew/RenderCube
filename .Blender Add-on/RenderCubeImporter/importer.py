###########
# Imports #
###########

# Operating system
import os
# Blender
from bpy.props import StringProperty, BoolProperty, CollectionProperty
from bpy.types import Operator, OperatorFileListElement
from bpy_extras.io_utils import ImportHelper
# Custom lib
from . import utils

#########################
# Add-on Operator Class #
#########################
class RenderCubeImporter(Operator, ImportHelper):
    """RenderCube data import"""
    # Important since its how bpy.ops.import_test.some_data is constructed
    bl_idname = 'rendercube_import.rendercube_data'
    bl_label = 'Import RenderCube Data'
    
    # ImportHelper mixin class uses this
    filename_ext = '.json'
    
    # File explorer searsh optiond
    filter_glob: StringProperty(
        default='*.json',
        options={'HIDDEN'},
        maxlen=255,  # Max internal buffer length, longer would be clamped.
    )
    
    # Directory, containing files for import
    directory: StringProperty(subtype='DIR_PATH')
    
    # Imported files (each one contains field with name of file)
    files: CollectionProperty(
        name="BVH files",
        type=OperatorFileListElement,
        )
    
    # Import option (Does the importer search for already existing materials?)
    search_for_materials: BoolProperty(
        name='Search for existing materials',
        description='Should importer look for already existing materials or will it create new ones',
        default=True,
    )

    #-------------------#
    # Executes operator #
    #-------------------#
    def execute(self, context):
        # For each imported file
        for file in self.files:
            # Import data
            loaded_data = utils.import_data(os.path.join(self.directory, file.name))
            
            # If loaded file is not empty
            if len(loaded_data) != 0:
                # Create object from loaded data
                utils.create_object(
                    file.name.split('.', 1)[0],
                    loaded_data,
                    file.name.split('.', 1)[0] + 'Mat',
                    self.search_for_materials)

        # Operation was successful
        return {'FINISHED'}