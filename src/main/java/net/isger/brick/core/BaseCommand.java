package net.isger.brick.core;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avro.Schema;

import net.isger.brick.auth.AuthIdentity;
import net.isger.brick.stub.model.Meta;
import net.isger.brick.stub.model.Model;
import net.isger.util.Helpers;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.reflect.AssemblerAdapter;
import net.isger.util.reflect.BoundField;
import net.isger.util.reflect.ClassAssembler;
import net.isger.util.reflect.TypeToken;

/**
 * 基本命令
 * 
 * @author issing
 * 
 */
public class BaseCommand extends Command implements Cloneable {

    public static final int HEADERS = 0;

    public static final int PARAMETERS = 1;

    public static final int FOOTERS = 2;

    public static final String CTRL_IDENTITY = "brick-identity";

    public static final String CTRL_MODULE = "brick-module";

    public static final String CTRL_OPERATE = "brick-operate";

    public static final String CTRL_PAYLOAD = "brick-payload";

    public static final String DRCT_RESULT = "brick-result";

    private ShellCommand shell;

    public BaseCommand() {
        this(null, true);
    }

    public BaseCommand(Command source) {
        this(source, true);
    }

    public BaseCommand(boolean hasShell) {
        this(null, hasShell);
    }

    private BaseCommand(Command source, boolean hasShell) {
        if (hasShell) {
            ShellCommand shell = null;
            if (source == null) {
                source = this;
            } else if (source instanceof BaseCommand) {
                shell = ((BaseCommand) source).shell;
            }
            makeShell(source, shell);
        }
    }

    public static BaseCommand getAction() {
        try {
            return Context.getAction().getCommand();
        } catch (Exception e) {
            return null;
        }
    }

    public static BaseCommand newAction() {
        try {
            return Context.getAction().newCommand();
        } catch (Exception e) {
            return null;
        }
    }

    public static BaseCommand mockAction() {
        try {
            return Context.getAction().mockCommand();
        } catch (Exception e) {
            return null;
        }
    }

    public static BaseCommand realAction() {
        try {
            return Context.getAction().realCommand();
        } catch (Exception e) {
            return null;
        }
    }

    public static BaseCommand cast(Command source) {
        if (source == null || source instanceof BaseCommand) {
            return (BaseCommand) source;
        }
        return new BaseCommand(source, true);
    }

    public final BaseCommand cast() {
        return infect(new BaseCommand(false));
    }

    public final <T extends BaseCommand> T infect(T command) {
        ((BaseCommand) command).shell = this.shell;
        return command;
    }

    public Command getSource() {
        return shell.getSource();
    }

    public Schema getSchema() {
        return shell.getSchema();
    }

    public Object get(String name) {
        return shell.get(name);
    }

    public Object get(int index) {
        return shell.get(index);
    }

    public void put(String name, Object value) {
        shell.put(name, value);
    }

    public void put(int index, Object value) {
        shell.put(index, value);
    }

    public Map<CharSequence, ByteBuffer> getHeaders() {
        return shell.getHeaders();
    }

    public void setHeaders(Map<CharSequence, ByteBuffer> value) {
        shell.setHeaders(value);
    }

    public Map<CharSequence, ByteBuffer> getParameters() {
        return shell.getParameters();
    }

    public void setParameters(Map<CharSequence, ByteBuffer> value) {
        shell.setParameters(value);
    }

    public Map<CharSequence, ByteBuffer> getFooters() {
        return shell.getFooters();
    }

    public void setFooters(Map<CharSequence, ByteBuffer> value) {
        shell.setFooters(value);
    }

    public Map<String, Object> getHeader() {
        return shell.getHeader();
    }

    public <T> T getHeader(CharSequence key) {
        return shell.getHeader(key);
    }

    public void setHeader(Map<String, Object> parameters) {
        shell.setHeader(parameters);
    }

    public void setHeader(Map<String, Object> parameters, boolean appended) {
        shell.setHeader(parameters, appended);
    }

    public void setHeader(CharSequence key, Object value) {
        shell.setHeader(key, value);
    }

    public void clearHeader() {
        shell.setHeader(null);
    }

    public Model getParameter(Model model) {
        return getParameter(model, null, false);
    }

    public Model getParameter(Model model, String namespace) {
        return getParameter(model, namespace, false);
    }

    public <T> T getParameter(Model model, boolean isBatch) {
        return getParameter(model, null, isBatch);
    }

    @SuppressWarnings("unchecked")
    public <T> T getParameter(Model model, String namespace, boolean isBatch) {
        return (T) shell.getParameter(model, namespace, isBatch);
    }

