package net.glease.ggfab;

import static gregtech.api.enums.ToolDictNames.*;
import static gregtech.common.items.GT_MetaGenerated_Tool_01.*;
import static net.glease.ggfab.api.GG_RecipeMaps.sToolCastRecipes;

import java.util.stream.IntStream;

import net.glease.ggfab.api.GigaGramFabAPI;
import net.glease.ggfab.items.GGMetaItem_DumbItems;
import net.glease.ggfab.mte.MTE_AdvAssLine;
import net.glease.ggfab.mte.MTE_LinkedInputBus;
import net.glease.ggfab.nei.IMCForNEI;
import net.glease.ggfab.util.GGUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import gregtech.api.GregTech_API;
import gregtech.api.enums.GT_Values;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.SoundResource;
import gregtech.api.enums.TierEU;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_BasicMachine_GT_Recipe;
import gregtech.api.util.GT_ProcessingArray_Manager;
import gregtech.api.util.GT_RecipeConstants;

@Mod(
        modid = GGConstants.MODID,
        version = GGConstants.VERSION,
        name = GGConstants.MODNAME,
        acceptedMinecraftVersions = "[1.7.10]",
        dependencies = "required-after:IC2;required-before:gregtech")
public class GigaGramFab {

    public GigaGramFab() {
        // initialize the textures
        // noinspection ResultOfMethodCallIgnored
        BlockIcons.OVERLAY_FRONT_ADV_ASSLINE.name();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        GregTech_API.sAfterGTPreload.add(() -> {
            GGItemList.AdvAssLine.set(
                    new MTE_AdvAssLine(13532, "ggfab.machine.adv_assline", "Advanced Assembly Line").getStackForm(1));
            GGItemList.LinkedInputBus.set(
                    new MTE_LinkedInputBus(13533, "ggfab.machine.linked_input_bus", "Linked Input Bus", 5)
                            .getStackForm(1));
            GGItemList.ToolCast_MV.set(
                    new GT_MetaTileEntity_BasicMachine_GT_Recipe(
                            13534,
                            "ggfab.toolcast.tier.mv",
                            "Basic Tool Casting Machine",
                            2,
                            "Cheap Crafting Tool for you!",
                            sToolCastRecipes,
                            1,
                            4,
                            2400,
                            SoundResource.NONE,
                            false,
                            false,
                            GT_MetaTileEntity_BasicMachine_GT_Recipe.SpecialEffects.MAIN_RANDOM_SPARKS,
                            "TOOL_CAST",
                            new Object[] { "PGP", "WMW", "CBC", 'M', GT_MetaTileEntity_BasicMachine_GT_Recipe.X.HULL,
                                    'P', GT_MetaTileEntity_BasicMachine_GT_Recipe.X.PUMP, 'C',
                                    GT_MetaTileEntity_BasicMachine_GT_Recipe.X.CIRCUIT, 'W',
                                    GT_MetaTileEntity_BasicMachine_GT_Recipe.X.WIRE, 'G',
                                    GT_MetaTileEntity_BasicMachine_GT_Recipe.X.GLASS, 'B',
                                    ItemList.Shape_Empty.get(1L) }).getStackForm(1L));
            GGItemList.ToolCast_HV.set(
                    new GT_MetaTileEntity_BasicMachine_GT_Recipe(
                            13535,
                            "ggfab.toolcast.tier.hv",
                            "Advanced Tool Casting Machine",
                            3,
                            "Cheap Crafting Tool for you!",
                            sToolCastRecipes,
                            1,
                            4,
                            3200,
                            SoundResource.NONE,
                            false,
                            false,
                            GT_MetaTileEntity_BasicMachine_GT_Recipe.SpecialEffects.MAIN_RANDOM_SPARKS,
                            "TOOL_CAST",
                            new Object[] { "PGP", "WMW", "CBC", 'M', GT_MetaTileEntity_BasicMachine_GT_Recipe.X.HULL,
                                    'P', GT_MetaTileEntity_BasicMachine_GT_Recipe.X.PUMP, 'C',
                                    GT_MetaTileEntity_BasicMachine_GT_Recipe.X.CIRCUIT, 'W',
                                    GT_MetaTileEntity_BasicMachine_GT_Recipe.X.WIRE, 'G',
                                    GT_MetaTileEntity_BasicMachine_GT_Recipe.X.GLASS, 'B',
                                    ItemList.Shape_Empty.get(1L) }).getStackForm(1L));
            GGItemList.ToolCast_EV.set(
                    new GT_MetaTileEntity_BasicMachine_GT_Recipe(
                            13536,
                            "ggfab.toolcast.tier.ev",
                            "Master Tool Casting Machine",
                            4,
                            "Cheap Crafting Tool for you!",
                            sToolCastRecipes,
                            1,
                            4,
                            2400,
                            SoundResource.NONE,
                            false,
                            false,
                            GT_MetaTileEntity_BasicMachine_GT_Recipe.SpecialEffects.MAIN_RANDOM_SPARKS,
                            "TOOL_CAST",
                            new Object[] { "PGP", "WMW", "CBC", 'M', GT_MetaTileEntity_BasicMachine_GT_Recipe.X.HULL,
                                    'P', GT_MetaTileEntity_BasicMachine_GT_Recipe.X.PUMP, 'C',
                                    GT_MetaTileEntity_BasicMachine_GT_Recipe.X.CIRCUIT, 'W',
                                    GT_MetaTileEntity_BasicMachine_GT_Recipe.X.WIRE, 'G',
                                    GT_MetaTileEntity_BasicMachine_GT_Recipe.X.GLASS, 'B',
                                    ItemList.Shape_Empty.get(1L) }).getStackForm(1L));
            GigaGramFabAPI.addSingleUseToolType(craftingToolFile, INSTANCE.mToolStats.get(FILE));
            GigaGramFabAPI.addSingleUseToolType(craftingToolWrench, INSTANCE.mToolStats.get(WRENCH));
            GigaGramFabAPI.addSingleUseToolType(craftingToolCrowbar, INSTANCE.mToolStats.get(CROWBAR));
            GigaGramFabAPI.addSingleUseToolType(craftingToolWireCutter, INSTANCE.mToolStats.get(WIRECUTTER));
            GigaGramFabAPI.addSingleUseToolType(craftingToolHardHammer, INSTANCE.mToolStats.get(HARDHAMMER));
            GigaGramFabAPI.addSingleUseToolType(craftingToolSoftHammer, INSTANCE.mToolStats.get(SOFTMALLET));
            GigaGramFabAPI.addSingleUseToolType(craftingToolScrewdriver, INSTANCE.mToolStats.get(SCREWDRIVER));
            GT_ProcessingArray_Manager.addRecipeMapToPA("ggfab.toolcast", sToolCastRecipes);
        });
        GregTech_API.sBeforeGTPostload.add(new ComponentRecipeLoader());
        GregTech_API.sBeforeGTPostload.add(new SingleUseToolRecipeLoader());
        ConfigurationHandler.INSTANCE.init(event.getSuggestedConfigurationFile());

        initDumbItem1();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        IMCForNEI.IMCSender();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        GT_Values.RA.stdBuilder().itemInputs(
                IntStream.range(0, 16).mapToObj(ignored -> new ItemStack(Items.diamond)).toArray(ItemStack[]::new))
                .fluidInputs(Materials.SolderingAlloy.getMolten(1)).itemOutputs(ItemList.Pump_EV.get(1L))
                .metadata(GT_RecipeConstants.RESEARCH_ITEM, ItemList.Pump_LV.get(1))
                .metadata(GT_RecipeConstants.RESEARCH_TIME, 1).eut(TierEU.RECIPE_IV).duration(160000)
                .addTo(GT_RecipeConstants.AssemblyLine);
    }

    private void initDumbItem1() {
        GGMetaItem_DumbItems i1 = new GGMetaItem_DumbItems("ggfab.d1");
        int id = 0;
        {
            int idShape = 30;
            final int budget = idShape;
            String prefix = "One_Use_craftingTool";
            String prefix2 = "Shape_One_Use_craftingTool";
            for (GGItemList i : GGItemList.values()) {
                ItemStack stack = null;
                if (i.name().startsWith(prefix)) {
                    stack = i1.addItem(
                            id++,
                            "Single Use "
                                    + GGUtils.processSentence(i.name().substring(prefix.length()), ' ', true, true),
                            null,
                            i,
                            i.name().substring("One_Use_".length()));
                } else if (i.name().startsWith(prefix2)) {
                    stack = i1.addItem(
                            idShape++,
                            "Tool Casting Mold ("
                                    + GGUtils.processSentence(i.name().substring(prefix2.length()), ' ', true, true)
                                    + ")",
                            null,
                            i);
                }
                if (stack != null) {
                    i.set(stack);
                }
            }
            if (id >= budget || idShape >= 2 * budget || idShape - id != budget) throw new AssertionError();
            id = budget * 2;
        }
    }
}
