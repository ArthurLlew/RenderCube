# ImportHelper is a helper class, defines filename and
# invoke() function which calls the file selector.
from bpy_extras.io_utils import ImportHelper
# Blender stuff
import bpy
from bpy.props import StringProperty, BoolProperty, EnumProperty
from bpy.types import Operator
import bmesh
# Json reading
import json
# Is used, when converting json string to object
from types import SimpleNamespace
# Math
import math

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
    # Clips float values to 0.001 accuracy
    def clip_float(value):
        # Divide float into numbers before and after decimal point
        after, before = math.modf(value)
        
        # If encountered 0 after decimal point do not clip
        if abs(after) == 0:
            return value
        # If we have number less than 0.001 after decimal point
        elif abs(after) < 0.001:
            # Clip it to 0.001
            return before + math.copysign(0.001, after)
        # If we have number more than 0.999 after decimal point
        elif abs(after) > 0.999:
            # Clip it to 0.999
            return before + math.copysign(0.999, after)
        return value
    
    # Reads file contents
    def import_rendercube(context, filepath):
        # Open file
        with open(filepath, 'r', encoding='utf-8') as f:
            # Read json object
            data = json.load(f)

        # Notify about success
        return data
    
    # Read quads from rendered model
    def import_quads(rendered_model, block_x, block_y, block_z, vertices, uv, faces):
        # For block guads
        for quad in rendered_model['quads']:
            # Current quad first vertex index
            face_first_vert_index = len(vertices)

            # For vertex in quad
            for vertex in quad['vertices']:
                # Vertex position = its position + block position
                vertices.append((RenderCubeUtils.clip_float(block_x + vertex['z']),
                                 RenderCubeUtils.clip_float(block_y + vertex['x']),
                                 RenderCubeUtils.clip_float(block_z + vertex['y'])))
                    
                # U and V coordinates for this vertex
                uv.append((vertex['u'], vertex['v']))
            
            # Append vertices indices of face
            faces.append(tuple(range(face_first_vert_index, len(vertices))))
    
    # Creates vertices and faces from imported data
    def import_geometry(data):
        
        vertices, uv, faces = [[], [], []], [[], [], []], [[], [], []]
        
        # Iterating through the blocks
        for block_data in data:
            RenderCubeUtils.import_quads(block_data['renderedBlock'],
                                         block_data['z'],
                                         block_data['x'],
                                         block_data['y'],
                                         vertices[0],
                                         uv[0],
                                         faces[0])
            RenderCubeUtils.import_quads(block_data['renderedEntity'],
                                         block_data['z'],
                                         block_data['x'],
                                         block_data['y'],
                                         vertices[1],
                                         uv[1],
                                         faces[1])
            RenderCubeUtils.import_quads(block_data['renderedLiquid'],
                                         block_data['z'],
                                         block_data['x'],
                                         block_data['y'],
                                         vertices[2],
                                         uv[2],
                                         faces[2])
        return vertices, uv, faces
    
    # Creates object in scene from geomentry data
    def create_object(name, vertices, uv, faces):
        # Add a new mesh
        mesh = bpy.data.meshes.new('mesh')
        # Add a new object using the mesh
        obj = bpy.data.objects.new(name, mesh)
        
        # Add geometry to mesh
        mesh.from_pydata(vertices, [], faces)
        # Update geometry
        mesh.update(calc_edges=True)
        
        # Add UVs
        uvlayer = mesh.uv_layers.new()
        mesh.uv_layers.active = uvlayer
        for face in mesh.polygons:
            for vert_idx, loop_idx in zip(face.vertices, face.loop_indices):
                uvlayer.data[loop_idx].uv = (uv[vert_idx][0], 1 - uv[vert_idx][1])
        
        # Put the object into the scene
        bpy.context.scene.collection.children['Collection'].objects.link(obj)
        

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
        vertices, uv, faces = RenderCubeUtils.import_geometry(data)
        
        # Create blocks object
        if len(vertices[0]) != 0:
            RenderCubeUtils.create_object("RenderedBlocks", vertices[0], uv[0], faces[0])
        # Create entities object
        if len(vertices[1]) != 0:
            RenderCubeUtils.create_object("RenderedEntities", vertices[1], uv[1], faces[1])
        # Create liquids object
        if len(vertices[2]) != 0:
            RenderCubeUtils.create_object("RenderedLiquids", vertices[2], uv[2], faces[2])

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