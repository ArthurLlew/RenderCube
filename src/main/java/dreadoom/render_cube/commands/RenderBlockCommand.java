package dreadoom.render_cube.commands;

import com.mojang.brigadier.CommandDispatcher;
import dreadoom.render_cube.RenderCube;
import dreadoom.render_cube.utils.JsonSequenceWriter;
import dreadoom.render_cube.utils.RenderCubeUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;

import java.io.IOException;

public class RenderBlockCommand {
    /**
     * Is used to define command pattern.
    **/
    public RenderBlockCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(RenderCube.MODID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("render")
                        .then(Commands.literal("block")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes((command) -> renderBlock(command.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(command, "pos")))))));
    }

    /**
     * Executes command.
     * @param source command executioner
     * @param position block position in world
     **/
    private int renderBlock(CommandSourceStack source, BlockPos position){
        try(JsonSequenceWriter jsonWriter = new JsonSequenceWriter(RenderCube.MODID + "\\rendered_cube.json")){
            // Process block
            boolean success = RenderCubeUtils.renderBlock(
                    source, jsonWriter, position, new BlockPos(0, 0 , 0));

            // We finish with success only if RenderCubeUtils.RenderBlock(...) returned true
            if(!success){
                // Finish with failure
                return -1;
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
            source.sendFailure(new TextComponent("Render block: " + e));

            // Finish with failure
            return -1;
        }
    }
}
