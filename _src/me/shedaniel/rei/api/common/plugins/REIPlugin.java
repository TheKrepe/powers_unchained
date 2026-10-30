package me.shedaniel.rei.api.common.plugins;

import java.util.Collection;
import java.util.Collections;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.registry.Reloadable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

@OverrideOnly
public interface REIPlugin<P extends REIPlugin<?>> extends Comparable<REIPlugin<P>>, REIPluginProvider<P> {
   default double getPriority() {
      return 0.0;
   }

   default int compareTo(REIPlugin o) {
      return Double.compare(this.getPriority(), o.getPriority());
   }

   @OverrideOnly
   default void preStage(PluginManager<P> manager, ReloadStage stage) {
   }

   @OverrideOnly
   default void postStage(PluginManager<P> manager, ReloadStage stage) {
   }

   default Collection<P> provide() {
      return Collections.singletonList((P)this);
   }

   @Internal
   @Experimental
   default boolean shouldBeForcefullyDoneOnMainThread(Reloadable<?> reloadable) {
      return false;
   }
}
