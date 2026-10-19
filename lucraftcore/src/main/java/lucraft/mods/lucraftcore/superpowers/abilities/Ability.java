package lucraft.mods.lucraftcore.superpowers.abilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import lucraft.mods.lucraftcore.superpowers.abilities.data.*;
import lucraft.mods.lucraftcore.superpowers.abilities.predicates.AbilityCondition;
import lucraft.mods.lucraftcore.superpowers.abilities.predicates.AbilityConditionAbility;
import lucraft.mods.lucraftcore.superpowers.abilities.predicates.AbilityConditionLevel;
import lucraft.mods.lucraftcore.superpowers.abilities.predicates.AbilityConditionSuperpower;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilityContainer;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilitySupplier;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.IAbilityProvider;
import lucraft.mods.lucraftcore.superpowers.capabilities.CapabilitySuperpower;
import lucraft.mods.lucraftcore.util.abilitybar.AbilityBarHandler;
import lucraft.mods.lucraftcore.util.abilitybar.EnumAbilityBarColor;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.*;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

@EventBusSubscriber(modid = LucraftCore.MODID)
public abstract class Ability implements INBTSerializable<NBTTagCompound> {

    public static IForgeRegistry<AbilityEntry> ABILITY_REGISTRY;
    private static Map<EnumAbilityContext, AbilitySupplier> abilitySupplier = new HashMap<>();

    @SubscribeEvent
    public static void onRegisterNewRegistries(RegistryEvent.NewRegistry e) {
        ABILITY_REGISTRY = new RegistryBuilder<AbilityEntry>().setName(new ResourceLocation(LucraftCore.MODID, "ability")).setType(AbilityEntry.class).setIDRange(0, 2048).create();
    }

    @SubscribeEvent
    public static void registerAbilities(RegistryEvent.Register<AbilityEntry> e) {
        e.getRegistry().register(new AbilityEntry(AbilityTogglePower.class, new ResourceLocation(LucraftCore.MODID, "toggle_power")));
        e.getRegistry().register(new AbilityEntry(AbilityHealth.class, new ResourceLocation(LucraftCore.MODID, "health")));
        e.getRegistry().register(new AbilityEntry(AbilityHealing.class, new ResourceLocation(LucraftCore.MODID, "healing")));
        e.getRegistry().register(new AbilityEntry(AbilityStrength.class, new ResourceLocation(LucraftCore.MODID, "strength")));
        e.getRegistry().register(new AbilityEntry(AbilityPunch.class, new ResourceLocation(LucraftCore.MODID, "punch")));
        e.getRegistry().register(new AbilityEntry(AbilitySprint.class, new ResourceLocation(LucraftCore.MODID, "sprint")));
        e.getRegistry().register(new AbilityEntry(AbilityJumpBoost.class, new ResourceLocation(LucraftCore.MODID, "jump_boost")));
        e.getRegistry().register(new AbilityEntry(AbilityDamageResistance.class, new ResourceLocation(LucraftCore.MODID, "resistance")));
        e.getRegistry().register(new AbilityEntry(AbilityFallResistance.class, new ResourceLocation(LucraftCore.MODID, "fall_resistance")));
        e.getRegistry().register(new AbilityEntry(AbilityFireResistance.class, new ResourceLocation(LucraftCore.MODID, "fire_resistance")));
        e.getRegistry().register(new AbilityEntry(AbilityStepAssist.class, new ResourceLocation(LucraftCore.MODID, "step_assist")));
        e.getRegistry().register(new AbilityEntry(AbilitySizeChange.class, new ResourceLocation(LucraftCore.MODID, "size_change")));
        e.getRegistry().register(new AbilityEntry(AbilityTeleport.class, new ResourceLocation(LucraftCore.MODID, "teleport")));
        e.getRegistry().register(new AbilityEntry(AbilityKnockbackResistance.class, new ResourceLocation(LucraftCore.MODID, "knockback_resistance")));
        e.getRegistry().register(new AbilityEntry(AbilityPotionPunch.class, new ResourceLocation(LucraftCore.MODID, "potion_punch")));
        e.getRegistry().register(new AbilityEntry(AbilitySlowfall.class, new ResourceLocation(LucraftCore.MODID, "slowfall")));
        e.getRegistry().register(new AbilityEntry(AbilityEnergyBlast.class, new ResourceLocation(LucraftCore.MODID, "energy_blast")));
        e.getRegistry().register(new AbilityEntry(AbilityFirePunch.class, new ResourceLocation(LucraftCore.MODID, "fire_punch")));
        e.getRegistry().register(new AbilityEntry(AbilityFlight.class, new ResourceLocation(LucraftCore.MODID, "flight")));
        e.getRegistry().register(new AbilityEntry(AbilityWaterBreathing.class, new ResourceLocation(LucraftCore.MODID, "water_breathing")));
        e.getRegistry().register(new AbilityEntry(AbilityToughLungs.class, new ResourceLocation(LucraftCore.MODID, "tough_lungs")));
        e.getRegistry().register(new AbilityEntry(AbilityInvisibility.class, new ResourceLocation(LucraftCore.MODID, "invisibility")));
        e.getRegistry().register(new AbilityEntry(AbilityCommand.class, new ResourceLocation(LucraftCore.MODID, "command")));
        e.getRegistry().register(new AbilityEntry(AbilityCommandLoop.class, new ResourceLocation(LucraftCore.MODID, "command_loop")));
        e.getRegistry().register(new AbilityEntry(AbilityCommandToggle.class, new ResourceLocation(LucraftCore.MODID, "command_toggle")));
        e.getRegistry().register(new AbilityEntry(AbilityCommandHeld.class, new ResourceLocation(LucraftCore.MODID, "command_held")));
        e.getRegistry().register(new AbilityEntry(AbilityCommandOnGain.class, new ResourceLocation(LucraftCore.MODID, "command_on_gain")));
        e.getRegistry().register(new AbilityEntry(AbilityCommandOnLose.class, new ResourceLocation(LucraftCore.MODID, "command_on_lose")));
        e.getRegistry().register(new AbilityEntry(AbilitySetPunch.class, new ResourceLocation(LucraftCore.MODID, "set_punch")));
    }

