package com.mmodding.mmodding_lib.library.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mmodding.mmodding_lib.mixin.accessors.IngredientAccessor;
import com.mmodding.mmodding_lib.mixin.accessors.ShapelessRecipeSerializerAccessor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

import java.util.stream.Stream;

public class RecipeSerializationUtils {

	public static Ingredient getIngredient(Stream<Ingredient.Entry> entries) {
		return IngredientAccessor.invokeOfEntries(entries);
	}

	public static Ingredient.StackEntry getIngredientStack(JsonObject json) {
		if (json.has("item")) {
			return new Ingredient.StackEntry(new ItemStack(ShapedRecipe.getItem(json)));
		}
		else {
			throw new JsonParseException("Ingredient Entry Is Not An Item");
		}
	}

	public static Ingredient.TagEntry getIngredientTag(JsonObject json) {
	    if (json.has("tag")) {
			Identifier identifier = new Identifier(JsonHelper.getString(json, "tag"));
			return new Ingredient.TagEntry(TagKey.of(Registry.ITEM_KEY, identifier));
		}
		else {
			throw new JsonParseException("Ingredient Entry Is Not A Tag");
		}
	}

	public static Ingredient.Entry getIngredientEntry(JsonObject json) {
		return IngredientAccessor.invokeEntryFromJson(json);
	}

	public static ItemStack getStackWithoutData(JsonObject json) {
		if (json.has("data")) {
			throw new JsonParseException("Disallowed Data Member Found");
		} else {
			return RecipeSerializationUtils.getStack(json);
		}
	}

	public static ItemStack getStack(JsonObject json) {
		Item item = RecipeSerializationUtils.getItem(json);
		int count = JsonHelper.getInt(json, "count", 1);
		if (count < 1) {
			throw new JsonSyntaxException("Count Can Not Be " + count);
		} else {
			return new ItemStack(item, count);
		}
	}

	public static Item getItem(JsonObject json) {
		return ShapedRecipe.getItem(json);
	}

	public static DefaultedList<Ingredient> getIngredients(JsonObject json, String key, int number) {
		DefaultedList<Ingredient> ingredients = ShapelessRecipeSerializerAccessor.invokeGetIngredients(JsonHelper.getArray(json, key));
		if (ingredients.isEmpty()) {
			throw new JsonParseException("No Ingredients For Recipe");
		}
		else if (ingredients.size() > number) {
			throw new JsonParseException("To Many Ingredients For Recipe");
		}
		else {
			return ingredients;
		}
	}

	public static DefaultedList<Ingredient> readIngredients(PacketByteBuf buf) {
		DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(buf.readVarInt(), Ingredient.EMPTY);
        ingredients.replaceAll(ignored -> Ingredient.fromPacket(buf));
		return ingredients;
	}

	public static void writeIngredients(PacketByteBuf buf, DefaultedList<Ingredient> ingredients) {
		buf.writeVarInt(ingredients.size());
		ingredients.forEach(ingredient -> ingredient.write(buf));
	}
}
