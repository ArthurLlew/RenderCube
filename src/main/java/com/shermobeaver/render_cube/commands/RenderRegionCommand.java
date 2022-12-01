package com.shermobeaver.render_cube.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.shermobeaver.render_cube.RenderCube;
import com.shermobeaver.render_cube.utils.JsonSequenceWriter;
import com.shermobeaver.render_cube.utils.RenderCubeUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;

import java.io.IOException;

public class RenderRegionCommand {
    /**
     * Is used to define command pattern
     **/
    public RenderRegionCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(RenderCube.MODID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("render")
                        .then(Commands.literal("region")
                                .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                                        .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                                                .executes((command) -> renderRegion(command.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(command, "pos1"),
                                                        BlockPosArgument.getLoadedBlockPos(command, "pos2"))))))));
    }

    /**
     * Executes command
     **/
    private int renderRegion(CommandSourceStack source, BlockPos position1, BlockPos position2){
        try(JsonSequenceWriter jsonWriter = new JsonSequenceWriter(RenderCube.MODID + "\\rendered_cube.json")){
            // Min and max coordinates over each axes
            int region_min_x = Math.min(position1.getX(), position2.getX());
            int region_max_x = Math.max(position1.getX(), position2.getX());
            int region_min_y = Math.min(position1.getY(), position2.getY());
            int region_max_y = Math.max(position1.getY(), position2.getY());
            int region_min_z = Math.min(position1.getZ(), position2.getZ());
            int region_max_z = Math.max(position1.getZ(), position2.getZ());

            // Loop over coordinates
            for(int x = region_min_x; x <= region_max_x; x++){
                for(int y = region_min_y; y <= region_max_y; y++){
                    for(int z = region_min_z; z <= region_max_z; z++){
                        // Current block position
                        BlockPos position = new BlockPos(x, y, z);

                        // Process block
                        boolean success = RenderCubeUtils.renderBlock(
                                source, jsonWriter, position, new BlockPos(
                                        position.getX() - region_min_x,
                                        position.getY() - region_min_y,
                                        position.getZ() - region_min_z));

                        // We finish with success only if RenderCubeUtils.RenderBlock(...) returned true
                        if(!success){
                            // Finish with failure
                            return -1;
                        }
                    }
                }
            }

            // Notify about success
            source.sendSuccess(new TextComponent("Operation succeeded."), true);

            // Finish with success
            return 1;
        }
        catch (IOException e){
            // Notify about exception
            source.sendFailure(new TextComponent("File error: " + e));

            // Finish with failure
            return -1;
        }
        catch(Exception e) {
            // Notify about exception
            source.sendFailure(new TextComponent("Render region: " + e));

            // Finish with failure
            return -1;
        }
    }
}
