package net.glease.ggfab;

import static gregtech.api.enums.ToolDictNames.*;
import static gregtech.api.util.GT_RecipeBuilder.SECONDS;

import net.glease.ggfab.api.GG_RecipeMaps;
import net.glease.ggfab.api.GigaGramFabAPI;

import gregtech.api.enums.GT_Values;
import gregtech.api.enums.Materials;
import gregtech.api.enums.TierEU;
import gregtech.api.enums.ToolDictNames;
import gregtech.api.interfaces.IToolStats;
import gregtech.api.util.GT_Utility;

class SingleUseToolRecipeLoader implements Runnable {

    @Override
    public void run() {
        ToolDictNames[] hardTools = new ToolDictNames[] { craftingToolHardHammer, craftingToolScrewdriver,
                craftingToolWrench, craftingToolCrowbar, craftingToolWireCutter, craftingToolFile };
        ToolDictNames[] softTools = new ToolDictNames[] { craftingToolSoftHammer };
        addSingleUseToolRecipe(Materials.Steel, hardTools);
        addSingleUseToolRecipe(Materials.Silver, 5000, hardTools);
        addSingleUseToolRecipe(Materials.VanadiumSteel, hardTools);
        addSingleUseToolRecipe(Materials.TungstenSteel, hardTools);
        addSingleUseToolRecipe(Materials.HSSG, hardTools);
        addSingleUseToolRecipe(Materials.Rubber, softTools);
        addSingleUseToolRecipe(Materials.StyreneButadieneRubber, softTools);
        addSingleUseToolRecipe(Materials.Polybenzimidazole, softTools);
    }

    private void addSingleUseToolRecipe(Materials material, ToolDictNames... types) {
        addSingleUseToolRecipe(material, 10000, types);
    }

    private void addSingleUseToolRecipe(Materials material, int outputModifier, ToolDictNames... types) {
        if (material.mStandardMoltenFluid == null) {
            throw new IllegalArgumentException("material does not have molten fluid form");
        }
        for (ToolDictNames type : types) {
            IToolStats stats = GigaGramFabAPI.SINGLE_USE_TOOLS.get(type);
            if (stats == null) {
                throw new IllegalArgumentException(type + " not registered");
            }
            long fluids = 144L, duration = 6 * SECONDS;
            int count = (int) (material.mDurability * stats.getMaxDurabilityMultiplier()
                    * outputModifier
                    * 100
                    / stats.getToolDamagePerContainerCraft()
                    / 10000);
            if (count > 64 * 4) {
                double mod = (double) count / (64 * 4L);
                fluids = Math.max((long) (fluids / mod), 1L);
                duration = Math.max((long) (duration / mod), 1L);
                count = 64 * 4;
            } else if (count < 128) {
                int mod = GT_Utility.ceilDiv(128, count);
                fluids *= mod;
                duration *= mod;
                count *= mod;
            }
            GT_Values.RA.stdBuilder().fluidInputs(material.getMolten(fluids)) //
                    .metadata(GG_RecipeMaps.OUTPUT_TYPE, type) //
                    .metadata(GG_RecipeMaps.OUTPUT_COUNT, count) //
                    .eut(TierEU.RECIPE_MV).duration(duration) //
                    .addTo(GG_RecipeMaps.sToolCastRecipes);
        }
    }
}
