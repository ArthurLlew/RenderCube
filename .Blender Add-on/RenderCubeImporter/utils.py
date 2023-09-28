###########
# Imports #
###########

# Regular expressions
import re
# Blender
import bpy
# Bytes interpretation
import struct

####################
# RenderCube Utils #
####################


def import_data(filepath):
    '''Reads file contents.'''
    
    # Open file
    with open(filepath, mode="rb") as f:
        # Read byte stream
        return f.read()


def parse_loaded_data(loaded_data):
    '''Creates vertices and faces from imported data.'''
    
    # Unit data
    vertices, uv, vertices_colors, faces = [], [], [], []

    # For quad in loaded data
    for i in range(0, len(loaded_data) // 192):
        # For vertex in quad
        for j in range(0, 4):
            # Append vertex position
            vertex_x = struct.unpack('>d', loaded_data[i * 192:(i + 1) * 192][j * 48:(j + 1) * 48][0:8])[0]
            vertex_y = struct.unpack('>d', loaded_data[i * 192:(i + 1) * 192][j * 48:(j + 1) * 48][8:16])[0]
            vertex_z = struct.unpack('>d', loaded_data[i * 192:(i + 1) * 192][j * 48:(j + 1) * 48][16:24])[0]
            vertices.append((vertex_z, vertex_x, vertex_y))
                
            # Append U and V coordinates for this vertex
            vertex_u = struct.unpack('>f', loaded_data[i * 192: (i + 1) * 192][j * 48: (j + 1) * 48][24:28])[0]
            vertex_v = struct.unpack('>f', loaded_data[i * 192: (i + 1) * 192][j * 48: (j + 1) * 48][28:32])[0]
            uv.append((vertex_u, vertex_v))
            
            # Append this vertex color
            vertex_r = struct.unpack('>i', loaded_data[i * 192: (i + 1) * 192][j * 48: (j + 1) * 48][32:36])[0] / 255
            vertex_g = struct.unpack('>i', loaded_data[i * 192: (i + 1) * 192][j * 48: (j + 1) * 48][36:40])[0] / 255
            vertex_b = struct.unpack('>i', loaded_data[i * 192: (i + 1) * 192][j * 48: (j + 1) * 48][40:44])[0] / 255
            vertex_a = struct.unpack('>i', loaded_data[i * 192: (i + 1) * 192][j * 48: (j + 1) * 48][44:48])[0] / 255
            vertices_colors.append((vertex_r, vertex_g, vertex_b, vertex_a))
        
        # Append vertices indices of face
        faces.append(tuple(range(len(vertices) - 4, len(vertices))))

    return vertices, uv, vertices_colors, faces


def create_material(material_name):
    '''Creates material from hex string.'''
    
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


def material_names_equality(template_name, name):
    '''Checks if material names are equal ('material' is equal to 'material.001').'''
    
    return re.fullmatch(template_name + r'(?:.\d\d\d|\Z)', name)


def create_object(name, loaded_data, material_name, search_for_materials):
    '''Creates object in scene from geometry data.'''
    
    # Parse loaded data to vertices, UVs, vertices_colors and faces
    vertices, uv, vertices_colors, faces = parse_loaded_data(loaded_data)
    
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
            vertex_color.data[loop_idx].color = vertices_colors[vert_idx]
    
    # Assign materials to faces of mesh
    for face in mesh.polygons:
        # Try to find material name with may be additional number suffix in mesh materials
        for material_key in mesh.materials.keys():
            if material_names_equality(material_name, material_key):
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
                    mesh.materials.append(create_material(material_name))
            else:
                # Create material and add it to mesh materials
                mesh.materials.append(create_material(material_name))

        # Find material in mesh materials
        for material in mesh.materials:
            if material_names_equality(material_name, material.name):
                # Assign material index in mesh materials to face
                face.material_index = mesh.materials.find(material.name)
                break
    
    # Put the object into the scene
    bpy.context.scene.collection.children['Collection'].objects.link(obj)