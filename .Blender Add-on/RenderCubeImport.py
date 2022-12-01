# Imports
import bpy
# ImportHelper is a helper class, defines filename and
# invoke() function which calls the file selector.
from bpy_extras.io_utils import ImportHelper
from bpy.props import StringProperty, BoolProperty, EnumProperty
from bpy.types import Operator
# Json reading
import json
# Is used, when converting json string to object
from types import SimpleNamespace

import bmesh

# Add-on info
bl_info = {
    "name": "RenderCubeImport",
    "author": "Serius",
    "version": (1, 0),
    "blender": (2, 80, 0),
    "description": "Imports RenderCube data",
    "warning": "",
    "doc_url": "",
    "category": "Import-Export",
}

# Add-on utils
class RenderCubeUtils:
    # Reads file contents
    def import_rendercube(context, filepath):
        # Open file
        with open(filepath, 'r', encoding='utf-8') as f:
            # Read json object
            data = json.load(f)

        # Notify about success
        return data
    
    # Creates vertices and faces from imported data
    def import_geometry(data):
        
        vertices, uv, faces = [], [], []
        
        print(data)
        
        # Iterating through the blocks
        for block_data in data:
            # Get block coordinates
            block_x = block_data['z']
            block_y = block_data['x']
            block_z = block_data['y']

            # For block guads
            for quad in block_data['quads']:
                # Current quad first vertex index
                face_first_vert_index = len(vertices)
                
                print(quad)
                
                # For vertex in quad
                for vertex in quad['vertices']:
                    # Vertex position = its position + block position
                    vertices.append((block_x + vertex['z'], block_y + vertex['x'], block_z + vertex['y']))
                    
                    # U and V coordinates for this vertex
                    uv.append((vertex['u'], vertex['v']))
                
                faces.append(tuple(range(face_first_vert_index, len(vertices))))
                
        return vertices, uv, faces

# Import operator
class ImportRenderCube(Operator, ImportHelper):
    """RenderCube data import"""
    # important since its how bpy.ops.import_test.some_data is constructed
    bl_idname = "rendercube_import.rendercube_data"
    bl_label = "Import RenderCube Data"
    
    # ImportHelper mixin class uses this
    filename_ext = ".json"
    
    # File explorer searsh optiond
    filter_glob: StringProperty(
        default="*.json",
        options={'HIDDEN'},
        maxlen=255,  # Max internal buffer length, longer would be clamped.
    )
    
    # Executes operator
    def execute(self, context):
        # Import rendercube data
        data = RenderCubeUtils.import_rendercube(context, self.filepath)
        # Import geometry from data
        verts, uv, faces = RenderCubeUtils.import_geometry(data)
        
        # Add a new mesh
        mesh = bpy.data.meshes.new('mesh')
        # Add a new object using the mesh
        obj = bpy.data.objects.new('RenderedCube', mesh)
        
        # Add geometry to mesh
        mesh.from_pydata(verts, [], faces)
        # Update geometry
        mesh.update(calc_edges=True)
        
        #me = obj.data
        uvlayer = mesh.uv_layers.new()

        mesh.uv_layers.active = uvlayer
        for face in mesh.polygons:
            for vert_idx, loop_idx in zip(face.vertices, face.loop_indices):
                uvlayer.data[loop_idx].uv = (uv[vert_idx][0], 1 - uv[vert_idx][1])
        
        # Put the object into the scene
        bpy.context.scene.collection.children['Collection'].objects.link(obj)
        # Set as the active object in the scene
        bpy.context.view_layer.objects.active = obj
        # Select object
        obj.select_set(True)
        
        # Operation was successful
        return {'FINISHED'}


# Only needed if you want to add into a dynamic menu
def menu_func_import(self, context):
    self.layout.operator(ImportRenderCube.bl_idname, text="RenderCube (.json)")

# Register
def register():
    # Register
    bpy.utils.register_class(ImportRenderCube)
    
    # Add to the "file selector" menu (required to use F3 search for quick access)
    bpy.types.TOPBAR_MT_file_import.append(menu_func_import)


def unregister():
    # Remove from "file selector" menu
    bpy.types.TOPBAR_MT_file_import.remove(menu_func_import)
    
    # Unregister
    bpy.utils.unregister_class(ImportRenderCube)

# This allows you to run the script directly from Blender's Text editor
# to test the add-on without having to install it.
if __name__ == "__main__":
    register()