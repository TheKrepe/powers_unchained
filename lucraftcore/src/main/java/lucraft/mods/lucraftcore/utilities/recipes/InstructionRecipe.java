package lucraft.mods.lucraftcore.utilities.recipes;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.utilities.network.MessageClearInstructionRecipes;
import lucraft.mods.lucraftcore.utilities.network.MessageSyncInstructionRecipe;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

import java.util.*;

@EventBusSubscriber(modid = LucraftCore.MODID)
public class InstructionRecipe {

    private static Map<ResourceLocation, InstructionRecipe> RECIPES = new HashMap<>();
    private static Map<ResourceLocation, InstructionRecipe> OVERRIDEN = new HashMap<>();

    public static void registerInstructionRecipe(InstructionRecipe recipe) {
        if (recipe.getRegistryName() == null) {
            LucraftCore.LOGGER.error("Instruction recipe is missing a registry name!");
            return;
        } else if (RECIPES.containsKey(recipe.getRegistryName())) {
            LucraftCore.LOGGER.error("There is already an instruction recipe with the registry name " + recipe.getRegistryName().toString() + "!");
            return;
        }

        RECIPES.put(recipe.getRegistryName(), recipe);
        OVERRIDEN.put(recipe.getRegistryName(), recipe);
    }

    public static Map<ResourceLocation, InstructionRecipe> getInstructionRecipesMap() {
        return OVERRIDEN;
    }

    public static List<InstructionRecipe> getInstructionRecipes() {
        List<InstructionRecipe> list = new ArrayList<>();
        for (InstructionRecipe recipes : OVERRIDEN.values())
            list.add(recipes);
        return list;
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerLoggedInEvent e) {
        if (!e.player.world.isRemote && e.player instanceof EntityPlayerMP) {
            LCPacketDispatcher.sendTo(new MessageClearInstructionRecipes(), (EntityPlayerMP) e.player);
            for (InstructionRecipe recipes : RECIPES.values()) {
                LCPacketDispatcher.sendTo(new MessageSyncInstructionRecipe(recipes), (EntityPlayerMP) e.player);
            }
        }
    }

    protected ItemStack output;
    protected List<ItemStack> requirements;
    protected ResourceLocation registryName;

    public InstructionRecipe(ItemStack output, ItemStack... requirements) {
        this.output = output;
        this.requirements = Arrays.asList(requirements);
    }

    public ItemStack getOutput() {
        return this.output;
    }

    public List<ItemStack> getRequirements() {
        return this.requirements;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InstructionRecipe))
            return false;
        InstructionRecipe r = (InstructionRecipe) obj;

        return ItemStack.areItemStacksEqualUsingNBTShareTag(r.getOutput(), getOutput()) && r.getRequirements().equals(this.getRequirements());
    }

    public InstructionRecipe setRegistryName(ResourceLocation name) {
        this.registryName = name;
        return this;
    }

    public InstructionRecipe setRegistryName(String modid, String name) {
        return setRegistryName(new ResourceLocation(modid, name));
    }

    public InstructionRecipe setRegistryName(String name) {
        return setRegistryName(new ResourceLocation(name));
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

}
