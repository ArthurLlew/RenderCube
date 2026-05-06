###########
# Imports #
###########


# Blender
import bpy
import mathutils
# Bytes interpretation
import struct
# Fast arrays
import numpy as np


####################
# RenderCube Utils #
####################


def import_data(filepath):
    """Reads file contents.
    """

    # Open file
    with open(filepath, mode="rb") as f:
        # Read byte stream
        return f.read()


def parse_loaded_data(loaded_data):
    """Creates vertices and faces from imported data.
    """

    # Unit data
    vertices, uv, color, faces = [], [], [], []

    # Vertex counter
    i = 0
    for x, y, z, u, v, r, g, b, a in struct.iter_unpack('>dddffiiii', loaded_data):
        # Append vertex data
        vertices.append((z, x, y))
        uv.append((u, 1 - v))
        color.append((r/255, g/255, b/255, a/255))
        # Update counter
        i += 1
        
        # Each face has exactly 4 vertices
        if i == 4:
            faces.append(tuple(range(len(vertices) - 4, len(vertices))))
            i = 0

    return vertices, uv, color, faces


def create_material(material_name):
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


def create_object(name, loaded_data, material_name, search_for_materials):
    """Creates object in scene from imported data.
    """

    # Parse loaded data to vertex coords, vertex UVs, vertex colors and faces
    xyz, uv, color, faces = parse_loaded_data(loaded_data)

    # Add a new mesh
    mesh = bpy.data.meshes.new('mesh')
    # Add new object using that mesh
    obj = bpy.data.objects.new(name, mesh)

    # Add geometry to mesh
    mesh.from_pydata(xyz, [], faces)
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
    # If we should search for material in other scene objects
    if search_for_materials:
        # Loop over all objects and their materials
        for object in bpy.context.scene.objects:
            for material_slot in object.material_slots:
                # Match material by name
                if material_name == material_slot.name:
                    material = material_slot.material
                    break
            else:
                # If no material was found skips next break statement
                continue
            break
    # If no material was found
    if material == None:
        material = create_material(material_name)
    # Append material
    mesh.materials.append(material)

    # Assign material to faces of mesh
    for face in mesh.polygons:
        face.material_index = mesh.materials.find(material_name)
        break

    # Put the object into the scene
    bpy.context.scene.collection.children['Collection'].objects.link(obj)