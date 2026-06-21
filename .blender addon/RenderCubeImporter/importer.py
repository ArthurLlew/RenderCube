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
    """RenderCube data importer.
    """

    # Important for registering
    bl_idname = 'rendercube_import.rendercube_data'
    bl_label = 'Import RenderCube Data'
    
    # ImportHelper mixin class uses this
    filename_ext = '.rcube'
    
    # File explorer search options
    filter_glob: StringProperty(
        default='*.rcube',
        options={'HIDDEN'},
        maxlen=384,  # Max internal buffer length, longer would be clamped.
        )
    
    # Directory, containing files for import
    directory: StringProperty(subtype='DIR_PATH')
    
    # Imported files (each one contains field with name of file)
    files: CollectionProperty(
        name="BVH files",
        type=OperatorFileListElement,
        )
    
    # Import option (should the importer search for already existing materials?)
    search_for_materials: BoolProperty(
        name='Search for existing materials',
        description='Should importer look for already existing materials or will it create new ones',
        default=True,
        )

    # Import option (should the importer set a single shared material for all the exported objects?)
    unified_material: StringProperty(
        name='Unified material name',
        description='If not empty, all imported objects will use shared material with that name',
        default='',
        )

    def execute(self, context):
        """Executes operator.
        """
        
        # For each imported file
        for file in self.files:
            # Import data
            loaded_data = utils.import_data(os.path.join(self.directory, file.name))
            
            # If loaded file is not empty
            if len(loaded_data) != 0 and len(loaded_data) % 192 == 0:
                # Compute object name (discard file extension)
                object_name = file.name.rsplit('.', 1)[0]

                # Choose material name
                if self.unified_material == '':
                    material_name = object_name + 'Mat'
                else:
                    material_name = self.unified_material

                # Create object from loaded data
                utils.create_object(
                    file.name.rsplit('.', 1)[0],
                    loaded_data,
                    material_name,
                    self.search_for_materials)

        # Operation was successful
        return {'FINISHED'}
