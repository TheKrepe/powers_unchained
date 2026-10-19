package lucraft.mods.lucraftcore.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class LCTransformer implements IClassTransformer, Opcodes {

    @Override
    public byte[] transform(String name, String transformedName, byte[] classBytes) {
        if (transformedName.equals("net.minecraft.client.renderer.entity.RenderLivingBase"))
            return patchRenderLivingBase(name, classBytes, false);

        if (transformedName.equals("net.minecraft.client.model.ModelBiped"))
            return patchModelBiped(name, classBytes, false);

        if (transformedName.equals("net.minecraft.client.Minecraft"))
            return patchMinecraft(name, classBytes, false);

        if (transformedName.equals("net.minecraft.entity.Entity"))
            return patchEntity(name, classBytes, false);

        return classBytes;
    }

    public static byte[] patchRenderLivingBase(String name, byte[] bytes, boolean obf) {
        String targetMethodName = LucraftCoreCoreMod.runtimeObfuscationEnabled ? "prepareScale" : "func_188322_c";
        String targetScaleMethodname = LucraftCoreCoreMod.runtimeObfuscationEnabled ? "scale" : "func_179152_a";
        String targetIsnName = "(Lnet/minecraft/entity/EntityLivingBase;F)F";

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (int i = 0; i < classNode.methods.size(); i++) {
            MethodNode method = classNode.methods.get(i);

            if (targetMethodName.equals(method.name) && targetIsnName.equals(method.desc)) {
                InsnList insnList = method.instructions;

                for (int j = 0; j < insnList.size(); j++) {
                    AbstractInsnNode insnNote = method.instructions.get(j);

                    if (insnNote.getOpcode() == Opcodes.INVOKESTATIC) {
                        MethodInsnNode method_0 = (MethodInsnNode) insnNote;

                        if (targetScaleMethodname.contains(method_0.name)) {
                            InsnList insnList_0 = new InsnList();
                            insnList_0.add(new VarInsnNode(ALOAD, 0));
                            insnList_0.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            String parameter = "(Lnet/minecraft/client/renderer/entity/RenderLivingBase;Lnet/minecraft/entity/EntityLivingBase;)V";
                            MethodInsnNode method_1 = new MethodInsnNode(Opcodes.INVOKESTATIC, "lucraft/mods/lucraftcore/core/LCClientHooks", "preRenderCallBack", parameter, false);
                            insnList_0.add(method_1);
                            insnList.insert(method_0, insnList_0);
                            break;
                        }
                    }
                }
                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    public static byte[] patchModelBiped(String name, byte[] bytes, boolean obf) {
        String renderMethod = LucraftCoreCoreMod.runtimeObfuscationEnabled ? "setRotationAngles" : "func_78087_a";
        String renderDesc = "(FFFFFFLnet/minecraft/entity/Entity;)V";

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (int j = 0; j < classNode.methods.size(); j++) {
            MethodNode method = classNode.methods.get(j);
            if (renderMethod.equals(method.name) && renderDesc.equals(method.desc)) {
                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode anode = iterator.next();

                    if (anode.getOpcode() == Opcodes.RETURN) {
                        InsnList newInstructions = new InsnList();
                        newInstructions.add(new VarInsnNode(ALOAD, 0));
                        newInstructions.add(new VarInsnNode(FLOAD, 1));
                        newInstructions.add(new VarInsnNode(FLOAD, 2));
                        newInstructions.add(new VarInsnNode(FLOAD, 3));
                        newInstructions.add(new VarInsnNode(FLOAD, 4));
                        newInstructions.add(new VarInsnNode(FLOAD, 5));
                        newInstructions.add(new VarInsnNode(FLOAD, 6));
                        newInstructions.add(new VarInsnNode(ALOAD, 7));
                        newInstructions.add(new MethodInsnNode(INVOKESTATIC, "lucraft/mods/lucraftcore/core/LCClientHooks", "renderBiped", "(Lnet/minecraft/client/model/ModelBiped;FFFFFFLnet/minecraft/entity/Entity;)V", false));
                        method.instructions.insertBefore(anode, newInstructions);
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    public static byte[] patchMinecraft(String name, byte[] classBytes, boolean b) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(classBytes);
        classReader.accept(classNode, 0);

        MethodNode refreshResources = null;
        MethodNode startGame = null;

        for (MethodNode mn : classNode.methods) {
            if (mn.name.equals("func_110436_a") || mn.name.equals("refreshResources")) {
                refreshResources = mn;
            } else if (mn.name.equals("func_71384_a") || mn.name.equals("init")) {
                startGame = mn;
            }
        }

        if (refreshResources != null) {
            for (int i = 0; i < refreshResources.instructions.size(); i++) {
                AbstractInsnNode ain = refreshResources.instructions.get(i);
                if (ain instanceof MethodInsnNode) {
                    MethodInsnNode min = (MethodInsnNode) ain;
                    if (min.name.equals("newArrayList")) {
                        AbstractInsnNode target = refreshResources.instructions.get(i + 1);

                        InsnList toInsert = new InsnList();
                        toInsert.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        toInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lucraft/mods/lucraftcore/core/LCClientHooks", "insertAddonPackResourcePacks", "(Ljava/util/List;)V", false));

                        refreshResources.instructions.insert(target, toInsert);
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);

        return writer.toByteArray();
    }

    public static byte[] patchEntity(String name, byte[] bytes, boolean obf) {
        String setSizeMethod = LucraftCoreCoreMod.runtimeObfuscationEnabled ? "setSize" : "func_70105_a";

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode method : classNode.methods) {
            if (method.name.equals(setSizeMethod) && method.desc.equals("(FF)V")) {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new VarInsnNode(FLOAD, 1));
                list.add(new VarInsnNode(FLOAD, 2));
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "lucraft/mods/lucraftcore/core/LCHooks", "setSize", "(Lnet/minecraft/entity/Entity;FF)V", false));
                for (int i = 0; i < method.instructions.size(); i++) {
                    AbstractInsnNode node = method.instructions.get(i);
                    list.add(node);
                }
                method.instructions.clear();
                method.instructions.add(list);
            }
        }

        ClassWriter writer = new ClassWriter(1);
        classNode.accept(writer);
        return writer.toByteArray();
    }

}
