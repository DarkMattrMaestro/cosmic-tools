//package com.darkmattrmaestro.cosmic_tools.items;
//
//import com.darkmattrmaestro.cosmic_tools.utils.*;
//import finalforeach.cosmicreach.blocks.BlockPosition;
//import finalforeach.cosmicreach.entities.player.Player;
//
//import static java.lang.Math.abs;
//
//public class FunctionalSelectionWand {
//    float reachDist = 12.0f;
//
//    public FunctionalSelectionWand(String itemID) {
//        super();
//
//        switch (itemID) {
//            case "selection_wand": {
//                this.reachDist = 20.0f;
//                break;
//            }
//        }
//    }
//
//    private static BlockPosition firstPos = null;
//    private static BlockPosition secondPos = null;
//
//    private static BlockPosition pasteStartPos = null;
//
//    private static boolean invertX = false;
//    private static boolean invertY = false;
//    private static boolean invertZ = false;
//
////    public BlockAxis blockAxis = null;
////    public Selection selection = null;
////    public String initialBlockID = "";
//
////    public Vector3Int firstPos = null;
////    public Vector3Int secondPos = null;
//
//    public static Selection getSelection(){
//        if(firstPos == null || secondPos == null) return null;
//        return Selection.of(firstPos, secondPos);
//    }
//
//    public static Selection getPasteSelection(){
//        if(pasteStartPos == null || firstPos == null || secondPos == null) return null;
//        return Selection.of(
//                pasteStartPos.getGlobalX(),
//                pasteStartPos.getGlobalY(),
//                pasteStartPos.getGlobalZ(),
//                pasteStartPos.getGlobalX() + abs(firstPos.getGlobalX() - secondPos.getGlobalX()),
//                pasteStartPos.getGlobalY() + abs(firstPos.getGlobalY() - secondPos.getGlobalY()),
//                pasteStartPos.getGlobalZ() + abs(firstPos.getGlobalZ() - secondPos.getGlobalZ())
//        );
//    }
//
////    /**
////     * Check that the player has enough of the correct item to paste all the hallucinated blocks.
////     *
////     * @return <code>true</code> if the player has enough blocks, else <code>false</code>
////     */
////    public boolean playerHasEnoughItems(Player player) {
////        AtomicInteger availableItems = new AtomicInteger();
////        player.inventory.forEachSlot(((ItemSlot itemSlot) -> {
////            if (itemSlot.getItem() == null) { return; }
////
////            if (ItemBlockComparison.compareItemBlockID(itemSlot.getItem(), this.initialBlockID)) {
////                availableItems.addAndGet(itemSlot.getItemAmount());
////            }
////        }));
////
////        return this.copyBlocks.blocks.size() <= availableItems.get() || player.gamemode.hasInfiniteItems();
////    }
//
////    /**
////     * <p>
////     * Return the hallucination formed from the largest semi-contiguous set of blocks that are similar to the player's
////     * actively selected block.
////     * </p>
////     * <p>
////     * The plane along which to expand is determined according to the block face with which the ray from the player
////     * collided. The targeted block is set as the minimum and maximum block, then the minimum and maximum are
////     * iteratively decreased and increased, respectively. Each block in the row that might be added (plus one block on
////     * either end to allow diagonal expansion) is checked for valid placement conditions and, if at least one block is
////     * valid, the respective extremum is expanded.
////     * </p>
////     *
////     * @return the hallucination of the blocks to be copied
////     */
////    public Hallucination getHallucination(Player player, BlockAxis blockAxis){
////        this.blockAxis = blockAxis;
////
////        this.initialBlockID = blockAxis.pos.getBlockState().getBlockId();
////
////        this.copyBlocks = new HallucinatedSelection();
////
////        // Flood Fill
////        Stack<BlockPosition> stack = new Stack<BlockPosition>();
////        stack.push(blockAxis.pos);
////        while (!stack.isEmpty()) {
////            BlockPosition blockPos = stack.pop();
////
////            if (// Ensure that the block is within range
////                    abs(blockAxis.pos.getGlobalX() - blockPos.getGlobalX()) > this.maxExpansion
////                    || abs(blockAxis.pos.getGlobalY() - blockPos.getGlobalY()) > this.maxExpansion
////                    || abs(blockAxis.pos.getGlobalZ() - blockPos.getGlobalZ()) > this.maxExpansion
////            ) { continue; }
////
////            if (this.copyBlocks.blocks.contains(blockPos) || !canPlace(player, blockPos)) {
////                continue;
////            }
////
////            this.copyBlocks.blocks.add(blockPos);
////
////            for (Offset offset: Offset.CoplanarWith(Offset.AXIAL, this.blockAxis.axis)) {
////                stack.push(BlockPosition.ofGlobal(player.getZone(), blockPos.getGlobalX() + offset.getXOffset(), blockPos.getGlobalY() + offset.getYOffset(), blockPos.getGlobalZ() + offset.getZOffset()));
////            }
////        }
////
////        return copyBlocks;
////    }
//
//    public boolean canPlace(Player player, BlockPosition blockPosition) {
//        BlockPosition blockPos = blockPosition.copy();
//        if (blockPos.chunk == null) {
//            blockPos.convertToLocal(player.getZone());
//        }
//
//        BlockPosition frontBlockPos = blockPos.getOffsetBlockPos(
//                player.getZone(),
//                this.blockAxis.axis.x,
//                this.blockAxis.axis.y,
//                this.blockAxis.axis.z
//        );
//        if (frontBlockPos.chunk() == null) {
//            frontBlockPos.convertToLocal(player.getZone(), true);
//        }
//
//        if (blockPos.chunk() != null && frontBlockPos.chunk() != null) { return true; }
//
//        return false;
//    }
//}
