package com.darkmattrmaestro.cosmic_tools.items;

import com.badlogic.gdx.Input;
import com.darkmattrmaestro.cosmic_tools.Constants;
import com.darkmattrmaestro.cosmic_tools.packets.PasteBlocksPacket;
import com.darkmattrmaestro.cosmic_tools.utils.*;
import finalforeach.cosmicreach.audio.SoundManager;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.networking.client.ClientNetworkManager;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientSpatula extends FunctionalSpatula {

    public ClientSpatula(String itemID) {
        super(itemID);
    }

    public Hallucination getHallucination(Player player){
        this.blockAxis = BlockSelectionUtil.getBlockSideLookingAtFar(this.reachDist);
        if (this.blockAxis == null) return null;

        return this.getHallucination(player, this.blockAxis);
    }

    public static boolean onMousePressed(int button) {
        if (GameState.currentGameState.getClass() != InGame.class) { return false; }

        ItemStack selected = UI.hotbar.getSelectedItemStack();
        if(selected == null || !Identifier.of(Constants.MOD_ID, "spatula").toString().equals(selected.getItem().getID())) {
            return false;
        }
        ClientSpatula clientSpatula = new ClientSpatula(selected.getItem().getID());

        if(!BlockSelectionUtil.doesCollideFar(clientSpatula.reachDist)) { return false; }

        if(button == Input.Buttons.RIGHT) {
            // Right Click => paste
            Constants.LOGGER.warn("CopyBlocks: {}", clientSpatula.getHallucination(InGame.getLocalPlayer()).blocks); // Update copyBlocks
            Constants.LOGGER.warn("playerHasEnoughItems {}", clientSpatula.playerHasEnoughItems(InGame.getLocalPlayer()));
            if (clientSpatula.playerHasEnoughItems(InGame.getLocalPlayer())) {
                if (!InGame.getLocalPlayer().gamemode.hasInfiniteItems()) {
                    AtomicInteger consumedItems = new AtomicInteger(clientSpatula.copyBlocks.blocks.size());
                    InGame.getLocalPlayer().inventory.forEachSlot((itemSlot -> {
                        if (itemSlot.getItem() == null) {
                            return;
                        }

                        if (itemSlot.getItem().getID().equals(initialBlockID)) {
                            int minConsumed = Math.min(consumedItems.get(), itemSlot.getItemAmount());
                            itemSlot.addAmount(-minConsumed);
                            consumedItems.addAndGet(-minConsumed);
                        }
                    }));
                }

                List<Chunk> chunksToUpdate = new ArrayList<>();
                String triggerName = "onPlace";

                Zone zone = InGame.getLocalPlayer().getZone();
                for (BlockPosition blockPos : clientSpatula.copyBlocks.blocks) {
                    BlockEventTrigger[] triggers = blockPos.getBlockState().getTrigger(triggerName);

                    Vector3Int hallucinatedPos = (new Vector3Int(blockPos.getGlobalX(), blockPos.getGlobalY(), blockPos.getGlobalZ())).add(clientSpatula.blockAxis.axis);

                    zone.setBlockState(blockPos.getBlockState(), hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
                    Chunk c = zone.getChunkAtBlock(hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
                    if (!chunksToUpdate.contains(c)) {
                        chunksToUpdate.add(c);
                    }

//                    if (triggers != null) {
//                        if (GameSingletons.isHost) {
//                            BlockEventArgs args = new BlockEventArgs();
//                            args.srcPlayer = InGame.getLocalPlayer();
//                            args.srcBlockState = blockPos.getBlockState();
//                            args.zone = zone;
//                            args.blockPos = blockPos;
//    //                        for (GameEventTrigger trigger: triggers) {
//    //                            Constants.LOGGER.warn("Triggers: {}", trigger.getAction());
//    //                        }
//                            args.run(triggers);
//                            args.runScheduledTriggers();
//                        }
//                    }

                }

                if (ClientNetworkManager.isConnected()) {
                    List<BlockPosition> offsetBlocks = clientSpatula.copyBlocks.blocks.stream().map((BlockPosition blockPos) -> {
                        return blockPos.getOffsetBlockPos(clientSpatula.blockAxis.axis.x, clientSpatula.blockAxis.axis.y, clientSpatula.blockAxis.axis.z);
                    }).toList();
                    ClientNetworkManager.sendAsClient(new PasteBlocksPacket(BlockMappings.ofZonePositions(zone, offsetBlocks)));
                }

                ChunkUtils.remesh(chunksToUpdate, zone);

                SoundManager.INSTANCE.playSound("cosmic_tools:sounds/items/spatula-place.ogg", 1, 1, 0);
            }
        }
        return true;
    }
}
