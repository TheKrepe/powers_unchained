package lucraft.mods.lucraftcore.superpowers.abilities.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Objects;

public class AbilityDataBlockState extends AbilityData<IBlockState> {

    public AbilityDataBlockState(String key) {
        super(key);
    }

    @Override
    public IBlockState parseValue(JsonObject jsonObject, IBlockState defaultValue) {
        if (!JsonUtils.hasField(jsonObject, this.jsonKey))
            return defaultValue;
        JsonObject jsonObject1 = JsonUtils.getJsonObject(jsonObject, this.jsonKey);
        Block block = Block.getBlockFromName(JsonUtils.getString(jsonObject1, "block"));
        if (block == null)
            throw new JsonSyntaxException("Block " + JsonUtils.getString(jsonObject1, "block") + " does not exist!");
        return block.getStateFromMeta(JsonUtils.getInt(jsonObject1, "meta"));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt, IBlockState value) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Block", Objects.requireNonNull(value.getBlock().getRegistryName()).toString());
        tag.setInteger("Meta", value.getBlock().getMetaFromState(value));
        nbt.setTag(this.key, tag);
    }

    @Override
    public IBlockState readFromNBT(NBTTagCompound nbt, IBlockState defaultValue) {
        if (!nbt.hasKey(this.key))
            return defaultValue;
        NBTTagCompound tag = nbt.getCompoundTag(this.key);
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(tag.getString("Block")));
        if (block == null)
            return Blocks.AIR.getDefaultState();
        return block.getStateFromMeta(tag.getInteger("Meta"));
    }

}
