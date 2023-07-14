package dreadoom.render_cube.commands;

import com.mojang.brigadier.CommandDispatcher;
import dreadoom.render_cube.RenderCube;
import dreadoom.render_cube.utils.FileWriters;
import dreadoom.render_cube.utils.RenderCubeUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;

public class RenderRegionCommand {
    /**
     * Is used to define command pattern.
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
     * Executes command.
     * @param source command executioner
     * @param position1 first region corner position in world
     * @param position2 second region corner position in world
     **/
    private int renderRegion(CommandSourceStack source, BlockPos position1, BlockPos position2){
        try (FileWriters fileWriters = new FileWriters()){
            // Min and max coordinates over each axes
            int region_min_x = Math.min(position1.getX(), position2.getX());
            int region_max_x = Math.max(position1.getX(), position2.getX());
            int region_min_y = Math.min(position1.getY(), position2.getY());
            int region_max_y = Math.max(position1.getY(), position2.getY());
            int region_min_z = Math.min(position1.getZ(), position2.getZ());
            int region_max_z = Math.max(position1.getZ(), position2.getZ());

            // Region size by X or Z can't be > 450
            if (region_max_x - region_min_x > 450) {
                throw new IllegalArgumentException("Region size by X axis can't be > 450");
            }
            if (region_max_z - region_min_z > 450) {
                throw new IllegalArgumentException("Region size by Z axis can't be > 450");
            }

            // Render region
            RenderCubeUtils.renderRegion(
                    source,
                    fileWriters,
                    new int[]{region_min_x, region_min_y, region_min_z, region_max_x, region_max_y, region_max_z});

            // Notify about success
            source.sendSuccess(new TextComponent("Operation succeeded."), true);

            // Finish with success
            return 1;
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
