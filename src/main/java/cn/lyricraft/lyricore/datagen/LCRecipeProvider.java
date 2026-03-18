package cn.lyricraft.lyricore.datagen;

import cn.lyricraft.lyricore.Lyricore;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.concurrent.CompletableFuture;

public abstract class LCRecipeProvider extends RecipeProvider {

    private String namespace = "";

    public LCRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    public void setNamespace(String namespace){
        this.namespace = namespace;
    }

    protected abstract void buildRecipes(RecipeOutput recipeOutput);

    public enum OreCookingType{
        SMELTING,
        BLASTING
    }

    public OreCookingBuilder oreCookingBuilder(OreCookingType type, ItemLike ingredient,
                                               ItemLike result, int cookingTime, RecipeCategory category, RecipeOutput recipeOutput){
        return new OreCookingBuilder(namespace, type, ingredient, result, cookingTime, category, recipeOutput);
    }

    public static class OreCookingBuilder{
        private String namespace;
        private RecipeOutput recipeOutput;
        private OreCookingType type;
        private ItemLike ingredient;
        private ItemLike result;
        private int cookingTime;
        private RecipeCategory category;
        private float experience = 0.0f;
        private String group = "";
        private String path = "";


        private OreCookingBuilder(String namespace, OreCookingType type, ItemLike ingredient,
                                  ItemLike result, int cookingTime, RecipeCategory category, RecipeOutput recipeOutput){
            this.namespace = namespace;
            this.recipeOutput = recipeOutput;
            this.type = type;
            this.ingredient = ingredient;
            this.result = result;
            this.cookingTime = cookingTime;
            this.category = category;
        }

        public OreCookingBuilder experience(float experience){
            this.experience = experience;
            return this;
        }

        public OreCookingBuilder group(String group){
            this.group = group;
            return this;
        }

        public OreCookingBuilder path(String path){
            this.path = path;
            return this;
        }

        public OreCookingBuilder conditions(ICondition... conditions){
            recipeOutput = recipeOutput.withConditions(conditions);
            return this;
        }

        public void build(){
            String truePath = namespace.isEmpty() ? "" : (namespace + ":");
            if (!path.isEmpty()) truePath += path;
            switch (type){
                case BLASTING -> {
                    if (path.isEmpty()) truePath += getItemName(result) + "_from_blasting_" + getItemName(ingredient);
                    SimpleCookingRecipeBuilder.generic(Ingredient.of(ingredient), category, result, experience, cookingTime,
                                    RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new).group(group).unlockedBy(getHasName(ingredient), has(ingredient))
                            .save(recipeOutput, truePath);
                }
                case SMELTING -> {
                    if (path.isEmpty()) truePath += getItemName(result) + "_from_smelting_" + getItemName(ingredient);
                    SimpleCookingRecipeBuilder.generic(Ingredient.of(ingredient), category, result, experience, cookingTime,
                                    RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new).group(group).unlockedBy(getHasName(ingredient), has(ingredient))
                            .save(recipeOutput, truePath);
                }
            }
        }

    }



}
