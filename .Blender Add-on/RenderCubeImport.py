# ImportHelper is a helper class, defines filename and
# invoke() function which calls the file selector.
from bpy_extras.io_utils import ImportHelper
# Blender stuff
import bpy
from bpy.props import StringProperty, BoolProperty
from bpy.types import Operator
# Json reading
import json
# Is used, when converting json string to object
from types import SimpleNamespace
# Math
import math
# Regular expretions
import re

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
    
    # Read quads from rendered model
    def import_quads(rendered_model, block_x, block_y, block_z, vertices, uv, faces, faces_color):
        # For block guads
        for quad in rendered_model['quads']:
            # Current quad first vertex index
            face_first_vert_index = len(vertices)

            # For vertex in quad
            for vertex in quad['vertices']:
                # Append vertex position which is equal to its position + block position
                vertices.append((block_x + vertex['z'], block_y + vertex['x'], block_z + vertex['y']))
                    
                # Append U and V coordinates for this vertex
                uv.append((vertex['u'], vertex['v']))
            
            # Append vertices indices of face
            faces.append(tuple(range(face_first_vert_index, len(vertices))))
            
            # Append face color
            faces_color.append(quad['quadColor'])
    
    # Creates vertices and faces from imported data
    def import_geometry(data):
        
        vertices, uv, faces, faces_color = [[], [], []], [[], [], []], [[], [], []], [[], [], []]
        
        # Iterating through the blocks
        for block_data in data:
            RenderCubeUtils.import_quads(block_data['renderedBlock'],
                                         block_data['z'],
                                         block_data['x'],
                                         block_data['y'],
                                         vertices[0],
                                         uv[0],
                                         faces[0],
                                         faces_color[0])
            RenderCubeUtils.import_quads(block_data['renderedEntity'],
                                         block_data['z'],
                                         block_data['x'],
                                         block_data['y'],
                                         vertices[1],
                                         uv[1],
                                         faces[1],
                                         faces_color[1])
            RenderCubeUtils.import_quads(block_data['renderedLiquid'],
                                         block_data['z'],
                                         block_data['x'],
                                         block_data['y'],
                                         vertices[2],
                                         uv[2],
                                         faces[2],
                                         faces_color[2])
        return vertices, uv, faces, faces_color
    
    # Convert hex substring (like 'ff') to linear channel
    def hex_substring_to_color(hex_substring):
        # Basic color from 0 to 255
        channel = int(hex_substring, base=16)
        
        # Color from 0.0 to 1.0
        schannel = channel / 255
        
        # Make it linear
        if schannel <= 0.04045:
            return schannel / 12.92
        else:
            return math.pow((schannel + 0.055) / 1.055, 2.4)
    
    # Covert hex to rgb
    def hex_to_rgb(hex):
        # Get rid of #
        hex = hex[1:]
        
        return (RenderCubeUtils.hex_substring_to_color(hex[:2]),
                RenderCubeUtils.hex_substring_to_color(hex[2:4]),
                RenderCubeUtils.hex_substring_to_color(hex[4:6]),
                1.0)
    
    # Creates material from hex string
    def create_material(material_name, hex):
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
        
        # If HEX color is not pure white
        if hex != '#ffffff':
            # Create mix color node
            mix_node = material.node_tree.nodes.new('ShaderNodeMix')
            # Set to color mixing
            mix_node.data_type = 'RGBA'
            # Move it away from Principled BSDF shader node
            mix_node.location = (-300, 200)
            
            # Set factor to 1.0
            mix_node.inputs[0].default_value = 1.0
            
            # Set 'A' input to hex color value
            mix_node.inputs[6].default_value = RenderCubeUtils.hex_to_rgb(hex)
            
            # Set color mixing to multiplication
            mix_node.blend_type = 'MULTIPLY'
            
            # Connect mix node output to base color of Principled BSDF shader node
            material.node_tree.links.new(mix_node.outputs[2], principled_node.inputs[0])
        
        return material
    
    # Checks if material names are equal ('material' is equal to 'material.001')
    def material_names_equality(template_name, name):
        return re.fullmatch(template_name + r'(?:.\d\d\d|\Z)', name)
    
    # Creates object in scene from geomentry data
    def create_object(name, vertices, uv, faces, faces_color, search_for_materials):
        # Add a new mesh
        mesh = bpy.data.meshes.new('mesh')
        # Add new object using that mesh
        obj = bpy.data.objects.new(name, mesh)
        
        # Add geometry to mesh
        mesh.from_pydata(vertices, [], faces)
        # Update geometry
        mesh.update(calc_edges=True)
        
        # Add UVs to mesh
        uvlayer = mesh.uv_layers.new()
        mesh.uv_layers.active = uvlayer
        for face in mesh.polygons:
            for vert_idx, loop_idx in zip(face.vertices, face.loop_indices):
                uvlayer.data[loop_idx].uv = (uv[vert_idx][0], 1 - uv[vert_idx][1])
        
        # Assign materials to faces of mesh
        for face_index, face in enumerate(mesh.polygons):
            # Name of the material
            material_name = f'minecraft_{faces_color[face_index]}'

            # Try to find this material name with may be additional number suffix after
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
                        mesh.materials.append(RenderCubeUtils.create_material(material_name, faces_color[face_index]))
                else:
                    # Create material and add it to mesh materials
                    mesh.materials.append(RenderCubeUtils.create_material(material_name, faces_color[face_index]))

            # Find material
            for material in mesh.materials:
                if RenderCubeUtils.material_names_equality(material_name, material.name):
                    # Assign material index in mesh materials to face
                    face.material_index = mesh.materials.find(material.name)
                    break
        
        # Test
        vertex_color = mesh.vertex_colors.new()
        mesh.vertex_colors.active = vertex_color
        for face in mesh.polygons:
            for vert_idx, loop_idx in zip(face.vertices, face.loop_indices):
                vertex_color.data[loop_idx].color = (0.7, 0.7, 0.5, 1)
        
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
    
    # Import option. Does the importer search for already existing materials
    search_for_materials: BoolProperty(
        name="Search for existing materials",
        description="Should importer look for already existing materials or will it create its own ones",
        default=True,
    )
    
    # Executes operator
    def execute(self, context):
        # Import rendercube data
        data = RenderCubeUtils.import_rendercube(context, self.filepath)
        # Convert it to another form
        vertices, uv, faces, faces_color = RenderCubeUtils.import_geometry(data)
        
        # Create blocks object
        if len(vertices[0]) != 0:
            RenderCubeUtils.create_object("RenderedBlocks",
                                          vertices[0],
                                          uv[0],
                                          faces[0],
                                          faces_color[0],
                                          self.search_for_materials)
        # Create entities object
        if len(vertices[1]) != 0:
            RenderCubeUtils.create_object("RenderedEntities",
                                          vertices[1],
                                          uv[1],
                                          faces[1],
                                          faces_color[1],
                                          self.search_for_materials)
        # Create liquids object
        if len(vertices[2]) != 0:
            RenderCubeUtils.create_object("RenderedLiquids",
                                          vertices[2],
                                          uv[2],
                                          faces[2],
                                          faces_color[2],
                                          self.search_for_materials)

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