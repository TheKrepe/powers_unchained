package lucraft.mods.heroesexpansion.items;

import lucraft.mods.heroesexpansion.abilities.AbilityBlackHole;
import lucraft.mods.heroesexpansion.abilities.AbilityForceField;
import lucraft.mods.heroesexpansion.abilities.AbilityGrabEntity;
import lucraft.mods.heroesexpansion.abilities.AbilityPortal;
import lucraft.mods.lucraftcore.infinity.EnumInfinityStone;
import lucraft.mods.lucraftcore.infinity.ModuleInfinity;
import lucraft.mods.lucraftcore.infinity.items.ItemInfinityStone;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.util.abilitybar.EnumAbilityBarColor;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import lucraft.mods.lucraftcore.util.sounds.LCSoundEvents;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

import java.util.Random;

public class ItemSpaceStone extends ItemInfinityStone {

    public ItemSpaceStone(String name) {
        this.setTranslationKey(name);
        this.setRegistryName(StringHelper.unlocalizedToResourceName(name));
        this.setCreativeTab(ModuleInfinity.TAB);
    }

    @Override
    public EnumInfinityStone getType() {
        return EnumInfinityStone.SPACE;
    }

    @Override
    public boolean isContainer() {
        return false;
    }

    @Override
    public Ability.AbilityMap addStoneAbilities(EntityLivingBase entity, Ability.AbilityMap abilities, Ability.EnumAbilityContext context) {
        abilities.put("portal", new AbilityPortal(entity).setDataValue(Ability.BAR_COLOR, EnumAbilityBarColor.LIGHT_BLUE));
        abilities.put("grab_entity", new AbilityGrabEntity(entity).setDataValue(Ability.BAR_COLOR, EnumAbilityBarColor.LIGHT_BLUE));
        abilities.put("force_field", new AbilityForceField(entity).setDataValue(Ability.BAR_COLOR, EnumAbilityBarColor.LIGHT_BLUE).setMaxCooldown(60));
        if (ItemInfinityStone.hasStone(entity, context, EnumInfinityStone.POWER))
            abilities.put("black_hole", new AbilityBlackHole(entity).setDataValue(Ability.BAR_COLOR, EnumAbilityBarColor.LIGHT_BLUE).setMaxCooldown(6000));
        return super.addStoneAbilities(entity, abilities, context);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        return randomTeleport(playerIn, handIn);
    }

    public static ActionResult<ItemStack> randomTeleport(EntityPlayer player, EnumHand hand) {
        if (player instanceof EntityPlayerMP) {
            PlayerHelper.playSoundToAll(player.world, player.posX, player.posY + player.height / 2D, player.posZ, 50, LCSoundEvents.USE_INFINITY_GAUNTLET, SoundCategory.PLAYERS);
            DimensionType newDimension = getRandomDimension(((EntityPlayerMP) player).world.provider.getDimensionType(), new Random());
            player.changeDimension(newDimension.getId(), (world, en, yaw) -> {
                en.setLocationAndAngles(0, 0, 0, en.rotationYaw, en.rotationPitch);
            });
            World world = player.getEntityWorld();
            BlockPos spawn = newDimension == DimensionType.THE_END ? world.provider.getSpawnCoordinate() : world.getSpawnPoint();

            while (!(world.isAirBlock(spawn) && world.isAirBlock(spawn.up())) && spawn.getY() < world.provider.getHeight() - 5)
                spawn = spawn.up();

            player.setPositionAndUpdate(spawn.getX(), spawn.getY(), spawn.getZ());
            PlayerHelper.playSoundToAll(player.world, player.posX, player.posY + player.height / 2D, player.posZ, 50, LCSoundEvents.USE_INFINITY_GAUNTLET, SoundCategory.PLAYERS);
            return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        }

        return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    public static DimensionType getRandomDimension(DimensionType current, Random rand) {
        DimensionType[] dimensions = DimensionType.values();
        if (dimensions.length == 1)
            return current;
        DimensionType dim = dimensions[rand.nextInt(dimensions.length)];
        return dim == current ? getRandomDimension(current, rand) : dim;
    }
}
