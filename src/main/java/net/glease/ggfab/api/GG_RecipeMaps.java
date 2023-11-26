package net.glease.ggfab.api;

import java.util.*;

import net.glease.ggfab.GGItemList;
import net.minecraft.item.ItemStack;

import com.gtnewhorizons.modularui.common.widget.ProgressBar;

import gregtech.api.enums.Mods;
import gregtech.api.enums.ToolDictNames;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_RecipeBuilder;

public class GG_RecipeMaps {

    public static final GT_RecipeBuilder.MetadataIdentifier<ToolDictNames> OUTPUT_TYPE = GT_RecipeBuilder.MetadataIdentifier
            .create(ToolDictNames.class, "output_type");
    public static final GT_RecipeBuilder.MetadataIdentifier<Integer> OUTPUT_COUNT = GT_RecipeBuilder.MetadataIdentifier
            .create(Integer.class, "output_COUNT");
    private static final String TEXTURES_GUI_BASICMACHINES = "textures/gui/basicmachines";
    public static final GT_Recipe.GT_Recipe_Map sToolCastRecipes = new GT_Recipe.GT_Recipe_Map(
            new HashSet<>(20),
            "ggfab.recipe.toolcast",
            "Tool Casting Machine",
            null,
            Mods.GregTech.getResourcePath(TEXTURES_GUI_BASICMACHINES, "basicmachines/Default"),
            1,
            4,
            1,
            1,
            1,
            "",
            1,
            "",
            true,
            true).setProgressBar(GT_UITextures.PROGRESSBAR_ARROW, ProgressBar.Direction.RIGHT)
                    .setUsualFluidInputCount(1).setUsualFluidOutputCount(0).setRecipeEmitter(b -> {
                        Optional<GT_Recipe> rr = b.noOptimize().validateNoInput().validateInputFluidCount(0, 1)
                                .validateNoOutput().validateNoOutputFluid().build();
                        if (!rr.isPresent()) return Collections.emptyList();
                        ToolDictNames outputType = b.getMetadata(OUTPUT_TYPE);
                        GT_Recipe r = rr.get();
                        int outputSize = b.getMetadata(OUTPUT_COUNT);
                        if (outputSize > 64 * 4 || outputSize <= 0) return Collections.emptyList();
                        ItemStack shape, output;
                        try {
                            shape = GGItemList.valueOf("Shape_One_Use_" + outputType).get(0L);
                            output = GGItemList.valueOf("One_Use_" + outputType).get(outputSize);
                        } catch (IllegalArgumentException ex) {
                            // this looks like python not java, but I don't have better way around this
                            return Collections.emptyList();
                        }
                        output.stackSize = outputSize;
                        List<ItemStack> outputs = new ArrayList<>();
                        int maxStackSize = output.getMaxStackSize();
                        while (output.stackSize > maxStackSize) outputs.add(output.splitStack(maxStackSize));
                        outputs.add(output);
                        r.mInputs = new ItemStack[] { shape };
                        r.mOutputs = outputs.toArray(new ItemStack[0]);
                        return Collections.singletonList(r);
                    });
}