    public <T> T getParameter(Class<T> type) {
        return getParameter(type, null, false);
    }

    public <T> T getParameter(Class<T> type, String namespace) {
        return getParameter(type, namespace, false);
    }

    public <T> T getParameter(Class<?> type, boolean isBatch) {
        return getParameter(type, null, isBatch);
    }

    public <T> T getParameter(Class<?> type, String namespace, boolean isBatch) {
        return getParameter(type, namespace, isBatch, null);
    }

    public <T> T getParameter(Class<T> type, ClassAssembler assembler) {
        return getParameter(type, null, false, assembler);
    }

    @SuppressWarnings("unchecked")
    public <T> T getParameter(Class<?> type, boolean isBatch, ClassAssembler assembler) {
        return (T) shell.getParameter(type, null, isBatch, assembler);
    }

    @SuppressWarnings("unchecked")
    public <T> T getParameter(Class<?> type, String namespace, boolean isBatch, ClassAssembler assembler) {
        return (T) shell.getParameter(type, namespace, isBatch, assembler);
    }

    public Map<String, Object> getParameter() {
        return shell.getParameter();
    }

    public <T> T getParameter(CharSequence key) {
        return getParameter(key, null, false, null);
    }

    public <T> T getParameter(CharSequence key, String namespace) {
        return getParameter(key, namespace, false, null);
    }

    public <T> T getParameter(CharSequence key, boolean isBatch) {
        return getParameter(key, null, isBatch, null);
    }

    public <T> T getParameter(CharSequence key, boolean isBatch, String suffix) {
        return getParameter(key, null, isBatch, suffix);
    }

