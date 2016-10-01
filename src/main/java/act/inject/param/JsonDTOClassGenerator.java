package act.inject.param;

import act.asm.*;
import act.inject.param.JsonDTOClassManager.DynamicClassLoader;
import org.osgl.$;
import org.osgl.inject.BeanSpec;
import org.osgl.util.S;

import java.util.List;

class JsonDTOClassGenerator implements Opcodes {


    private static final String JSON_DTO_CLASS = "act/inject/param/JsonDTO";

    private String className;
    private List<BeanSpec> beanSpecs;
    private DynamicClassLoader dynamicClassLoader;
    private ClassWriter cw;
    private MethodVisitor mv;

    JsonDTOClassGenerator(String name, List<BeanSpec> list, DynamicClassLoader classLoader) {
        this.className = name;
        this.beanSpecs = list;
        this.dynamicClassLoader = classLoader;
        this.cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    }

    Class<? extends JsonDTO> generate() {
        return $.cast(dynamicClassLoader.defineClass(className, generateByteCode()));
    }

    byte[] generateByteCode() {
        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null, JSON_DTO_CLASS, null);
        generateConstructor();
        generateSetters();
        return cw.toByteArray();
    }

    private void generateConstructor() {
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "act/inject/param/JsonDTO", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private void generateSetters() {
        for (BeanSpec beanSpec : beanSpecs) {
            generateSetter(beanSpec);
        }
    }

    private void generateSetter(BeanSpec beanSpec) {
        String setterName = setterName(beanSpec);
        mv = cw.visitMethod(ACC_PUBLIC, setterName, setterSignature(beanSpec), null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(beanSpec.name());
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "set", "(Ljava/lang/String;Ljava/lang/Object;)V", false);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitInsn(RETURN);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitLocalVariable("this", S.fmt("L%s;", className), null, l0, l2, 0);
        mv.visitLocalVariable("v", classDesc(beanSpec), null, l0, l2, 1);
        mv.visitMaxs(3, 2);
        mv.visitEnd();
    }

    private static String setterSignature(BeanSpec spec) {
        return S.fmt("(%s)V", classDesc(spec));
    }

    private static String classDesc(BeanSpec spec) {
        Class c = spec.rawType();
        if (c.isPrimitive()) {
            c = $.wrapperClassOf(c);
        }
        return Type.getDescriptor(c);
    }

    private static String setterName(BeanSpec beanSpec) {
        return S.builder("set").append(S.capFirst(beanSpec.name())).toString();
    }
}