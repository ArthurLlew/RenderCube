# Operating system
import os
# Regular expretions
import re
# Math
import math
# Json reading
import json
# Is used, when converting json string to object
from types import SimpleNamespace
# ImportHelper is a helper class, defines filename and
# invoke() function which calls the file selector.
from bpy_extras.io_utils import ImportHelper
# Blender stuff
import bpy
from bpy.props import StringProperty, BoolProperty, CollectionProperty
from bpy.types import Operator, OperatorFileListElement

# Add-on info
bl_info = {
    "name": "RenderedCubeImport",
    "author": "Dreadoom",
    "version": (1, 0),
    "blender": (2, 80, 0),
    "description": "Imports RenderedCube data",
    "warning": "",
    "doc_url": "",
    "category": "Import-Export",
}

# Add-on utils
class RenderCubeUtils:
    # Reads file contents
    def import_data(filepath):
        # Open file
        with open(filepath, 'r', encoding='utf-8') as f:
            # Read json object and return contents
            return json.load(f)
    
    # Creates vertices and faces from imported data
    def parse_loaded_data(loaded_data):
        # Unit data
        vertices, uv, vertices_colors, faces = [], [], [], []
        
        # For guad in loaded data
        for quad in loaded_data:
            # Current quad first vertex index
            face_first_vert_index = len(vertices)
            
            # For vertex in quad
            for vertex in quad['vertices']:
                # Append vertex position
                vertices.append((vertex['z'], vertex['x'], vertex['y']))
                    
                # Append U and V coordinates for this vertex
                uv.append((vertex['u'], vertex['v']))
                
                # Append this vertex color
                vertices_colors.append(vertex['color'])
            
            # Append vertices indices of face
            faces.append(tuple(range(face_first_vert_index, len(vertices))))

        return vertices, uv, vertices_colors, faces
    
    # Convert hex substring (like 'ff') to linear channel
    def hex_substring_to_color(hex_substring):
        # Basic color from 0 to 255
        channel = int(hex_substring, base=16)
        
        # Color from 0.0 to 1.0
        schannel = channel / 255
        
        return schannel
    
    # Covert hex to rgb
    def hex_to_rgb(hex):
        # Get rid of #
        hex = hex[1:]
        
        return (RenderCubeUtils.hex_substring_to_color(hex[:2]),
                RenderCubeUtils.hex_substring_to_color(hex[2:4]),
                RenderCubeUtils.hex_substring_to_color(hex[4:6]),
                1.0)
    
    # Creates material from hex string
    def create_material(material_name):
        # Init material
        material = bpy.data.materials.new(material_name)
        
        # Set alpha settings to HASHED
        material.blend_method = 'HASHED'
        material.shadow_method = 'HASHED'
        # Hide back surfaces of faces
        material.use_backface_culling = True

        # Start using nodes
        material.use_nodes = True
        # Get Principled BSDF shader node
        principled_node = material.node_tree.nodes.get('Principled BSDF')
        
        # Clear default values
        principled_node.inputs[4].default_value = 0
        principled_node.inputs[7].default_value = 0
        principled_node.inputs[9].default_value = 0
        principled_node.inputs[13].default_value = 0
        principled_node.inputs[15].default_value = 0
        
        # Create mix color node
        mix_node = material.node_tree.nodes.new('ShaderNodeMix')
        # Set to color mixing
        mix_node.data_type = 'RGBA'
        # Move it away from Principled BSDF shader node
        mix_node.location = (-300, 200)
            
        # Set factor to 1.0
        mix_node.inputs[0].default_value = 1.0
            
        # Set color mixing to multiplication
        mix_node.blend_type = 'MULTIPLY'
            
        # Connect mix node output to base color of Principled BSDF shader node
        material.node_tree.links.new(mix_node.outputs[2], principled_node.inputs[0])
        
        # Create Color Attribute node
        vertex_color = material.node_tree.nodes.new('ShaderNodeVertexColor')
        # Move it away from Principled BSDF shader node
        vertex_color.location = (-600, 180)
        
        # Connect Color Attribute node output to 'A' input of mix node
        material.node_tree.links.new(vertex_color.outputs[0], mix_node.inputs[6])
        
        return material
    
    # Checks if material names are equal ('material' is equal to 'material.001')
    def material_names_equality(template_name, name):
        return re.fullmatch(template_name + r'(?:.\d\d\d|\Z)', name)
    
    # Creates object in scene from geomentry data
    def create_object(name, loaded_data, material_name, search_for_materials):
        # Parse loaded data to vertices, UVs, vertices_colors and faces
        vertices, uv, vertices_colors, faces = RenderCubeUtils.parse_loaded_data(loaded_data)
        
        # Add a new mesh
        mesh = bpy.data.meshes.new('mesh')
        # Add new object using that mesh
        obj = bpy.data.objects.new(name, mesh)
        
        # Add geometry to mesh
        mesh.from_pydata(vertices, [], faces)
        # Update geometry
        mesh.update(calc_edges=True)
        
        # Add UVs to mesh
        uvlayer = mesh.uv_layers.new(name='UVs')
        mesh.uv_layers.active = uvlayer
        for face in mesh.polygons:
            for vert_idx, loop_idx in zip(face.vertices, face.loop_indices):
                uvlayer.data[loop_idx].uv = (uv[vert_idx][0], 1 - uv[vert_idx][1])
        
        # Add vertex colors
        vertex_color = mesh.vertex_colors.new(name='Color')
        mesh.vertex_colors.active = vertex_color
        for face in mesh.polygons:
            for vert_idx, loop_idx in zip(face.vertices, face.loop_indices):
                vertex_color.data[loop_idx].color = RenderCubeUtils.hex_to_rgb(vertices_colors[vert_idx])
        
        # Assign materials to faces of mesh
        for face in mesh.polygons:
            # Try to find material name with may be additional number suffix in mesh materials
            for material_key in mesh.materials.keys():
                if RenderCubeUtils.material_names_equality(material_name, material_key):
                    break
            # If this material is not yet present in mesh
            else:
                # If we want to search for such material in scene
                if search_for_materials:
                    # Try to find material in scene
                    for object in bpy.context.scene.objects:
                        for material_slot in object.material_slots:
                            if material_slot.name == material_name:
                                mesh.materials.append(material_slot.material)
                                # Signal, that material was found
                                break
                        else:
                            # If no material was not found, we continue outer loop (break statement is skipped)
                            continue
                        # Signal, that material was found
                        break
                    # No such material was found
                    else:
                        # Create material and add it to mesh materials
                        mesh.materials.append(RenderCubeUtils.create_material(material_name))
                else:
                    # Create material and add it to mesh materials
                    mesh.materials.append(RenderCubeUtils.create_material(material_name))

            # Find material in mesh materials
            for material in mesh.materials:
                if RenderCubeUtils.material_names_equality(material_name, material.name):
                    # Assign material index in mesh materials to face
                    face.material_index = mesh.materials.find(material.name)
                    break
        
        # Put the object into the scene
        bpy.context.scene.collection.children['Collection'].objects.link(obj)
        

# Import operator
class ImportRenderCube(Operator, ImportHelper):
    """RenderCube data import"""
    # Important since its how bpy.ops.import_test.some_data is constructed
    bl_idname = 'rendercube_import.rendercube_data'
    bl_label = 'Import RenderedCube Data'
    
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
        description='Should importer look for already existing materials or will it create its own ones',
        default=True,
    )

    # Executes operator
    def execute(self, context):
        # For each imported file
        for file in self.files:
            # Import data
            loaded_data = RenderCubeUtils.import_data(os.path.join(self.directory, file.name))
            
            # Create object from loaded data
            RenderCubeUtils.create_object(file.name, loaded_data, file.name, self.search_for_materials)

        # Operation was successful
        return {'FINISHED'}


# Only needed if you want to add into a dynamic menu
def menu_func_import(self, context):
    self.layout.operator(ImportRenderCube.bl_idname, text='RenderedCube (.json)')

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