package lucraft.mods.lucraftcore.superpowers.abilities.supplier;

public enum EnumSync {

    NONE, SELF, EVERYONE;

    public EnumSync add(EnumSync newSync) {
        return newSync.ordinal() > this.ordinal() ? newSync : this;
    }

}
