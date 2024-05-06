package net.isger.brick.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.isger.brick.core.Console;
import net.isger.brick.core.CoreHelper;
import net.isger.brick.inject.Container;
import net.isger.brick.stub.model.Meta;
import net.isger.util.Helpers;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.reflect.AssemblerAdapter;
import net.isger.util.reflect.BoundField;
import net.isger.util.reflect.ClassAssembler;
import net.isger.util.reflect.TypeToken;

public class Assemblers {

    private Assemblers() {
    }

    public static ClassAssembler createAssembler() {
        Console console = CoreHelper.getConsole();
        return console == null ? null : createAssembler(console.getContainer());
    }

    public static ClassAssembler createAssembler(final Container container) {
        return new AssemblerAdapter() {
            public Class<?> assemble(Class<?> rawClass) {
                if (rawClass.isInterface()) {
                    rawClass = container.getInstance(Class.class, (Strings.toColumnName(rawClass.getSimpleName()).replaceAll("[_]", ".") + ".class"));
                }
                return rawClass;
            }

            @SuppressWarnings("unchecked")
            public Object assemble(BoundField field, Object instance, Object value, Map<String, ? extends Object>... args) {
                Map<String, ? extends Object> data = args[args.length - 1]; // 组装数据
                Assemble assermble = createAssemble(field); // 组装信息
                if (value == Reflects.UNKNOWN) value = Helpers.getInstance(data, Strings.toFieldName(assermble.sourceColumn));
                TypeToken<?> typeToken = field.getToken(); // 组装类型
                Class<?> rawClass = typeToken.getRawClass();
                if (Collection.class.isAssignableFrom(rawClass)) {
                    rawClass = (Class<?>) Reflects.getActualType(typeToken.getType());
                } else if (rawClass.isArray()) {
                    rawClass = (Class<?>) Reflects.getComponentType(typeToken.getType());
                }
                // 获取接口类型所配置的实现类型
                rawClass = assemble(rawClass);
                if (!(value == null || value instanceof Map)) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put(assermble.targetField, value);
                    value = params;
                }
                return Reflects.newInstance(rawClass, (Map<String, Object>) value, this);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static Assemble createAssemble(BoundField field) {
        Assemble assemble = new Assemble();
        assemble.meta = Meta.createMeta(field); // 元字段
        if (assemble.meta.toModel() == null) {
            assemble.sourceColumn = assemble.meta.getName();
            assemble.targetField = (String) assemble.meta.getValue();
        } else {
            Map<String, Object> params = (Map<String, Object>) assemble.meta.getValue();
            Map<String, Object> source = (Map<String, Object>) params.get("source");
            assemble.sourceColumn = (String) source.get("name");
            Map<String, Object> target = (Map<String, Object>) params.get("target");
            assemble.targetField = Strings.toFieldName((String) target.get("name"));
        }
        return assemble;
    }

    private static class Assemble {
        Meta meta;
        String sourceColumn;
        String targetField;
    }

}
