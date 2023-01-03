package net.glease.ggfab.mte;

import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.*;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_InputBus;
import gregtech.api.util.GT_TooltipDataCache;
import gregtech.api.util.GT_Utility;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class MTE_LinkedInputBus extends GT_MetaTileEntity_Hatch_InputBus {
    public static final int SIZE_INVENTORY = 18;
    private SharedInventory mRealInventory;
    private final ItemStackHandlerProxy handler = new ItemStackHandlerProxy();
    private String mChannel;
    private boolean mPrivate;
    private State mState;
    private WorldSave save;

    public MTE_LinkedInputBus(int id, String name, String nameRegional, int tier) {
        super(id, name, nameRegional, tier, 1, new String[]{
                "16 slot input bus linked together wirelessly",
                "Link does not cross world boundary"
        });
    }

    public MTE_LinkedInputBus(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 1, aDescription, aTextures);
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTE_LinkedInputBus(mName, mTier, mDescriptionArray, mTextures);
    }

    @Override
    public int getCircuitSlot() {
        return 0;
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        // hack to clear this shit
        if (getBaseMetaTileEntity().isClientSide())
            Arrays.fill(ItemStackHandlerProxy.EMPTY, null);
        builder.widget(new TextFieldWidget()
                        .setSynced(true, true)
                        .setGetter(() -> mChannel == null ? "" : mChannel)
                        .setSetter(this::setChannel)
                        .setTextColor(Color.WHITE.dark(1))
                        .setTextAlignment(Alignment.CenterLeft)
                        .setBackground(GT_UITextures.BACKGROUND_TEXT_FIELD)
                        .setGTTooltip(() -> mTooltipCache.getData("ggfab.tooltip.linked_input_bus.change_freq_warn"))
                        .setSize(60, 18)
                        .setPos(48, 3))
                .widget(new CycleButtonWidget()
                        .setToggle(this::isPrivate, this::setPrivate)
                        .setTextureGetter(i -> i == 1 ? GT_UITextures.OVERLAY_BUTTON_CHECKMARK : GT_UITextures.OVERLAY_BUTTON_CROSS)
                        .setVariableBackground(GT_UITextures.BUTTON_STANDARD_TOGGLE)
                        .setSynced(true, true)
                        .setGTTooltip(() -> mTooltipCache.getData("ggfab.tooltip.linked_input_bus.private"))
                        .setSize(18, 18)
                        .setPos(150, 3))
                .widget(SlotGroup.ofItemHandler(handler, 9)
                        .startFromSlot(0)
                        .endAtSlot(SIZE_INVENTORY - 1)
                        .background(getGUITextureSet().getItemSlot())
                        .slotCreator(i -> new BaseSlot(handler, i, false) {
                            @Override
                            public boolean isEnabled() {
                                return mChannel != null;
                            }
                        })
                        .build()
                        .setPos(7, 24))
                .widget(new TextWidget(new Text("Private"))
                        .setPos(110, 3)
                        .setSize(43, 20))
                .widget(new TextWidget(new Text("Channel"))
                        .setPos(5, 3)
                        .setSize(43, 20));
    }

    @Override
    public int getCircuitSlotX() {
        return 152;
    }

    @Override
    public ItemStack getStackInSlot(int aIndex) {
        if (aIndex == getCircuitSlot())
            return super.getStackInSlot(aIndex);
        if (mState != State.Blocked && mRealInventory != null) {
            if (aIndex > 0 && aIndex <= SIZE_INVENTORY)
                return mRealInventory.stacks[aIndex - 1];
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int aIndex, ItemStack aStack) {
        if (aIndex == getCircuitSlot()) {
            mInventory[0] = GT_Utility.copyAmount(0, aStack);
        } else if (mState != State.Blocked && mRealInventory != null) {
            if (aIndex > 0 && aIndex <= SIZE_INVENTORY) {
                mRealInventory.stacks[aIndex] = aStack;
                getWorldSave().markDirty();
            }
        }
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return super.getTexturesActive(aBaseTexture);
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return super.getTexturesInactive(aBaseTexture);
    }

    @Override
    public boolean canInsertItem(int aIndex, ItemStack aStack, int aSide) {
        return isValidSlot(aIndex)
                && aStack != null
                && aIndex < getSizeInventory()
                && (mInventory[aIndex] == null || GT_Utility.areStacksEqual(aStack, mInventory[aIndex]))
                && allowPutStack(getBaseMetaTileEntity(), aIndex, (byte) aSide, aStack);
    }

    @Override
    public boolean canExtractItem(int aIndex, ItemStack aStack, int aSide) {
        return isValidSlot(aIndex)
                && aStack != null
                && aIndex < getSizeInventory()
                && allowPullStack(getBaseMetaTileEntity(), aIndex, (byte) aSide, aStack);
    }

    @Override
    public int getSizeInventory() {
        if (mState != State.Blocked && mRealInventory != null)
            return SIZE_INVENTORY + 1;
        return 1;
    }

    @Override
    public void startRecipeProcessing() {
        if (mRealInventory == null) return;
        if (mRealInventory.used) {
            mState = State.Blocked;
        } else {
            mRealInventory.used = true;
            mState = State.Activated;
        }
    }

    @Override
    public void endRecipeProcessing() {
        if (mState == State.Activated) {
            assert mRealInventory != null;
            mRealInventory.used = false;
        }
        mState = State.Default;
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
        super.onPostTick(aBaseMetaTileEntity, aTimer);

        for (int i = 0; i < mRealInventory.stacks.length; i++) {
            if (mRealInventory.stacks[i] != null && !GT_Utility.isStackValid(mRealInventory.stacks[i]))
                mRealInventory.stacks[i] = null;
        }
    }

    private void dropItems(ItemStack[] aStacks) {
        for (ItemStack stack : aStacks) {
            if (!GT_Utility.isStackValid(stack)) continue;
            EntityItem ei = new EntityItem(getBaseMetaTileEntity().getWorld(),
                    getBaseMetaTileEntity().getOffsetX(getBaseMetaTileEntity().getFrontFacing(), 1) + 0.5,
                    getBaseMetaTileEntity().getOffsetY(getBaseMetaTileEntity().getFrontFacing(), 1) + 0.5,
                    getBaseMetaTileEntity().getOffsetZ(getBaseMetaTileEntity().getFrontFacing(), 1) + 0.5,
                    stack);
            ei.motionX = ei.motionY = ei.motionZ = 0;
            getBaseMetaTileEntity().getWorld().spawnEntityInWorld(ei);
        }
    }

    @Override
    public boolean shouldDropItemAt(int index) {
        return mRealInventory != null && mRealInventory.ref <= 1;
    }

    @Override
    public void onBlockDestroyed() {
        super.onBlockDestroyed();
        if (mRealInventory != null) {
            if (--mRealInventory.ref <= 0)
                getWorldSave().remove(mChannel);
        }
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        if (mChannel != null)
            aNBT.setString("channel", mChannel);
        aNBT.setBoolean("private", mPrivate);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        String channel = aNBT.getString("channel");
        if ("".equals(channel))
            channel = null;
        this.mChannel = channel;
        mPrivate = aNBT.getBoolean("private");
    }

    public String getChannel() {
        return mChannel;
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);
        if (mChannel != null) {
            mRealInventory = getWorldSave().get(getRealChannel());
            handler.set(mRealInventory.stacks);
        }
    }

    private String getRealChannel() {
        if (mChannel == null) return null;
        if (mPrivate) return getBaseMetaTileEntity().getOwnerUuid() + mChannel;
        return new UUID(0, 0) + mChannel;
    }

    public boolean isPrivate() {
        return mPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        if (aPrivate == mPrivate) return;
        if (getBaseMetaTileEntity().isClientSide()) {
            mPrivate = aPrivate;
            return;
        }
        if (this.mChannel == null) {
            mPrivate = aPrivate;
            return;
        }
        getWorldSave().markDirty();
        if (--this.mRealInventory.ref <= 0) {
            // last referrer, drop inventory
            dropItems(mRealInventory.stacks);
            getWorldSave().remove(getRealChannel());
        }
        mPrivate = aPrivate;
        mRealInventory = getWorldSave().get(getRealChannel());
        this.handler.set(mRealInventory.stacks);
        mRealInventory.ref++;
        getWorldSave().markDirty();
    }

    public void setChannel(String aChannel) {
        if ("".equals(aChannel)) aChannel = null;
        if (getBaseMetaTileEntity().isClientSide()) {
            mChannel = aChannel;
            return;
        }
        if (Objects.equals(this.mChannel, aChannel)) return; // noop
        if (this.mChannel != null) {
            if (--this.mRealInventory.ref <= 0) {
                // last referrer, drop inventory
                dropItems(mRealInventory.stacks);
                getWorldSave().remove(getRealChannel());
            }
        }
        if (aChannel == null) {
            this.mChannel = null;
            this.mRealInventory = null;
            this.handler.setFake();
        } else {
            this.mChannel = aChannel;
            this.mRealInventory = getWorldSave().get(getRealChannel());
            this.handler.set(mRealInventory.stacks);
            mRealInventory.ref++;
        }
        getWorldSave().markDirty();
    }

    private WorldSave getWorldSave() {
        if (save == null) {
            WorldSave save = (WorldSave) getBaseMetaTileEntity().getWorld().loadItemData(WorldSave.class, "LinkedInputBusses");
            if (save == null) {
                save = new WorldSave("LinkedInputBusses");
                getBaseMetaTileEntity().getWorld().setItemData(save.mapName, save);
            }
            this.save = save;
        }
        return save;
    }

    private enum State {
        Activated,
        Blocked,
        Default,
    }

    private static class SharedInventory {
        private final ItemStack[] stacks;
        /**
         * Inventory wrapper for ModularUI
         */
        private final ItemStackHandler inventoryHandler;
        private boolean used;
        private int ref;

        public SharedInventory() {
            this.stacks = new ItemStack[SIZE_INVENTORY];
            inventoryHandler = new ItemStackHandler(stacks);
        }

        public SharedInventory(NBTTagCompound tag) {
            this.stacks = new ItemStack[SIZE_INVENTORY];
            inventoryHandler = new ItemStackHandler(stacks);

            for (int i = 0; i < SIZE_INVENTORY; i++) {
                String key = "" + i;
                if (!tag.hasKey(key, Constants.NBT.TAG_COMPOUND)) continue;
                stacks[i] = ItemStack.loadItemStackFromNBT(tag.getCompoundTag(key));
            }

            ref = tag.getInteger("ref");
        }

        public NBTTagCompound save() {
            NBTTagCompound tag = new NBTTagCompound();
            for (int i = 0; i < SIZE_INVENTORY; i++) {
                ItemStack stack = stacks[i];
                if (stack == null) continue;
                tag.setTag("" + i, stack.writeToNBT(new NBTTagCompound()));
            }
            tag.setInteger("ref", ref);
            return tag;
        }
    }

    public static class WorldSave extends WorldSavedData {
        private final Map<String, SharedInventory> data = new HashMap<>();

        public WorldSave(String p_i2141_1_) {
            super(p_i2141_1_);
        }

        @Override
        public void readFromNBT(NBTTagCompound tag) {
            data.clear();
            @SuppressWarnings("unchecked")
            Set<Map.Entry<String, NBTBase>> set = tag.tagMap.entrySet();
            for (Map.Entry<String, NBTBase> e : set) {
                data.put(e.getKey(), new SharedInventory((NBTTagCompound) e.getValue()));
            }
        }

        @Override
        public void writeToNBT(NBTTagCompound tag) {
            for (Map.Entry<String, SharedInventory> e : data.entrySet()) {
                if (e.getValue().ref > 0)
                    tag.setTag(e.getKey(), e.getValue().save());
            }
        }

        public SharedInventory get(Object channel) {
            return data.computeIfAbsent(channel.toString(), k -> new SharedInventory());
        }

        public void remove(Object channel) {
            data.remove(channel.toString());
            markDirty();
        }
    }

    private static class ItemStackHandlerProxy extends ItemStackHandler {
        private static final ItemStack[] EMPTY = new ItemStack[SIZE_INVENTORY];
        private boolean fake;

        public ItemStackHandlerProxy() {
            super(EMPTY);
            fake = true;
        }

        public void setFake() {
            set(EMPTY);
            fake = true;
        }

        public boolean isFake() {
            return fake;
        }

        public void set(ItemStack[] stacks) {
            this.stacks = Arrays.asList(stacks);
            fake = false;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound tag = super.serializeNBT();
            tag.setBoolean("fake", fake);
            return tag;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            super.deserializeNBT(nbt);
            fake = nbt.getBoolean("fake");
        }
    }
}
