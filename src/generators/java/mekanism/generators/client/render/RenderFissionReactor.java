package mekanism.generators.client.render;

import javax.annotation.ParametersAreNonnullByDefault;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mekanism.api.Coord4D;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.GasRenderData;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.fission.FissionReactorUpdateProtocol.FormedAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.BlockPos;

@ParametersAreNonnullByDefault
public class RenderFissionReactor extends MekanismTileEntityRenderer<TileEntityFissionReactorCasing> {

    private static Model3D glowModel;

    public RenderFissionReactor(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityFissionReactorCasing tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        if (tile.isRendering && tile.getMultiblock().isFormed() && tile.getMultiblock().renderLocation != null) {
            BlockPos pos = tile.getPos();
            IVertexBuilder buffer = renderer.getBuffer(MekanismRenderType.resizableCuboid());
            if (tile.getMultiblock().isBurning()) {
                if (glowModel == null) {
                    glowModel = new Model3D();
                    glowModel.minX = 0.1; glowModel.minY = 0.01; glowModel.minZ = 0.1;
                    glowModel.maxX = 0.9; glowModel.maxY = 0.99; glowModel.maxZ = 0.9;
                    glowModel.setTexture(MekanismRenderer.whiteIcon);
                }
                for (FormedAssembly assembly : tile.getMultiblock().assemblies) {
                    matrix.push();
                    matrix.translate(assembly.getPos().getX() - pos.getX(), assembly.getPos().getY() - pos.getY(), assembly.getPos().getZ() - pos.getZ());
                    matrix.scale(1, assembly.getHeight(), 1);
                    int argb = MekanismRenderer.getColorARGB(0.466F, 0.882F, 0.929F, 0.6F);
                    MekanismRenderer.renderObject(glowModel, matrix, buffer, argb, MekanismRenderer.FULL_LIGHT);
                    matrix.pop();
                }
            }
            if (!tile.getMultiblock().fluidCoolantTank.isEmpty()) {
                FluidRenderData data = new FluidRenderData();
                data.height = tile.getMultiblock().height - 2;
                if (data.height >= 1) {
                    data.location = new Coord4D(tile.getMultiblock().renderLocation, tile.getWorld());
                    data.length = tile.getMultiblock().length;
                    data.width = tile.getMultiblock().width;
                    data.fluidType = tile.getMultiblock().fluidCoolantTank.getFluid();
                    int glow = data.calculateGlowLight(light);
                    matrix.push();
                    matrix.translate(data.location.x - pos.getX(), data.location.y - pos.getY(), data.location.z - pos.getZ());
                    MekanismRenderer.renderObject(ModelRenderer.getModel(data, tile.getMultiblock().prevCoolantScale), matrix, buffer, data.getColorARGB(tile.getMultiblock().prevCoolantScale), glow);
                    matrix.pop();
                    MekanismRenderer.renderValves(matrix, buffer, tile.getMultiblock().valves, data, pos, glow);
                }
            }
            if (!tile.getMultiblock().heatedCoolantTank.isEmpty()) {
                GasRenderData data = new GasRenderData();
                data.height = tile.getMultiblock().height - 2;
                if (data.height >= 1) {
                    data.location = new Coord4D(tile.getMultiblock().renderLocation, tile.getWorld());
                    data.length = tile.getMultiblock().length;
                    data.width = tile.getMultiblock().width;
                    data.gasType = tile.getMultiblock().heatedCoolantTank.getStack();
                    matrix.push();
                    matrix.scale(0.998F, 0.998F, 0.998F);
                    matrix.translate(data.location.x - pos.getX() + 0.001, data.location.y - pos.getY() + 0.001, data.location.z - pos.getZ() + 0.001);
                    Model3D gasModel = ModelRenderer.getModel(data, 1);
                    MekanismRenderer.renderObject(gasModel, matrix, buffer, data.getColorARGB(tile.getMultiblock().prevHeatedCoolantScale), data.calculateGlowLight(light));
                    matrix.pop();
                }
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.FISSION_REACTOR;
    }

    @Override
    public boolean isGlobalRenderer(TileEntityFissionReactorCasing tile) {
        return tile.isRendering && tile.getMultiblock().isFormed() && tile.getMultiblock().renderLocation != null;
    }
}