    public <T> T getParameter(CharSequence key, String namespace, boolean isBatch) {
        return getParameter(key, namespace, isBatch, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T getParameter(CharSequence key, String namespace, boolean isBatch, String suffix) {
        return (T) shell.getParameter(key, namespace, isBatch, suffix);
    }

    public void setParameter(Map<String, ? extends Object> parameters) {
        shell.setParameter(parameters);
    }

    public void setParameter(Map<String, ? extends Object> parameters, boolean appended) {
        shell.setParameter(parameters, appended);
    }

    public void setParameter(CharSequence key, Object value) {
        shell.setParameter(key, value);
    }

    public void clearParameter() {
        shell.setParameter(null);
    }

    public Map<String, Object> getFooter() {
        return shell.getFooter();
    }

    public <T> T getFooter(CharSequence key) {
        return shell.getFooter(key);
    }

    public void setFooter(Map<String, Object> parameters) {
        shell.setFooter(parameters);
    }

    public void setFooter(Map<String, Object> parameters, boolean appended) {
        shell.setFooter(parameters, appended);
    }

    public void setFooter(CharSequence key, Object value) {
        shell.setFooter(key, value);
    }

    public void clearFooter() {
        shell.setFooter(null);
    }

    public Object get(int index, CharSequence key) {
        return shell.get(index, key);
    }

    public void set(int index, CharSequence key, Object value) {
        shell.set(index, key, value);
    }

    public String getPermission() {
        return getOperate();
    }

    public AuthIdentity getIdentity() {
        return shell.getIdentity();
    }

    public void setIdentity(AuthIdentity identity) {
        shell.setIdentity(identity);
    }

    public String getModule() {
        return shell.getModule();
    }

    public void setModule(String module) {
        shell.setModule(module);
    }

    public String getOperate() {
        return shell.getOperate();
    }

    public void setOperate(String operate) {
        shell.setOperate(operate);
    }

    public Object getPayload() {
        return shell.getPayload();
    }

    public void setPayload(Object payload) {
        shell.setPayload(payload);
    }

    public Object getResult() {
        return shell.getResult();
    }

    public void setResult(Object result) {
        shell.setResult(result);
    }

    public BaseCommand clone() {
        BaseCommand cmd;
        try {
            cmd = (BaseCommand) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
        Command source = this.getSource();
        if (source == this) {
            source = cmd;
        } else if (source instanceof BaseCommand) {
            source = ((BaseCommand) source).clone();
        }
        cmd.makeShell(source, this.shell);
        return cmd;
    }

    private void makeShell(Command source, ShellCommand shell) {
        this.shell = source == this ? new AutoCommand() : (source instanceof BaseCommand ? new ProxyCommand() : new ShellCommand());
        this.shell.source = source;
        if (shell == null) {
            shell = this.shell;
        }
        this.shell.mappings = Helpers.newMap(shell.mappings);
        this.shell.setHeaders(Helpers.newMap(shell.getHeaders()));
        this.shell.setParameters(Helpers.newMap(shell.getParameters()));
        this.shell.setFooters(Helpers.newMap(shell.getFooters()));
    }

    private class ShellCommand {

        private Command source;

        private Map<ByteBuffer, Object> mappings;

        protected Command getSource() {
            return source;
        }

        protected Schema getSchema() {
            return source.getSchema();
        }

        protected Object get(String name) {
            return source.get(name);
        }

        protected Object get(int index) {
            return source.get(index);
        }

        protected void put(String name, Object value) {
            source.put(name, value);
        }

        protected void put(int index, Object value) {
            source.put(index, value);
        }

        protected Map<CharSequence, ByteBuffer> getHeaders() {
            return source.getHeaders();
        }

        protected void setHeaders(Map<CharSequence, ByteBuffer> value) {
            source.setHeaders(value);
        }

        protected Map<CharSequence, ByteBuffer> getParameters() {
            return source.getParameters();
        }

        protected void setParameters(Map<CharSequence, ByteBuffer> value) {
            source.setParameters(value);
        }

        protected Map<CharSequence, ByteBuffer> getFooters() {
            return source.getFooters();
        }

        protected void setFooters(Map<CharSequence, ByteBuffer> value) {
            source.setFooters(value);
        }

        @SuppressWarnings("unchecked")
        protected Map<String, Object> gets(int index) {
            Map<CharSequence, ByteBuffer> indeces = (Map<CharSequence, ByteBuffer>) get(index);
            Map<String, Object> result = new HashMap<String, Object>(indeces.size());
            ByteBuffer buffer;
            for (Entry<CharSequence, ByteBuffer> entry : indeces.entrySet()) {
                if (mappings.containsKey(buffer = entry.getValue())) {
                    result.put(entry.getKey().toString(), mappings.get(buffer));
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        protected void sets(int index, Map<String, ? extends Object> values, boolean appended) {
            if (values == null) {
                values = new HashMap<String, Object>();
                appended = false;
            }
            Map<CharSequence, ByteBuffer> indeces = (Map<CharSequence, ByteBuffer>) get(index);
            if (!appended) clear(indeces);
            for (Entry<String, ? extends Object> entry : values.entrySet()) {
                set(indeces, entry.getKey(), entry.getValue());
            }
        }

        @SuppressWarnings("unchecked")
        protected <T> T get(int index, CharSequence key) {
            Map<CharSequence, ByteBuffer> indeces = (Map<CharSequence, ByteBuffer>) get(index);
            return (T) this.mappings.get(indeces.get(key));
        }

        @SuppressWarnings("unchecked")
        protected void set(int index, CharSequence key, Object value) {
            Map<CharSequence, ByteBuffer> buffers = (Map<CharSequence, ByteBuffer>) get(index);
            set(buffers, key, value);
        }

        private void set(Map<CharSequence, ByteBuffer> indeces, CharSequence key, Object value) {
            ByteBuffer index = indeces.get(key);
            if (value == null) {
                if (index != null) {
                    indeces.remove(key);
                    mappings.remove(index);
                }
            } else {
                synchronized (indeces) {
                    if (index == null) {
                        indeces.put(key, index = mapping(value));
                        return;
                    }
                }
                mappings.put(index, value);
            }
        }

        private ByteBuffer mapping(Object value) {
            ByteBuffer index;
            synchronized (mappings) {
                do {
                    index = ByteBuffer.wrap(Helpers.makeUUID().getBytes());
                } while (mappings.containsKey(index));
                mappings.put(index, value);
            }
            return index;
        }

        private void clear(Map<CharSequence, ByteBuffer> indeces) {
            for (ByteBuffer index : indeces.values()) {
                mappings.remove(index);
            }
            indeces.clear();
        }

        public final Map<String, Object> getHeader() {
            return gets(HEADERS);
        }

        public final <T> T getHeader(CharSequence key) {
            return get(HEADERS, key);
        }

        public final void setHeader(Map<String, Object> headers) {
            sets(HEADERS, headers, true);
        }

        public final void setHeader(Map<String, Object> headers, boolean appended) {
            sets(HEADERS, headers, appended);
        }

        public final void setHeader(CharSequence key, Object value) {
            set(HEADERS, key, value);
        }

        @SuppressWarnings("unchecked")
        public final Object getParameter(Model model, String namespace, boolean isBatch) {
            /* 获取所有参数键值对 */
            Map<String, Object> params = new HashMap<String, Object>();
            Object payload = getPayload();
            if (payload instanceof Map) {
                params.putAll((Map<String, Object>) payload);
            } else if (payload instanceof String && Strings.isNotEmpty(payload)) {
                Map<String, Object> pending = Helpers.fromJson((String) payload, Map.class);
                if (pending != null) {
                    params.putAll(pending);
                }
            }
            params.putAll(getParameter());
            params = Helpers.coalesce((Map<String, Object>) Helpers.getMap(params, namespace), params);
            Model instance;
            if (!isBatch) {
                instance = model.clone();
                instance.metaValue(params);
                return instance;
            }
            List<Model> result = new ArrayList<Model>();
            List<String> names = new ArrayList<String>();
            List<Object> values = new ArrayList<Object>();
            Object value;
            for (String name : model.metas().names()) {
                value = Helpers.getValues(params, name);
                if (value != null) {
                    names.add(name);
                    values.add(value);
                }
            }
            Object[] columns = names.toArray();
            for (Object[] row : Helpers.newGrid(true, values.toArray())) {
                instance = model.clone();
                instance.metaValue(Reflects.toMap(columns, row));
                result.add(instance);
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        public final Object getParameter(Class<?> type, String namespace, boolean isBatch, ClassAssembler assembler) {
            /* 获取所有参数键值对 */
            Map<String, Object> params = new HashMap<String, Object>();
            Object payload = getPayload();
            if (payload instanceof Map) {
                params.putAll((Map<String, Object>) payload);
            } else if (payload instanceof String && Strings.isNotEmpty(payload)) {
                Map<String, Object> pending = Helpers.fromJson((String) payload, Map.class);
                if (pending != null) {
                    params.putAll(pending);
                }
            }
            params.putAll(getParameter());
            params = Helpers.coalesce((Map<String, Object>) Helpers.getMap(params, namespace), params);
            if (assembler == null) {
                assembler = createAssembler();
            }
            /* 单实例转换 */
            if (Map.class.isAssignableFrom(type)) {
                return params;
            } else if (!isBatch) {
                return Reflects.newInstance(type, params, assembler);
            }
            /* 批实例转换 */
            List<Object> result = new ArrayList<Object>();
            List<String> names = new ArrayList<String>();
            List<Object> values = new ArrayList<Object>();
            String name;
            Object value;
            Map<String, List<BoundField>> fields = Reflects.getBoundFields(type);
            BoundField field;
            Class<?> rawClass;
            for (Entry<String, List<BoundField>> entry : fields.entrySet()) {
                name = entry.getKey();
                field = entry.getValue().get(0);
                rawClass = assembler.assemble(field.getToken().getRawClass());
                value = Helpers.getValues(params, name, rawClass);
                if (value == null) {
                    value = Helpers.getValues(params, name = field.getAlias(), rawClass);
                }
                if (value != null) {
                    names.add(name);
                    values.add(value);
                }
            }
            Object[] columns = names.toArray();
            for (Object[] row : Helpers.newGrid(true, values.toArray())) {
                result.add(Reflects.newInstance(type, Reflects.toMap(columns, row), assembler));
            }
            return Helpers.toArray(type, result.toArray());
        }

        @SuppressWarnings("unchecked")
        public final Object getParameter(CharSequence key, String namespace, boolean isBatch, String suffix) {
            /* 获取所有参数键值对 */
            Map<String, Object> params = new HashMap<String, Object>();
            Object payload = getPayload();
            if (payload instanceof Map) {
                params.putAll((Map<String, Object>) payload);
            } else if (payload instanceof String && Strings.isNotEmpty(payload)) {
                Map<String, Object> pending = Helpers.fromJson((String) payload, Map.class);
                if (pending != null) {
                    params.putAll(pending);
                }
            }
            params.putAll(getParameter());
            params = Helpers.coalesce((Map<String, Object>) Helpers.getMap(params, namespace), params); // 名称空间不为空时，参数集合将被层级化
            if (!isBatch) {
                return Helpers.getValue(params, key.toString());
            }
            Object values = Helpers.getValues(params, key.toString(), suffix);
            return values == null || values.getClass().isArray() ? values : Helpers.newArray(values);
        }

        protected ClassAssembler createAssembler() {
            final Console console = CoreHelper.getConsole();
            return console == null ? null : new AssemblerAdapter() {
                public Class<?> assemble(Class<?> rawClass) {
                    if (rawClass.isInterface()) {
                        rawClass = console.getContainer().getInstance(Class.class, (Strings.toColumnName(rawClass.getSimpleName()).replaceAll("[_]", ".") + ".class"));
                    }
                    return rawClass;
                }

                @SuppressWarnings("unchecked")
                public Object assemble(BoundField field, Object instance, Object value, Object... args) {
                    Map<String, Object> data = (Map<String, Object>) args[0]; // 组装数据
                    Assemble assermble = createAssemble(field); // 组装信息
                    if (value == Reflects.UNKNOWN) {
                        value = Helpers.getInstance(data, Strings.toFieldName(assermble.sourceColumn));
                    }
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
        private Assemble createAssemble(BoundField field) {
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

        private class Assemble {
            Meta meta;
            String sourceColumn;
            String targetField;
        }

        public final Map<String, Object> getParameter() {
            return gets(PARAMETERS);
        }

        public final void setParameter(Map<String, ? extends Object> parameters) {
            sets(PARAMETERS, parameters, true);
        }

        public final void setParameter(Map<String, ? extends Object> parameters, boolean appended) {
            sets(PARAMETERS, parameters, appended);
        }

        public final void setParameter(CharSequence key, Object value) {
            set(PARAMETERS, key, value);
        }

        public final Map<String, Object> getFooter() {
            return gets(FOOTERS);
        }

        public final <T> T getFooter(CharSequence key) {
            return get(FOOTERS, key);
        }

        public final void setFooter(Map<String, Object> footers) {
            sets(FOOTERS, footers, true);
        }

        public final void setFooter(Map<String, Object> footers, boolean appended) {
            sets(FOOTERS, footers, appended);
        }

        public final void setFooter(CharSequence key, Object value) {
            set(FOOTERS, key, value);
        }

        public final AuthIdentity getIdentity() {
            return getHeader(CTRL_IDENTITY);
        }

        public final void setIdentity(AuthIdentity identity) {
            setHeader(CTRL_IDENTITY, identity);
        }

        public final String getModule() {
            return getHeader(CTRL_MODULE);
        }

        public final void setModule(String module) {
            setHeader(CTRL_MODULE, module);
        }

        public final String getOperate() {
            return getHeader(CTRL_OPERATE);
        }

        public final void setOperate(String operate) {
            setHeader(CTRL_OPERATE, operate);
        }

        public final Object getPayload() {
            return getHeader(CTRL_PAYLOAD);
        }

        public final void setPayload(Object payload) {
            setHeader(CTRL_PAYLOAD, payload);
        }

        public final Object getResult() {
            return getFooter(DRCT_RESULT);
        }

        public final void setResult(Object result) {
            setFooter(DRCT_RESULT, result);
        }

    }

    private class ProxyCommand extends ShellCommand {

        protected BaseCommand getSource() {
            return (BaseCommand) super.getSource();
        }

        protected Map<String, Object> gets(int index) {
            return getSource().shell.gets(index);
        }

        protected void sets(int index, Map<String, ? extends Object> values, boolean appended) {
            getSource().shell.sets(index, values, appended);
        }

        protected <T> T get(int index, CharSequence key) {
            return getSource().shell.get(index, key);
        }

        protected void set(int index, CharSequence key, Object value) {
            getSource().shell.set(index, key, value);
        }

    }

    private class AutoCommand extends ShellCommand {

        protected Schema getSchema() {
            return BaseCommand.super.getSchema();
        }

        protected Object get(String name) {
            return BaseCommand.super.get(name);
        }

        protected Object get(int index) {
            return BaseCommand.super.get(index);
        }

        protected void put(String name, Object value) {
            BaseCommand.super.put(name, value);
        }

        protected void put(int index, Object value) {
            BaseCommand.super.put(index, value);
        }

        protected Map<CharSequence, ByteBuffer> getHeaders() {
            return BaseCommand.super.getHeaders();
        }

        protected void setHeaders(Map<CharSequence, ByteBuffer> value) {
            BaseCommand.super.setHeaders(value);
        }

        protected Map<CharSequence, ByteBuffer> getParameters() {
            return BaseCommand.super.getParameters();
        }

        protected void setParameters(Map<CharSequence, ByteBuffer> value) {
            BaseCommand.super.setParameters(value);
        }

        protected Map<CharSequence, ByteBuffer> getFooters() {
            return BaseCommand.super.getFooters();
        }

        protected void setFooters(Map<CharSequence, ByteBuffer> value) {
            BaseCommand.super.setFooters(value);
        }

    }

}
