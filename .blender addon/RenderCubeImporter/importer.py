###########
# Imports #
###########


# Operating system
import os
# Blender
import bpy
from bpy.props import StringProperty, CollectionProperty, BoolProperty, FloatVectorProperty
from bpy.types import Operator, OperatorFileListElement
from bpy_extras.io_utils import ImportHelper
import mathutils
# Bytes interpretation
import struct
# Fast arrays
import numpy as np


#########################
# Add-on Operator Class #
#########################


class RenderCubeImporter(Operator, ImportHelper):
    """RenderCube data importer.
    """

    # Layout of bytes in each data packet
    data_byte_format_string = '>dddfffffiiiifi'

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
    
    # Import option (should the importer set a single shared material for all the exported objects?)
    offset: FloatVectorProperty(
        name="Offset",
        description="Constant positional offset for the imported geometry",
        default=(0.0, 0.0, 0.0),
        subtype='TRANSLATION',
        size=3
    )


    def execute(self, context):
        """Executes operator.
        """

        # For each imported file
        for file in self.files:
            # Import data
            loaded_data = self.load_data(os.path.join(self.directory, file.name))

            # If loaded data is not empty and can be read
            if (len(loaded_data) != 0) and (len(loaded_data) % (struct.calcsize(self.data_byte_format_string) * 4) == 0):
                # Compute object name (discard file extension)
                object_name = file.name.rsplit('.', 1)[0]

                # Choose material name
                if self.unified_material == '':
                    material_name = object_name + 'Mat'
                else:
                    material_name = self.unified_material

                # Create object from loaded data
                self.create_object(object_name, loaded_data, material_name)

        # Operation was successful
        return {'FINISHED'}


    def load_data(self, filepath):
        """Reads file contents.
        """

        # Open file
        with open(filepath, mode="rb") as f:
            # Read byte stream
            return f.read()


    def create_object(self, object_name, loaded_data, material_name):
        """Creates object in scene from imported data.
        """

        # Parse loaded data
        xyz, normals, uv, color, brightness, light, faces = self.parse_loaded_data(loaded_data)

        # Add a new mesh
        mesh = bpy.data.meshes.new('mesh')
        # Add new object using that mesh
        obj = bpy.data.objects.new(object_name, mesh)

        # Add geometry to mesh
        mesh.from_pydata(xyz, [], faces)
        # Set normals
        mesh.normals_split_custom_set_from_vertices(normals)
        # Update geometry
        mesh.update(calc_edges=True)

        # Add UVs
        uv_layer = mesh.uv_layers.new(name='UVs')
        mesh.uv_layers.active = uv_layer
        uv_layer.data.foreach_set("uv", np.array(uv, dtype=np.float32).reshape(-1))
        # Add vertex color
        vertex_color = mesh.color_attributes.new(name="Color", type='FLOAT_COLOR', domain='POINT')
        mesh.attributes.active_color = vertex_color
        vertex_color.data.foreach_set("color", np.array(color, dtype=np.float32).reshape(-1))
        # Add vertex brightness
        vertex_brightness = mesh.color_attributes.new(name="Brightness", type='FLOAT_COLOR', domain='POINT')
        vertex_brightness.data.foreach_set("color", np.array(brightness, dtype=np.float32).reshape(-1))
        # Add vertex light
        vertex_light = mesh.color_attributes.new(name="BlockLight", type='FLOAT_COLOR', domain='POINT')
        vertex_light.data.foreach_set("color", np.array(light, dtype=np.float32).reshape(-1))

        # Setup empty material
        material = None
        # If we should search for material in the scene
        if self.search_for_materials:
            # Loop over all materials in a scene
            for mat in bpy.data.materials:
                # Match material by name (omitting potential duplicates)
                if material_name == mat.name.rsplit('.', 1)[0]:
                    material = mat
                    break
        # If no material was found
        if material == None:
            material = self.create_material(material_name)
        # Append material
        mesh.materials.append(material)

        # Assign material to faces of mesh
        for face in mesh.polygons:
            face.material_index = mesh.materials.find(material_name)
            break

        # Put the object into the scene inside currently selected collection
        bpy.context.view_layer.active_layer_collection.collection.objects.link(obj)


    def parse_loaded_data(self, loaded_data):
        """Parses vertex coordinates, UVs, color, brightness and block light from imported data and constructs faces.
        """

        # Init data
        xyz, normals, uv, color, brightness, light, faces = [], [], [], [], [], [], []

        # Vertex counter
        i = 0
        # Per unpacked vertex data
        for x, y, z, nx, ny, nz, u, v, r, g, b, a, br, l in struct.iter_unpack(self.data_byte_format_string, loaded_data):
            # Append vertex data
            xyz.append((z + self.offset[0], x + self.offset[1], y + self.offset[2])) # Minecraft coordinate system is oriented differently
            normals.append((nz, nx, ny)) # Minecraft coordinate system is oriented differently
            uv.append((u, 1 - v))
            color.append((r/255, g/255, b/255, a/255))
            brightness.append((br, br, br, 1))
            light.append((l/15, l/15, l/15, 1))
            # Update counter
            i += 1

            # Each face has exactly 4 vertices
            if i == 4:
                faces.append(tuple(range(len(xyz) - 4, len(xyz))))
                i = 0

        return xyz, normals, uv, color, brightness, light, faces


    def create_material(self, material_name):
        """Creates material from hex string.
        """

        # Init material
        material = bpy.data.materials.new(material_name)
        # Hide back surfaces of faces
        material.use_backface_culling = True
        # Start using nodes
        material.use_nodes = True

        ###################
        # Principled BSDF #
        ###################

        # Get Principled BSDF shader node
        principled_node = material.node_tree.nodes.get('Principled BSDF')
        # Set roughness to 0.0
        principled_node.inputs['Roughness'].default_value = 0.0
        # Set IOR to 1.0
        principled_node.inputs['IOR'].default_value = 1.0
        # Expand emission panel
        principled_node.panel_states[6].is_collapsed = False

        #############################
        # Mix Color With Brightness #
        #############################

        # Create mix color node
        mix_node_1 = material.node_tree.nodes.new('ShaderNodeMix')
        # Set to color mixing
        mix_node_1.data_type = 'RGBA'
        # Move it away from Principled BSDF shader node
        mix_node_1.location = mathutils.Vector((-550.0, 300.0))
        # Set factor to 1.0
        mix_node_1.inputs['Factor'].default_value = 1.0
        # Set color mixing to multiplication
        mix_node_1.blend_type = 'MULTIPLY'

        # Connect mix node output to base color and emission color of Principled BSDF shader node
        material.node_tree.links.new(mix_node_1.outputs['Result'], principled_node.inputs['Base Color'])
        material.node_tree.links.new(mix_node_1.outputs['Result'], principled_node.inputs['Emission Color'])

        #########################
        # Mix Texture With Tint #
        #########################

        # Create mix color node
        mix_node_2 = material.node_tree.nodes.new('ShaderNodeMix')
        # Set to color mixing
        mix_node_2.data_type = 'RGBA'
        # Move it away from Principled BSDF shader node
        mix_node_2.location = mathutils.Vector((-750.0, 100.0))
        # Set factor to 1.0
        mix_node_2.inputs['Factor'].default_value = 1.0
        # Set color mixing to multiplication
        mix_node_2.blend_type = 'MULTIPLY'

        # Connect the second mix node output to 'B' input of the first mix node
        material.node_tree.links.new(mix_node_2.outputs['Result'], mix_node_1.inputs['B'])

        ########
        # Tint #
        ########

        # Create 'Color Attribute' node
        color = material.node_tree.nodes.new('ShaderNodeAttribute')
        # Move it away from Principled BSDF shader node
        color.location = mathutils.Vector((-950.0, 100.0))
        # Select color attribute
        color.attribute_name = 'Color'

        # Connect output of Color Attribute node to 'A' input of the second mix node
        material.node_tree.links.new(color.outputs['Color'], mix_node_2.inputs['A'])

        ##############
        # Brightness #
        ##############

        # Create 'Color Attribute' node
        brightness = material.node_tree.nodes.new('ShaderNodeAttribute')
        # Move it away from Principled BSDF shader node
        brightness.location = mathutils.Vector((-750.0, 300.0))
        # Select color attribute
        brightness.attribute_name = 'Brightness'

        # Connect output of Color Attribute node to 'A' input of the first mix node
        material.node_tree.links.new(brightness.outputs['Color'], mix_node_1.inputs['A'])

        ###############
        # Block Light #
        ###############

        # Create 'Color Attribute' node
        light = material.node_tree.nodes.new('ShaderNodeAttribute')
        # Move it away from Principled BSDF shader node
        light.location = mathutils.Vector((-500.0, -150.0))
        # Select color attribute
        light.attribute_name = 'BlockLight'

        # Connect output of Block light node to the emission strength of Principled BSDF shader node
        material.node_tree.links.new(light.outputs['Color'], principled_node.inputs['Emission Strength'])

        return material