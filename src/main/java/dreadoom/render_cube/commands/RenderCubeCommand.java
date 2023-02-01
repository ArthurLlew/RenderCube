package dreadoom.render_cube.commands;

import com.mojang.brigadier.CommandDispatcher;
import dreadoom.render_cube.RenderCube;
import dreadoom.render_cube.utils.JsonSequenceWriter;
import dreadoom.render_cube.utils.RenderCubeConstants;
import dreadoom.render_cube.utils.RenderCubeUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;

import java.io.IOException;

public class RenderCubeCommand {
    /**
     * Is used to define command pattern.
    **/
    public RenderCubeCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(RenderCube.MODID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("render")
                        .then(Commands.literal("cube")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes((command) -> renderCube(command.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(command, "pos")))))));
    }

    /**
     * Executes command.
     * @param source command executioner
     * @param position block position in world
     **/
    private int renderCube(CommandSourceStack source, BlockPos position){
        try(JsonSequenceWriter jsonWriter = new JsonSequenceWriter(
                RenderCube.MODID + "\\" + RenderCubeConstants.exportedFileName)){

            // Render cube (region of 1 block)
            RenderCubeUtils.renderRegion(
                    source,
                    jsonWriter,
                    new int[]{position.getX(), position.getY(), position.getZ(), position.getX(), position.getY(),
                            position.getZ()});

            // Notify about success
            source.sendSuccess(new TextComponent(RenderCubeConstants.successMessage), true);

            // Finish with success
            return 1;
        }
        catch (IOException e){
            // Notify about exception
            source.sendFailure(new TextComponent(RenderCubeConstants.fileExceptionMessage + e));

            // Finish with failure
            return -1;
        }
        catch(Exception e) {
            // Notify about exception
            source.sendFailure(
                    new TextComponent(new Throwable().getStackTrace()[0].getMethodName() + ": " + e));

            // Finish with failure
            return -1;
        }
    }
}