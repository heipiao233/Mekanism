package mekanism.common.network;

import java.util.UUID;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketGearStateUpdate {

    private final GearType gearType;
    private final boolean state;
    private final UUID uuid;

    //Client to server AND sort of server to client, as the server then reroutes it to the client
    public PacketGearStateUpdate(GearType gearType, UUID uuid, boolean state) {
        this.gearType = gearType;
        this.uuid = uuid;
        this.state = state;
    }

    public static void handle(PacketGearStateUpdate message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            if (message.gearType == GearType.FLAMETHROWER) {
                Mekanism.playerState.setFlamethrowerState(message.uuid, message.state, false);
            } else if (message.gearType == GearType.JETPACK) {
                Mekanism.playerState.setJetpackState(message.uuid, message.state, false);
            } else if (message.gearType == GearType.GAS_MASK) {
                Mekanism.playerState.setGasmaskState(message.uuid, message.state, false);
            }
            //If we got this packet on the server, inform all clients tracking the changed player
            if (!player.world.isRemote) {
                //Note: We just resend all the data for the updated player as the packet size is about the same
                // and this allows us to separate the packet into a server to client and client to server packet
                Mekanism.packetHandler.sendToAllTracking(new PacketPlayerData(message.uuid), player);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketGearStateUpdate pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.gearType);
        buf.writeUniqueId(pkt.uuid);
        buf.writeBoolean(pkt.state);
    }

    public static PacketGearStateUpdate decode(PacketBuffer buf) {
        return new PacketGearStateUpdate(buf.readEnumValue(GearType.class), buf.readUniqueId(), buf.readBoolean());
    }

    public enum GearType {
        FLAMETHROWER,
        JETPACK,
        GAS_MASK
    }
}