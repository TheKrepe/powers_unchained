package me.shedaniel.rei.impl.common.plugins;

import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.mojang.datafixers.util.Pair;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import dev.architectury.utils.GameInstance;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.PluginView;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.plugins.REIPluginProvider;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.registry.Reloadable;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.logging.performance.PerformanceLogger.Plugin;
import me.shedaniel.rei.impl.common.logging.performance.PerformanceLogger.Plugin.Inner;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class PluginManagerImpl<P extends REIPlugin<?>> implements PluginManager<P>, PluginView<P> {
   private final List<Reloadable<P>> reloadables = new ArrayList<>();
   private final Map<Class<? extends Reloadable<P>>, Reloadable<? super P>> cache = new ConcurrentHashMap<>();
   private final Class<P> pluginClass;
   @Nullable
   private ReloadStage reloading = null;
   private final List<ReloadStage> observedStages = new ArrayList<>();
   private final List<REIPluginProvider<P>> plugins = new ArrayList<>();
   private final Stopwatch reloadStopwatch = Stopwatch.createUnstarted();
   private boolean forcedMainThread;
   private final Stopwatch forceMainThreadStopwatch = Stopwatch.createUnstarted();

   @SafeVarargs
   public PluginManagerImpl(Class<P> pluginClass, Reloadable<? extends P>... reloadables) {
      this.pluginClass = pluginClass;

      for (Reloadable<? extends P> reloadable : reloadables) {
         this.registerReloadable(reloadable);
      }
   }

   public void registerReloadable(Reloadable<? extends P> reloadable) {
      this.reloadables.add((Reloadable<P>)reloadable);
   }

   public boolean isReloading() {
      return this.reloading != null;
   }

   public <T extends Reloadable<? super P>> T get(Class<T> reloadableClass) {
      Reloadable<? super P> reloadable = this.cache.get(reloadableClass);
      if (reloadable != null) {
         return (T)reloadable;
      } else {
         for (Reloadable<P> r : this.reloadables) {
            if (reloadableClass.isInstance(r)) {
               this.cache.put(reloadableClass, r);
               return (T)r;
            }
         }

         throw new IllegalArgumentException("Unknown reloadable type! " + reloadableClass.getName());
      }
   }

   public List<Reloadable<P>> getReloadables() {
      return Collections.unmodifiableList(this.reloadables);
   }

   public PluginView<P> view() {
      return this;
   }

   public void registerPlugin(REIPluginProvider<? extends P> plugin) {
      this.plugins.add((REIPluginProvider<P>)plugin);
      InternalLogger.getInstance().info("Registered plugin provider %s for %s", new Object[]{plugin.getPluginProviderName(), name(this.pluginClass)});
   }

   public List<REIPluginProvider<P>> getPluginProviders() {
      return Collections.unmodifiableList(this.plugins);
   }

   public FluentIterable<P> getPlugins() {
      return FluentIterable.concat(Iterables.transform(this.plugins, REIPluginProvider::provide));
   }

   public FluentIterable<PluginManagerImpl.PluginWrapper<P>> getPluginWrapped() {
      return FluentIterable.concat(
         Iterables.transform(this.plugins, input -> Iterables.transform(input.provide(), plugin -> new PluginManagerImpl.PluginWrapper((P)plugin, input)))
      );
   }

   private PluginManagerImpl<P>.SectionClosable section(PluginReloadContext context, String section) {
      return new PluginManagerImpl.SectionClosable(context, section);
   }

   private void pluginSection(
      PluginReloadContext context,
      String sectionName,
      List<PluginManagerImpl.PluginWrapper<P>> list,
      @Nullable Reloadable<?> reloadable,
      BiConsumer<PluginManagerImpl.PluginWrapper<P>, PluginManagerImpl.SectionPluginSink> consumer
   ) throws InterruptedException {
      for (PluginManagerImpl.PluginWrapper<P> wrapper : list) {
         try (PluginManagerImpl<P>.SectionClosable section = this.section(context, sectionName + wrapper.getPluginProviderName() + "/")) {
            consumer.accept(
               wrapper,
               (respectMainThread, runnable) -> {
                  if (respectMainThread && reloadable != null && wrapper.plugin.shouldBeForcefullyDoneOnMainThread(reloadable)) {
                     try {
                        this.forcedMainThread = true;
                        this.forceMainThreadStopwatch.start();
                        InternalLogger.getInstance()
                           .warn(
                              "Forcing plugin "
                                 + wrapper.getPluginProviderName()
                                 + " to run on the main thread for "
                                 + sectionName
                                 + "! This is extremely dangerous, and have large performance implications."
                           );
                        if (Platform.getEnvironment() == Env.CLIENT) {
                           EnvExecutor.runInEnv(Env.CLIENT, () -> () -> this.queueExecutionClient(runnable));
                        } else {
                           this.queueExecution(runnable);
                        }
                     } finally {
                        this.forceMainThreadStopwatch.stop();
                     }
                  } else {
                     runnable.run();
                  }
               }
            );
         } catch (Throwable var13) {
            if (var13 instanceof InterruptedException) {
               throw (InterruptedException)var13;
            }

            InternalLogger.getInstance().error(wrapper.getPluginProviderName() + " plugin failed to " + sectionName + "!", var13);
         }
      }
   }

   private void queueExecution(Runnable runnable) {
      MinecraftServer server = GameInstance.getServer();
      if (server != null) {
         server.executeBlocking(runnable);
      }
   }

   private void queueExecutionClient(Runnable runnable) {
      Minecraft.getInstance().executeBlocking(runnable);
   }

   public void pre(PluginReloadContext context0) throws InterruptedException {
      this.reloading = context0.stage();
      PluginReloadContext context = PluginReloadContext.of(context0.stage(), context0.interruptionContext().withJob(() -> this.reloading = null));
      List<PluginManagerImpl.PluginWrapper<P>> plugins = new ArrayList<>(this.getPluginWrapped().toList());
      plugins.sort(Comparator.comparingDouble(PluginManagerImpl.PluginWrapper::getPriority).reversed());
      Collections.reverse(plugins);
      InternalLogger.getInstance().debug("========================================");
      InternalLogger.getInstance().debug(name(this.pluginClass) + " starting pre-reload for " + context.stage() + ".");
      InternalLogger.getInstance().debug("Reloadables (%d):".formatted(this.reloadables.size()));

      for (Reloadable<P> reloadable : this.reloadables) {
         InternalLogger.getInstance().debug(" - " + name(reloadable.getClass()));
      }

      InternalLogger.getInstance().debug("Plugins (%d):".formatted(plugins.size()));

      for (PluginManagerImpl.PluginWrapper<P> plugin : plugins) {
         InternalLogger.getInstance().debug(" - (%.2f) ".formatted(plugin.getPriority()) + plugin.getPluginProviderName());
      }

      InternalLogger.getInstance().debug("========================================");
      this.forcedMainThread = false;
      this.forceMainThreadStopwatch.reset();
      this.reloadStopwatch.reset().start();
      this.observedStages.clear();
      this.observedStages.add(context.stage());

      try (PluginManagerImpl<P>.SectionClosable preRegister = this.section(context, "pre-register/")) {
         Plugin perfLogger = RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.stage("Pre Registration");

         try {
            this.pluginSection(context, "pre-register/", plugins, null, (plugin, sink) -> {
               Inner innerx = perfLogger.plugin(new Pair(plugin.provider, plugin.plugin));

               try {
                  sink.accept(false, () -> plugin.plugin.preStage(this, context.stage()));
               } catch (Throwable var9x) {
                  if (innerx != null) {
                     try {
                        innerx.close();
                     } catch (Throwable var8x) {
                        var9x.addSuppressed(var8x);
                     }
                  }

                  throw var9x;
               }

               if (innerx != null) {
                  innerx.close();
               }
            });
         } catch (Throwable var24) {
            if (perfLogger != null) {
               try {
                  perfLogger.close();
               } catch (Throwable var18) {
                  var24.addSuppressed(var18);
               }
            }

            throw var24;
         }

         if (perfLogger != null) {
            perfLogger.close();
         }
      } catch (InterruptedException var26) {
         throw var26;
      } catch (Throwable var27) {
         InternalLogger.getInstance().throwException(new RuntimeException("Failed to run pre registration in stage [" + context.stage() + "]"));
      }

      try (PluginManagerImpl<P>.SectionClosable preStageAll = this.section(context, "pre-stage/")) {
         Plugin perfLogger = RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.stage("Pre Stage " + context.stage().name());

         try {
            for (Reloadable<P> reloadable : this.reloadables) {
               Class<?> reloadableClass = reloadable.getClass();

               try (PluginManagerImpl<P>.SectionClosable preStage = this.section(context, "pre-stage/" + name(reloadableClass) + "/")) {
                  Inner inner = perfLogger.stage(name(reloadableClass));

                  try {
                     reloadable.preStage(context.stage());
                  } catch (Throwable var19) {
                     if (inner != null) {
                        try {
                           inner.close();
                        } catch (Throwable var16) {
                           var19.addSuppressed(var16);
                        }
                     }

                     throw var19;
                  }

                  if (inner != null) {
                     inner.close();
                  }
               } catch (Throwable var21) {
                  if (var21 instanceof InterruptedException) {
                     throw (InterruptedException)var21;
                  }

                  InternalLogger.getInstance()
                     .error("Failed to run pre registration task for reloadable [" + name(reloadableClass) + "] in stage [" + context.stage() + "]", var21);
               }
            }
         } catch (Throwable var22) {
            if (perfLogger != null) {
               try {
                  perfLogger.close();
               } catch (Throwable var14) {
                  var22.addSuppressed(var14);
               }
            }

            throw var22;
         }

         if (perfLogger != null) {
            perfLogger.close();
         }
      }

      this.reloading = null;
      this.reloadStopwatch.stop();
      InternalLogger.getInstance().debug("========================================");
      InternalLogger.getInstance().debug(name(this.pluginClass) + " finished pre-reload for " + context.stage() + " in " + this.reloadStopwatch + ".");
      InternalLogger.getInstance().debug("========================================");
   }

   public void post(PluginReloadContext context0) throws InterruptedException {
      this.reloading = context0.stage();
      PluginReloadContext context = PluginReloadContext.of(context0.stage(), context0.interruptionContext().withJob(() -> this.reloading = null));
      List<PluginManagerImpl.PluginWrapper<P>> plugins = new ArrayList<>(this.getPluginWrapped().toList());
      plugins.sort(Comparator.comparingDouble(PluginManagerImpl.PluginWrapper::getPriority).reversed());
      Collections.reverse(plugins);
      InternalLogger.getInstance().debug("========================================");
      InternalLogger.getInstance().debug(name(this.pluginClass) + " starting post-reload for " + context.stage() + ".");
      InternalLogger.getInstance().debug("Reloadables (%d):".formatted(this.reloadables.size()));

      for (Reloadable<P> reloadable : this.reloadables) {
         InternalLogger.getInstance().debug(" - " + name(reloadable.getClass()));
      }

      InternalLogger.getInstance().debug("Plugins (%d):".formatted(plugins.size()));

      for (PluginManagerImpl.PluginWrapper<P> plugin : plugins) {
         InternalLogger.getInstance().debug(" - (%.2f) ".formatted(plugin.getPriority()) + plugin.getPluginProviderName());
      }

      InternalLogger.getInstance().debug("========================================");
      this.reloadStopwatch.start();
      Stopwatch postStopwatch = Stopwatch.createStarted();

      try (PluginManagerImpl<P>.SectionClosable postRegister = this.section(context, "post-register/")) {
         Plugin perfLogger = RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.stage("Post Registration");

         try {
            this.pluginSection(context, "post-register/", plugins, null, (plugin, sink) -> {
               Inner innerx = perfLogger.plugin(new Pair(plugin.provider, plugin.plugin));

               try {
                  sink.accept(false, () -> plugin.plugin.postStage(this, context.stage()));
               } catch (Throwable var9x) {
                  if (innerx != null) {
                     try {
                        innerx.close();
                     } catch (Throwable var8x) {
                        var9x.addSuppressed(var8x);
                     }
                  }

                  throw var9x;
               }

               if (innerx != null) {
                  innerx.close();
               }
            });
         } catch (Throwable var25) {
            if (perfLogger != null) {
               try {
                  perfLogger.close();
               } catch (Throwable var19) {
                  var25.addSuppressed(var19);
               }
            }

            throw var25;
         }

         if (perfLogger != null) {
            perfLogger.close();
         }
      } catch (Throwable var27) {
         if (var27 instanceof InterruptedException) {
            throw (InterruptedException)var27;
         }

         InternalLogger.getInstance().throwException(new RuntimeException("Failed to run post registration in stage [" + context.stage() + "]"));
      }

      try (PluginManagerImpl<P>.SectionClosable postStageAll = this.section(context, "post-stage/")) {
         Plugin perfLogger = RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.stage("Pre Stage " + context.stage().name());

         try {
            for (Reloadable<P> reloadable : this.reloadables) {
               Class<?> reloadableClass = reloadable.getClass();

               try (PluginManagerImpl<P>.SectionClosable postStage = this.section(context, "post-stage/" + name(reloadableClass) + "/")) {
                  Inner inner = perfLogger.stage(name(reloadableClass));

                  try {
                     reloadable.postStage(context.stage());
                  } catch (Throwable var20) {
                     if (inner != null) {
                        try {
                           inner.close();
                        } catch (Throwable var17) {
                           var20.addSuppressed(var17);
                        }
                     }

                     throw var20;
                  }

                  if (inner != null) {
                     inner.close();
                  }
               } catch (Throwable var22) {
                  if (var22 instanceof InterruptedException) {
                     throw (InterruptedException)var22;
                  }

                  InternalLogger.getInstance()
                     .error("Failed to run post registration task for reloadable [" + name(reloadableClass) + "] in stage [" + context.stage() + "]", var22);
               }
            }
         } catch (Throwable var23) {
            if (perfLogger != null) {
               try {
                  perfLogger.close();
               } catch (Throwable var15) {
                  var23.addSuppressed(var15);
               }
            }

            throw var23;
         }

         if (perfLogger != null) {
            perfLogger.close();
         }
      }

      this.reloading = null;
      this.reloadStopwatch.stop();
      postStopwatch.stop();
      InternalLogger.getInstance().debug("========================================");
      InternalLogger.getInstance()
         .info(name(this.pluginClass) + " finished post-reload for " + context.stage() + " in " + postStopwatch + ", totaling " + this.reloadStopwatch + ".");
      if (this.forcedMainThread) {
         InternalLogger.getInstance().warn("Forcing plugins to run on main thread took " + this.forceMainThreadStopwatch);
      }

      InternalLogger.getInstance().debug("========================================");
   }

   private static String name(Class<?> clazz) {
      String simpleName = clazz.getSimpleName();
      return simpleName.isEmpty() ? clazz.getName() : simpleName;
   }

   public void startReload(ReloadStage stage) {
      try {
         this.reload(PluginReloadContext.of(stage, ReloadInterruptionContext.ofNever()));
      } catch (InterruptedException var3) {
         ExceptionUtils.rethrow(var3);
      }
   }

   public void reload(PluginReloadContext context0) throws InterruptedException {
      try {
         this.reloadStopwatch.start();
         Stopwatch reloadingStopwatch = Stopwatch.createStarted();
         this.reloading = context0.stage();
         PluginReloadContext context = PluginReloadContext.of(context0.stage(), context0.interruptionContext().withJob(() -> this.reloading = null));
         List<PluginManagerImpl.PluginWrapper<P>> plugins = new ArrayList<>(this.getPluginWrapped().toList());
         plugins.sort(Comparator.comparingDouble(PluginManagerImpl.PluginWrapper::getPriority).reversed());
         Collections.reverse(plugins);
         String line = new String[]{"*", "=", "#", "@", "%", "~", "O", "-", "+"}[new Random().nextInt(9)].repeat(40);
         InternalLogger.getInstance().info(line);
         InternalLogger.getInstance().info(name(this.pluginClass) + " starting main-reload for " + context.stage() + ".");
         InternalLogger.getInstance().debug("Reloadables (%d):".formatted(this.reloadables.size()));

         for (Reloadable<P> reloadable : this.reloadables) {
            InternalLogger.getInstance().debug(" - " + name(reloadable.getClass()));
         }

         InternalLogger.getInstance().info("Plugins (%d):".formatted(plugins.size()));

         for (PluginManagerImpl.PluginWrapper<P> plugin : plugins) {
            InternalLogger.getInstance().info(" - (%.2f) ".formatted(plugin.getPriority()) + plugin.getPluginProviderName());
         }

         InternalLogger.getInstance().info(line);

         try (PluginManagerImpl<P>.SectionClosable startReloadAll = this.section(context, "start-reload/")) {
            Plugin perfLogger = RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.stage("Reload Initialization");

            try {
               for (Reloadable<P> reloadable : this.reloadables) {
                  Class<?> reloadableClass = reloadable.getClass();

                  try (PluginManagerImpl<P>.SectionClosable startReload = this.section(context, "start-reload/" + name(reloadableClass) + "/")) {
                     Inner inner = perfLogger.stage(name(reloadableClass));

                     try {
                        reloadable.startReload(context.stage());
                     } catch (Throwable var61) {
                        if (inner != null) {
                           try {
                              inner.close();
                           } catch (Throwable var59) {
                              var61.addSuppressed(var59);
                           }
                        }

                        throw var61;
                     }

                     if (inner != null) {
                        inner.close();
                     }
                  } catch (Throwable var63) {
                     if (var63 instanceof InterruptedException) {
                        throw (InterruptedException)var63;
                     }

                     InternalLogger.getInstance()
                        .error("Failed to run start-reload task for reloadable [" + name(reloadableClass) + "] in stage [" + context.stage() + "]", var63);
                  }
               }
            } catch (Throwable var64) {
               if (perfLogger != null) {
                  try {
                     perfLogger.close();
                  } catch (Throwable var53) {
                     var64.addSuppressed(var53);
                  }
               }

               throw var64;
            }

            if (perfLogger != null) {
               perfLogger.close();
            }
         }

         InternalLogger.getInstance().debug("========================================");
         InternalLogger.getInstance().debug(name(this.pluginClass) + " started main-reload for " + context.stage() + ".");
         InternalLogger.getInstance().debug("========================================");

         for (Reloadable<P> reloadable : this.getReloadables()) {
            Class<?> reloadableClass = reloadable.getClass();

            try (PluginManagerImpl<P>.SectionClosable reloadablePlugin = this.section(context, "reloadable-plugin/" + name(reloadableClass) + "/")) {
               Plugin perfLogger = RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.stage(name(reloadableClass));

               try {
                  Inner inner = perfLogger.stage("reloadable-plugin/" + name(reloadableClass) + "/prompt-others-before");

                  try {
                     for (Reloadable<P> listener : this.reloadables) {
                        try {
                           listener.beforeReloadable(context.stage(), reloadable);
                        } catch (Throwable var57) {
                           InternalLogger.getInstance()
                              .error("Failed to prompt others before reloadable [" + name(reloadableClass) + "] in stage [" + context.stage() + "]", var57);
                        }
                     }
                  } catch (Throwable var67) {
                     if (inner != null) {
                        try {
                           inner.close();
                        } catch (Throwable var51) {
                           var67.addSuppressed(var51);
                        }
                     }

                     throw var67;
                  }

                  if (inner != null) {
                     inner.close();
                  }

                  this.pluginSection(
                     context,
                     "reloadable-plugin/" + name(reloadableClass) + "/",
                     plugins,
                     reloadable,
                     (plugin, sink) -> {
                        Inner innerx = perfLogger.plugin(new Pair(plugin.provider, plugin.plugin));

                        try {
                           sink.accept(
                              true,
                              () -> {
                                 for (Reloadable<P> listenerxx : this.reloadables) {
                                    try {
                                       listenerxx.beforeReloadablePlugin(context.stage(), reloadable, plugin.plugin);
                                    } catch (Throwable var17) {
                                       InternalLogger.getInstance()
                                          .error(
                                             "Failed to run pre-reloadable task for "
                                                + plugin.getPluginProviderName()
                                                + " before reloadable ["
                                                + name(reloadableClass)
                                                + "] in stage ["
                                                + context.stage()
                                                + "]",
                                             var17
                                          );
                                    }
                                 }

                                 try {
                                    reloadable.acceptPlugin(plugin.plugin, context.stage());
                                 } finally {
                                    for (Reloadable<P> listenerx : this.reloadables) {
                                       try {
                                          listenerx.afterReloadablePlugin(context.stage(), reloadable, plugin.plugin);
                                       } catch (Throwable var16) {
                                          InternalLogger.getInstance()
                                             .error(
                                                "Failed to run post-reloadable task for "
                                                   + plugin.getPluginProviderName()
                                                   + " after reloadable ["
                                                   + name(reloadableClass)
                                                   + "] in stage ["
                                                   + context.stage()
                                                   + "]",
                                                var16
                                             );
                                       }
                                    }
                                 }
                              }
                           );
                        } catch (Throwable var11x) {
                           if (innerx != null) {
                              try {
                                 innerx.close();
                              } catch (Throwable var10x) {
                                 var11x.addSuppressed(var10x);
                              }
                           }

                           throw var11x;
                        }

                        if (innerx != null) {
                           innerx.close();
                        }
                     }
                  );
                  inner = perfLogger.stage("reloadable-plugin/" + name(reloadableClass) + "/prompt-others-after");

                  try {
                     for (Reloadable<P> listener : this.reloadables) {
                        try {
                           listener.afterReloadable(context.stage(), reloadable);
                        } catch (Throwable var56) {
                           InternalLogger.getInstance()
                              .error("Failed to prompt others after reloadable [" + name(reloadableClass) + "] in stage [" + context.stage() + "]", var56);
                        }
                     }
                  } catch (Throwable var66) {
                     if (inner != null) {
                        try {
                           inner.close();
                        } catch (Throwable var50) {
                           var66.addSuppressed(var50);
                        }
                     }

                     throw var66;
                  }

                  if (inner != null) {
                     inner.close();
                  }
               } catch (Throwable var68) {
                  if (perfLogger != null) {
                     try {
                        perfLogger.close();
                     } catch (Throwable var49) {
                        var68.addSuppressed(var49);
                     }
                  }

                  throw var68;
               }

               if (perfLogger != null) {
                  perfLogger.close();
               }
            }
         }

         InternalLogger.getInstance().debug("========================================");
         InternalLogger.getInstance().debug(name(this.pluginClass) + " ending main-reload for " + context.stage() + ".");
         InternalLogger.getInstance().debug("========================================");

         try (PluginManagerImpl<P>.SectionClosable endReloadAll = this.section(context, "end-reload/")) {
            Plugin perfLogger = RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.stage("Reload Finalization");

            try {
               for (Reloadable<P> reloadable : this.reloadables) {
                  Class<?> reloadableClass = reloadable.getClass();

                  try (PluginManagerImpl<P>.SectionClosable endReload = this.section(context, "end-reload/" + name(reloadableClass) + "/")) {
                     Inner inner = perfLogger.stage(name(reloadableClass));

                     try {
                        reloadable.endReload(context.stage());
                     } catch (Throwable var60) {
                        if (inner != null) {
                           try {
                              inner.close();
                           } catch (Throwable var55) {
                              var60.addSuppressed(var55);
                           }
                        }

                        throw var60;
                     }

                     if (inner != null) {
                        inner.close();
                     }
                  } catch (Throwable var71) {
                     if (var71 instanceof InterruptedException) {
                        throw (InterruptedException)var71;
                     }

                     InternalLogger.getInstance()
                        .error("Failed to run end-reload task for reloadable [" + name(reloadableClass) + "] in stage [" + context.stage() + "]", var71);
                  }
               }
            } catch (Throwable var72) {
               if (perfLogger != null) {
                  try {
                     perfLogger.close();
                  } catch (Throwable var47) {
                     var72.addSuppressed(var47);
                  }
               }

               throw var72;
            }

            if (perfLogger != null) {
               perfLogger.close();
            }
         }

         this.reloadStopwatch.stop();
         InternalLogger.getInstance().debug("========================================");
         InternalLogger.getInstance().debug(name(this.pluginClass) + " ended main-reload for " + context.stage() + " in " + reloadingStopwatch.stop() + ".");
         InternalLogger.getInstance().debug("========================================");
      } catch (Throwable var74) {
         if (var74 instanceof InterruptedException) {
            throw (InterruptedException)var74;
         }

         InternalLogger.getInstance().error("Failed to run reload task in stage [" + context0.stage() + "]", var74);
      } finally {
         this.reloading = null;
      }
   }

   public List<ReloadStage> getObservedStages() {
      return this.observedStages;
   }

   private record PluginWrapper<P extends REIPlugin<?>>(P plugin, REIPluginProvider<P> provider) {
      public double getPriority() {
         return this.plugin.getPriority();
      }

      public String getPluginProviderName() {
         String providerName = this.provider.getPluginProviderName();
         if (!this.provider.provide().isEmpty()) {
            String pluginName = this.plugin.getPluginProviderName();
            if (!providerName.equals(pluginName)) {
               providerName = pluginName + " of " + providerName;
            }
         }

         return providerName;
      }
   }

   private class SectionClosable implements Closeable {
      private final PluginReloadContext context;
      private final String section;
      private final Stopwatch stopwatch;

      public SectionClosable(PluginReloadContext context, String section) {
         Objects.requireNonNull(PluginManagerImpl.this);
         super();
         this.context = context;
         this.section = section;
         this.stopwatch = Stopwatch.createStarted();
         InternalLogger.getInstance()
            .trace(
               "[" + PluginManagerImpl.name(PluginManagerImpl.this.pluginClass) + " " + context.stage() + "] Reloading Section: \"%s\"", new Object[]{section}
            );
      }

      @Override
      public void close() {
         this.stopwatch.stop();
         InternalLogger.getInstance()
            .trace(
               "[" + PluginManagerImpl.name(PluginManagerImpl.this.pluginClass) + " " + this.context.stage() + "] Reloading Section: \"%s\" done in %s",
               new Object[]{this.section, this.stopwatch}
            );
         this.stopwatch.reset();

         try {
            this.context.interruptionContext().checkInterrupted();
         } catch (InterruptedException var2) {
            ExceptionUtils.rethrow(var2);
         }
      }
   }

   @FunctionalInterface
   private interface SectionPluginSink {
      void accept(boolean var1, Runnable var2);
   }
}
