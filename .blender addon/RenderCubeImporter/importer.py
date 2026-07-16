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
    data_byte_format_string = '>dddffiiiifff'

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


    def parse_loaded_data(self, loaded_data):
        """Creates vertices and faces from imported data.
        """

        # Init data
        vertices, uv, color, faces, normals = [], [], [], [], []

        # Vertex counter
        i = 0
        # Per unpacked vertex data
        for x, y, z, u, v, r, g, b, a, nx, ny, nz in struct.iter_unpack(self.data_byte_format_string, loaded_data):
            # Append vertex data
            vertices.append((z + self.offset[0], x + self.offset[1], y + self.offset[2])) # Minecraft coordinate system is oriented differently
            uv.append((u, 1 - v))
            color.append((r/255, g/255, b/255, a/255))
            normals.append((nz, nx, ny)) # Minecraft coordinate system is oriented differently
            # Update counter
            i += 1
            
            # Each face has exactly 4 vertices
            if i == 4:
                faces.append(tuple(range(len(vertices) - 4, len(vertices))))
                i = 0

        return vertices, uv, color, faces, normals


    def create_material(self, material_name):
        """Creates material from hex string.
        """

        # Init material
        material = bpy.data.materials.new(material_name)
        # Hide back surfaces of faces
        material.use_backface_culling = True
        # Start using nodes
        material.use_nodes = True

        # Get Principled BSDF shader node
        principled_node = material.node_tree.nodes.get('Principled BSDF')
        # Set roughness to 0.0
        principled_node.inputs[2].default_value = 0.0
        # Set IOR to 1.0
        principled_node.inputs[3].default_value = 1.0

        # Create mix color node
        mix_node = material.node_tree.nodes.new('ShaderNodeMix')
        # Set to color mixing
        mix_node.data_type = 'RGBA'
        # Move it away from Principled BSDF shader node
        mix_node.location = mathutils.Vector((-450.0, 300.0))
        # Set factor to 1.0
        mix_node.inputs[0].default_value = 1.0
        # Set color mixing to multiplication
        mix_node.blend_type = 'MULTIPLY'

        # Connect mix node output to base color of Principled BSDF shader node
        material.node_tree.links.new(mix_node.outputs[2], principled_node.inputs[0])

        # Create 'Color Attribute' node
        vertex_color = material.node_tree.nodes.new('ShaderNodeAttribute')
        # Move it away from Principled BSDF shader node
        vertex_color.location = mathutils.Vector((-650.0, 300.0))
        # Select color attribute
        vertex_color.attribute_name = 'Color'

        # Connect output of Color Attribute node to 'A' input of mix node
        material.node_tree.links.new(vertex_color.outputs[0], mix_node.inputs[6])

        return material


    def create_object(self, object_name, loaded_data, material_name):
        """Creates object in scene from imported data.
        """

        # Parse loaded data to vertex coords, vertex UVs, vertex colors and faces
        xyz, uv, color, faces, normals = self.parse_loaded_data(loaded_data)

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

        # Init UVs
        uv_layer = mesh.uv_layers.new(name='UVs')
        mesh.uv_layers.active = uv_layer
        # Init vertex color
        vertex_color = mesh.vertex_colors.new(name='Color')
        mesh.vertex_colors.active = vertex_color
        # Fill in data
        uv_layer.data.foreach_set("uv", np.array(uv, dtype=np.float32).reshape(-1))
        vertex_color.data.foreach_set("color", np.array(color, dtype=np.float32).reshape(-1))

        # Setup empty material
        material = None
        # If we should search for material in the scene
        if self.search_for_materials:
            # Loop over all materials in a scene
            for mat in bpy.data.materials:
                # Match material by name (ommiting potential duplicates)
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