    @SideOnly(Side.CLIENT)
    public static void generateHtmlFile(File file) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write("<html><head><title>Abilities</title><style>\n" +
                    "table{font-family:arial, sans-serif;border-collapse:collapse;}\n" +
                    "td,th{border:1px solid #666666;text-align:left;padding:8px;min-width:45px;}\n" +
                    "th{background-color:#CCCCCC;}\n" +
                    "p{margin:0;}\n" +
                    "tr:nth-child(even){background-color:#D8D8D8;}\n" +
                    "tr:nth-child(odd){background-color:#EEEEEE;}\n" +
                    "td.true{background-color:#72FF85AA;}\n" +
                    "td.false{background-color:#FF6666AA;}\n" +
                    "td.other{background-color:#42A3FFAA;}\n" +
                    "td.error{color:#FF0000;}\n" +
                    "th,td.true,td.false,td.other{text-align:center;}\n" +
                    "</style></head><body>");

            List<Ability> abilities = new ArrayList<>();
            Map<String, List<Ability>> sorted = new HashMap<>();

            // Sort abilities by mods
            for (AbilityEntry entry : Ability.ABILITY_REGISTRY.getValuesCollection()) {
                try {
                    Ability ability = entry.getAbilityClass().getConstructor(EntityLivingBase.class).newInstance((EntityLivingBase) null);
                    abilities.add(ability);
                    String modName = getModContainerFromId(entry.getRegistryName().getNamespace()) != null ? getModContainerFromId(entry.getRegistryName().getNamespace()).getName() : entry.getRegistryName().getNamespace();
                    List<Ability> modsAbilities = sorted.containsKey(modName) ? sorted.get(modName) : new ArrayList<>();
                    modsAbilities.add(ability);
                    sorted.put(modName, modsAbilities);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Generate overview lists
            sorted.forEach((s, l) -> {
                try {
                    bw.write("<h1>" + s + "</h1>\n");
                    bw.write("<ul>\n");
                    for (Ability ability : l) {
                        bw.write("<li><a href=\"#" + ability.getAbilityEntry().getRegistryName().toString() + "\">" + StringUtils.stripControlCodes(ability.getDisplayName()) + "</a></li>\n");
                    }
                    bw.write("</ul>\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            bw.write("\n");

            // Write ability info
            for (Ability ability : abilities) {
                AbilityEntry entry = ability.getAbilityEntry();
                bw.write("<hr>\n");

                // Title
                bw.write("<p><h1 id=\"" + entry.getRegistryName().toString() + "\">" + StringUtils.stripControlCodes(ability.getDisplayName()) + "</h1>\n");
                bw.write("<h3>" + entry.getRegistryName().toString() + "</h3>\n");
                bw.write("Type: " + ability.getAbilityType().toString() + "</p><br>\n");
                List<AbilityData<?>> dataList = ability.getDataManager().getSettingData();

                // Example
                bw.write("<p>Example:<br>\n");
                bw.write("<code>\"example_ability\": {<br>\n");
                bw.write("  \"ability\": \"" + entry.getRegistryName().toString() + "\",<br>\n");
                for (int i = 0; i < dataList.size(); i++) {
                    AbilityData abilityData = dataList.get(i);
                    Object value = abilityData.getDisplay(ability.getDataManager().getDefaultValue(abilityData));
                    String s = abilityData.displayAsString(ability.getDataManager().getDefaultValue(abilityData)) ? "\"" + value.toString() + "\"" : value.toString() + "";
                    bw.write("  \"" + abilityData.getJsonKey() + "\": " + s + (i < dataList.size() - 1 ? "," : "") + "<br>\n");
                }
                bw.write("}</code>\n");

                // Table
                bw.write("<table>\n<tr><th>Setting</th><th>Type</th><th>Default</th><th>Description</th></tr>\n");
                for (AbilityData abilityData : dataList) {
                    Object value = abilityData.getDisplay(ability.getDataManager().getDefaultValue(abilityData));
                    String s = abilityData.displayAsString(ability.getDataManager().getDefaultValue(abilityData)) ? "\"" + value.toString() + "\"" : value.toString() + "";
                    bw.write("<tr>\n" +
                            "<td><code>" + abilityData.getJsonKey() + "</code></td>\n" +
                            "<td><code>" + abilityData.getType().getTypeName().substring(abilityData.getType().getTypeName().lastIndexOf(".") + 1) + "</code></td>\n" +
                            "<td><code>" + s + "</code></td>\n" +
                            "<td><p>" + (abilityData.getDescription() == null || abilityData.getDescription().isEmpty() ? "/" : abilityData.getDescription()) + "</p>\n" +
                            "</td></tr><br>");
                }
                bw.write("</table>\n\n");
            }
            bw.write("</body></html>");
            bw.close();

            LucraftCore.LOGGER.info("Successfully generated abilities.html!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ModContainer getModContainerFromId(String modid) {
        for (ModContainer container : Loader.instance().getModList()) {
            if (container.getModId().equals(modid)) {
                return container;
            }
        }

        return null;
    }

    // -----------------------------------------------------------------------------------------

    public static final AbilityData<Boolean> UNLOCKED = new AbilityDataBoolean("unlocked").disableSaving();
    public static final AbilityData<Boolean> ENABLED = new AbilityDataBoolean("enabled").disableSaving();
    public static final AbilityData<Integer> MAX_COOLDOWN = new AbilityDataInteger("max_cooldown").disableSaving().setSyncType(EnumSync.SELF).enableSetting("cooldown", "Maximum cooldown for using this ability");
    public static final AbilityData<Integer> COOLDOWN = new AbilityDataInteger("cooldown").setSyncType(EnumSync.SELF);
    public static final AbilityData<Boolean> SHOW_IN_BAR = new AbilityDataBoolean("show_in_bar").disableSaving().setSyncType(EnumSync.SELF).enableSetting("show_in_bar", "Determines if this ability should be displayed in the ability bar");
    public static final AbilityData<Boolean> HIDDEN = new AbilityDataBoolean("hidden").setSyncType(EnumSync.SELF);
    public static final AbilityData<ITextComponent> TITLE = new AbilityDataTextComponent("title").disableSaving().setSyncType(EnumSync.SELF).enableSetting("title", "Allows you to set a custom title for this ability");
    public static final AbilityData<AbilityDataIcon.Icon> ICON = new AbilityDataIcon("icon").disableSaving().setSyncType(EnumSync.SELF).enableSetting("icon", "Lets you customize the icon for the ability");
    public static final AbilityData<EnumAbilityBarColor> BAR_COLOR = new AbilityDataBarColor("bar_color").disableSaving().setSyncType(EnumSync.SELF).enableSetting("bar_color", "Lets you choose the color of the ability bar frame for this ability. Available options: " + AbilityDataBarColor.values());

    protected final EntityLivingBase entity;
    protected final AbilityDataManager dataManager = new AbilityDataManager(this);
    protected int ticks;
    protected int ticksExisted;
    protected AbilityEntry entry;
    public EnumAbilityContext context;
    protected Ability parentAbility;
    protected List<AbilityCondition> conditions = new ArrayList<>();
    public EnumSync sync = EnumSync.NONE;
    public boolean dirty;
    private String key;

    public Ability(EntityLivingBase entity) {
        this.entity = entity;

        for (AbilityEntry entries : ABILITY_REGISTRY.getValues()) {
            if (entries.getAbilityClass() == this.getClass()) {
                entry = entries;
            }
        }

        this.registerData();
        this.ticks = 0;

        if (getAbilityType() == AbilityType.ACTION)
            this.setCooldown(getMaxCooldown());
        else
            this.setCooldown(0);
    }

    /**
     * This method is called BEFORE the abilities are added to the AbilityContainer
     * which is why you should use the abilities-parameter instead of getting the
     * abilities from method in Ability.class
     *
     * @param abilities
     */
    public void init(Map<String, Ability> abilities) {

    }

    public void registerData() {
        this.dataManager.register(UNLOCKED, true);
        this.dataManager.register(ENABLED, false);
        if (this.getAbilityType() != AbilityType.CONSTANT) {
            this.dataManager.register(MAX_COOLDOWN, 0);
            this.dataManager.register(COOLDOWN, 0);
        }
        this.dataManager.register(SHOW_IN_BAR, getAbilityType() != AbilityType.CONSTANT);
        this.dataManager.register(HIDDEN, false);
        this.dataManager.register(TITLE, new TextComponentTranslation(getTranslationName()));
        this.dataManager.register(ICON, new AbilityDataIcon.Icon(true));
        this.dataManager.register(BAR_COLOR, EnumAbilityBarColor.LIGHT_GRAY);
    }

    public AbilityDataManager getDataManager() {
        return dataManager;
    }

    public <T> Ability setDataValue(AbilityData<T> data, T value) {
        this.dataManager.set(data, value);
        return this;
    }

    public AbilityEntry getAbilityEntry() {
        return entry;
    }

    public String getKey() {
        return key;
    }

    public String getUnlocalizedName() {
        return ABILITY_REGISTRY.getKey(getAbilityEntry()).getPath();
    }

    public String getModId() {
        return ABILITY_REGISTRY.getKey(getAbilityEntry()).getNamespace();
    }

    public String getTranslationName() {
        return getModId() + ".abilities." + getUnlocalizedName() + ".name";
    }

    public String getTranslationDescription() {
        return getModId() + ".abilities." + getUnlocalizedName() + ".desc";
    }

    public EntityLivingBase getEntity() {
        return entity;
    }

    @SideOnly(Side.CLIENT)
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        LCRenderHelper.drawIcon(mc, gui, x, y, 0, 4);
    }

    @SideOnly(Side.CLIENT)
    public static void drawIcon(Ability ab, Minecraft mc, Gui gui, int x, int y) {
        AbilityDataIcon.Icon icon = ab.getDataManager().get(ICON);

        if (icon.internal)
            ab.drawIcon(mc, gui, x, y);
        else if (icon.texture != null) {
            if (!icon.texture.getPath().isEmpty()) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, 0);
                GlStateManager.scale(0.0625F, 0.0625F, 1);
                GlStateManager.color(1, 1, 1, 1);
                Minecraft.getMinecraft().renderEngine.bindTexture(icon.texture);
                gui.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
                GlStateManager.popMatrix();
            }
        } else if (!icon.stack.isEmpty()) {
            float zLevel = Minecraft.getMinecraft().getRenderItem().zLevel;
            Minecraft.getMinecraft().getRenderItem().zLevel = -100.5F;
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, 0);
            Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(icon.stack, 0, 0);
            GlStateManager.popMatrix();
            Minecraft.getMinecraft().getRenderItem().zLevel = zLevel;
        }
    }

    @SideOnly(Side.CLIENT)
    public void drawAdditionalInfo(Minecraft mc, Gui gui, int x, int y) {
        if (isEnabled() && (getAbilityType() == AbilityType.TOGGLE || getAbilityType() == AbilityType.HELD)) {
            mc.renderEngine.bindTexture(AbilityBarHandler.Renderer.HUD_TEX);
            mc.ingameGUI.drawTexturedModalRect(x + 12, y + 12, 24, 0, 6, 6);
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean renderCooldown() {
        return hasCooldown();
    }

    @SideOnly(Side.CLIENT)
    public float getCooldownPercentage() {
        return 1F - (float) getCooldown() / (float) getMaxCooldown();
    }

    public String getDisplayName() {
        return this.dataManager.get(TITLE).getFormattedText();
    }

    public String getDisplayDescription() {
        String desc = TextFormatting.UNDERLINE + "" + TextFormatting.BOLD + getDisplayName() + "\n" + TextFormatting.RESET + StringHelper
                .translateToLocal(getTranslationDescription());

        if (this.conditions.size() > 0)
            desc = desc + "\n";

        for (AbilityCondition predicate : this.conditions) {
            ITextComponent conditionText = new TextComponentString(TextFormatting.DARK_GRAY + " - ").appendSibling(predicate.getDisplayText().createCopy().setStyle(new Style().setColor(predicate.test(this) ? TextFormatting.GREEN : TextFormatting.RED)));
            desc = desc + "\n" + conditionText.getFormattedText();
        }

        return desc;
    }

    public void updateConditions() {
        this.ticksExisted++;
        if(this.getEntity() != null && !this.getEntity().world.isRemote) {
            boolean unlocked = true;

            if (this.conditions != null) {
                for (AbilityCondition condition : this.conditions) {
                    unlocked &= condition.test(this);
                }
            }

            this.dataManager.set(UNLOCKED, unlocked);

            if(this.ticksExisted <= 1200 || this.ticksExisted % 1200 == 0) {
                this.sync = EnumSync.EVERYONE;
                this.dirty = true;
            }
        }
    }

    public boolean isUnlocked() {
        return this.dataManager.get(UNLOCKED) && ((getParentAbility() == null || !(getParentAbility() instanceof AbilityToggle || getParentAbility() instanceof AbilityHeld)) || getParentAbility().isEnabled());
    }

    public boolean isEnabled() {
        if (getAbilityType() == AbilityType.ACTION)
            return false;
        return this.dataManager.get(ENABLED);
    }

    public void setEnabled(boolean enabled) {
        if (this.getEntity() != null && !this.getEntity().world.isRemote && this.isEnabled() != enabled) {
            this.dataManager.set(ENABLED, enabled);
        }
    }

    public Ability getParentAbility() {
        return parentAbility;
    }

    public Ability setParentAbility(Ability ability) {
        this.parentAbility = ability;
        return this;
    }

    public Ability addCondition(AbilityCondition condition) {
        this.conditions.add(condition);
        return this;
    }

    public List<AbilityCondition> getConditions() {
        return conditions;
    }

    public boolean hasCooldown() {
        return this.dataManager.has(MAX_COOLDOWN) && this.dataManager.get(MAX_COOLDOWN) > 0;
    }

    public int getCooldown() {
        if (!this.dataManager.has(MAX_COOLDOWN))
            return 0;
        return MathHelper.clamp(this.dataManager.get(COOLDOWN), 0, getMaxCooldown());
    }

    public void setCooldown(int cooldown) {
        if (this.dataManager.has(MAX_COOLDOWN)) {
            this.dataManager.set(COOLDOWN, MathHelper.clamp(cooldown, 0, getMaxCooldown()));
        }
    }

    public int getMaxCooldown() {
        if (!this.dataManager.has(MAX_COOLDOWN))
            return 0;
        return this.dataManager.get(MAX_COOLDOWN);
    }

    public Ability setMaxCooldown(int maxCooldown) {
        if (!this.dataManager.has(MAX_COOLDOWN))
            return this;
        this.dataManager.set(MAX_COOLDOWN, maxCooldown);

        if (getAbilityType() == AbilityType.ACTION)
            this.setCooldown(getMaxCooldown());
        else
            this.setCooldown(0);

        return this;
    }

    public int getTicks() {
        return ticks;
    }

    public boolean isCoolingdown() {
        return hasCooldown() && getCooldown() > 0 && (getAbilityType() != AbilityType.ACTION || !isEnabled());
    }

    public abstract AbilityType getAbilityType();

    public boolean isHidden() {
        return this.dataManager.get(HIDDEN);
    }

    public void setHidden(boolean hidden) {
        this.dataManager.set(HIDDEN, hidden);
    }

    public Ability setCustomTitle(ITextComponent title) {
        this.dataManager.set(TITLE, title);
        return this;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public abstract void onUpdate();

    public void firstTick() {

    }

    public void updateTick() {

    }

    public void lastTick() {

    }

    public boolean action() {
        return false;
    }

    public abstract void onKeyPressed();

    public abstract void onKeyReleased();

    public boolean showInAbilityBar() {
        return this.dataManager.get(SHOW_IN_BAR);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("Ability", getAbilityEntry().getRegistryName().toString());
        nbt.setTag("Data", this.dataManager.serializeNBT());
        nbt.setInteger("Ticks", ticks);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.dataManager.deserializeNBT(nbt.getCompoundTag("Data"));
        this.ticks = nbt.getInteger("Ticks");
    }

    public NBTTagCompound serializeNBTSync() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("Data", this.dataManager.serializeNBTSync());
        nbt.setInteger("Ticks", ticks);
        return nbt;
    }

    public void deserializeNBTSync(NBTTagCompound nbt) {
        this.dataManager.deserializeNBTSync(nbt.getCompoundTag("Data"));
        this.ticks = nbt.getInteger("Ticks");
    }

    public void onAttacked(LivingAttackEvent e) {

    }

    public void onEntityHurt(LivingHurtEvent e) {

    }

    public void onHurt(LivingHurtEvent e) {

    }

    public void onAttackEntity(AttackEntityEvent e) {

    }

    public void onBreakSpeed(BreakSpeed e) {

    }

    public void readFromAddonPack(JsonObject data, Ability.AbilityMap abilities) {
        for (AbilityData abilityData : this.dataManager.getData()) {
            this.dataManager.set(abilityData, abilityData.parseValue(data, this.dataManager.getDefaultValue(abilityData)));
        }

        if (JsonUtils.hasField(data, "required_level"))
            this.addCondition(new AbilityConditionLevel(JsonUtils.getInt(data, "required_level")));
        if (JsonUtils.hasField(data, "required_ability")) {
            for (Ability ab : abilities.values()) {
                if (ab.key.equals(JsonUtils.getString(data, "required_ability"))) {
                    this.addCondition(new AbilityConditionAbility(ab));
                }
            }
        }
        if (JsonUtils.hasField(data, "required_superpower"))
            this.addCondition(new AbilityConditionSuperpower(SuperpowerHandler.SUPERPOWER_REGISTRY.getValue(new ResourceLocation(JsonUtils.getString(data, "required_superpower")))));

        if (JsonUtils.hasField(data, "parent_ability"))
            for (Ability ab : abilities.values()) {
                if (ab.key.equals(JsonUtils.getString(data, "parent_ability"))) {
                    this.setParentAbility(ab);
                }
            }

        if (JsonUtils.hasField(data, "conditions")) {
            JsonArray jsonArray = JsonUtils.getJsonArray(data, "conditions");

            for (int i = 0; i < jsonArray.size(); i++) {
                AbilityCondition condition = AbilityCondition.ConditionFactory.parseCondition(jsonArray.get(i).getAsJsonObject(), this, abilities);
                if (condition != null)
                    this.addCondition(condition);
            }
        }
    }

    @Deprecated
    public static <T extends Ability> T getAbilityFromClass(List<Ability> list, Class<T> abilityClass) {
        for (Ability ab : list) {
            if (ab.getClass() == abilityClass) {
                return (T) ab;
            }
        }

        return null;
    }

    public static <T extends Ability> List<T> getAbilitiesFromClass(List<Ability> list, Class<T> abilityClass) {
        List<T> abilities = new ArrayList<>();
        for (Ability ab : list) {
            if (ab.getClass() == abilityClass) {
                abilities.add((T) ab);
            }
        }

        return abilities;
    }

    public static <T extends Ability> List<T> getAbilitiesFromClass(Collection<Ability> list, Class<T> abilityClass) {
        List<T> abilities = new ArrayList<>();
        for (Ability ab : list) {
            if (ab.getClass() == abilityClass) {
                abilities.add((T) ab);
            }
        }

        return abilities;
    }

    public static List<Ability> getAbilities(EntityLivingBase entity) {
        List<Ability> abilities = new ArrayList<>();

        for (EnumAbilityContext context : EnumAbilityContext.values()) {
            AbilityContainer container = entity.getCapability(CapabilitySuperpower.SUPERPOWER_CAP, null).getAbilityContainer(context);
            if (container != null)
                abilities.addAll(container.getAbilities());
        }

        return abilities;
    }

    public static boolean hasAbility(EntityLivingBase entity, Class<? extends Ability> ability) {
        for (Ability ab : getAbilities(entity)) {
            if (ab.getClass() == ability) {
                return true;
            }
        }
        return false;
    }

    public static AbilityEntry getAbilityEntryFromClass(Class<? extends Ability> clazz) {
        for (AbilityEntry entries : ABILITY_REGISTRY.getValues()) {
            if (entries.getAbilityClass().equals(clazz)) {
                return entries;
            }
        }

        return null;
    }

    public static boolean isAbilityEnabled(Ability ability) {
        return isAbilityEnabled(ability.getClass());
    }

    public static boolean isAbilityEnabled(Class<? extends Ability> clz) {
        List<ResourceLocation> list = new ArrayList<>();
        for (String s : LCConfig.superpowers.disabledAbilities)
            list.add(new ResourceLocation(s));
        return !list.contains(ABILITY_REGISTRY.getKey(Ability.getAbilityEntryFromClass(clz)));
    }

    public static void addAbilityContext(String name) {
        EnumHelper.addEnum(EnumAbilityContext.class, name, new Class<?>[0]);
    }

    public static void registerSupplier(EnumAbilityContext context, Function<EntityLivingBase, IAbilityProvider> providerSupplier, AbilitySupplier.AbilityContainerFactory containerFactory) {
        abilitySupplier.put(context, new AbilitySupplier(providerSupplier, containerFactory));
    }

    public static AbilitySupplier getAbilitySupplier(EnumAbilityContext context) {
        return abilitySupplier.get(context);
    }

    public static IAbilityProvider getAbilityProvider(EntityLivingBase entity, EnumAbilityContext context) {
        return abilitySupplier.containsKey(context) ? abilitySupplier.get(context).providerSupplier.apply(entity) : null;
    }

    public static AbilityContainer getAbilityContainer(EnumAbilityContext context, EntityLivingBase entity) {
        return entity.getCapability(CapabilitySuperpower.SUPERPOWER_CAP, null).getAbilityContainer(context);
    }

    public static List<AbilityGenerator> parseAbilityGenerators(JsonElement element) {
        List<AbilityGenerator> list = new ArrayList<>();

        if (element.isJsonArray()) {
            JsonArray array = (JsonArray) element;
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i) instanceof JsonObject) {
                    JsonObject o = (JsonObject) array.get(i);
                    ResourceLocation loc = new ResourceLocation(JsonUtils.getString(o, "ability"));
                    list.add(new AbilityGenerator(loc, generateAbilityKey(loc, list), o));
                }
            }
        } else if (element.isJsonObject()) {
            JsonObject object = (JsonObject) element;
            object.entrySet().forEach((e) -> {
                if (e.getValue() instanceof JsonObject) {
                    JsonObject o = (JsonObject) e.getValue();
                    ResourceLocation loc = new ResourceLocation(JsonUtils.getString(o, "ability"));
                    list.add(new AbilityGenerator(loc, e.getKey(), o));
                }
            });
        }

        return list;
    }

    private static String generateAbilityKey(ResourceLocation abilityLoc, List<AbilityGenerator> list) {
        String name = abilityLoc.getPath();
        boolean inList = true;

        while (inList) {
            inList = false;
            for (AbilityGenerator generator : list) {
                if (generator.key.equalsIgnoreCase(name)) {
                    inList = true;
                    name = name + "_";
                }
            }
        }

        return name;
    }

    public enum AbilityType {
        ACTION, HELD, TOGGLE, CONSTANT
    }

    public enum EnumAbilityContext {
        SUPERPOWER, SUIT, COMBAT, MAIN_HAND, OFF_HAND;

        public static EnumAbilityContext fromString(String name) {
            for (EnumAbilityContext context : EnumAbilityContext.values()) {
                if (context.toString().equals(name)) {
                    return context;
                }
            }

            return null;
        }
    }

    public static class AbilityMap extends LinkedHashMap<String, Ability> {

        @Override
        public Ability put(String key, Ability value) {
            value.key = key;
            return super.put(key, value);
        }
    }

}
