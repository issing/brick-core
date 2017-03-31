package net.isger.brick.core;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.isger.brick.auth.AuthIdentity;
import net.isger.brick.stub.model.Model;
import net.isger.util.Helpers;
import net.isger.util.Reflects;
import net.isger.util.reflect.BoundField;

import org.apache.avro.Schema;

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

    public static final String KEY_IDENTITY = "brick-identity";

    public static final String KEY_MODULE = "brick-module";

    public static final String KEY_OPERATE = "brick-operate";

    public static final String KEY_RESULT = "brick-result";

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
        return this.infect(new BaseCommand(false));
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

    public void setHeader(CharSequence key, Object value) {
        shell.setHeader(key, value);
    }

    public Model getParameter(Model model) {
        return (Model) getParameter(model, null, false);
    }

    public Model getParameter(Model model, String namespace) {
        return (Model) getParameter(model, namespace, false);
    }

    public Object getParameter(Model model, boolean isBatch) {
        return getParameter(model, null, isBatch);
    }

    public Object getParameter(Model model, String namespace, boolean isBatch) {
        return shell.getParameter(model, namespace, isBatch);
    }

    @SuppressWarnings("unchecked")
    public <T> T getParameter(Class<T> type) {
        return (T) getParameter(type, null, false);
    }

    @SuppressWarnings("unchecked")
    public <T> T getParameter(Class<T> type, String namespace) {
        return (T) getParameter(type, namespace, false);
    }

    public Object getParameter(Class<?> type, boolean isBatch) {
        return getParameter(type, null, isBatch);
    }

    public Object getParameter(Class<?> type, String namespace, boolean isBatch) {
        return shell.getParameter(type, namespace, isBatch);
    }

    public Map<String, Object> getParameter() {
        return shell.getParameter();
    }

    public <T> T getParameter(CharSequence key) {
        return shell.getParameter(key);
    }

    public void setParameter(Map<String, Object> parameters) {
        shell.setParameter(parameters);
    }

    public void setParameter(CharSequence key, Object value) {
        shell.setParameter(key, value);
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

    public void setFooter(CharSequence key, Object value) {
        shell.setFooter(key, value);
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
        }
        cmd.makeShell(source, this.shell);
        return cmd;
    }

    private void makeShell(Command source, ShellCommand shell) {
        this.shell = source == this ? new AutoCommand() : new ShellCommand();
        this.shell.source = source;
        if (shell == null) {
            shell = this.shell;
        }
        this.shell.mappings = Helpers.getMap(shell.mappings);
        this.shell.setHeaders(Helpers.getMap(shell.getHeaders()));
        this.shell.setParameters(Helpers.getMap(shell.getParameters()));
        this.shell.setFooters(Helpers.getMap(shell.getFooters()));
    }

    private class ShellCommand {

        private Command source;

        private Map<ByteBuffer, Object> mappings;

        public Command getSource() {
            return source;
        }

        public Schema getSchema() {
            return source.getSchema();
        }

        public Object get(String name) {
            return source.get(name);
        }

        public Object get(int index) {
            return source.get(index);
        }

        public void put(String name, Object value) {
            source.put(name, value);
        }

        public void put(int index, Object value) {
            source.put(index, value);
        }

        public Map<CharSequence, ByteBuffer> getHeaders() {
            return source.getHeaders();
        }

        public void setHeaders(Map<CharSequence, ByteBuffer> value) {
            source.setHeaders(value);
        }

        public Map<CharSequence, ByteBuffer> getParameters() {
            return source.getParameters();
        }

        public void setParameters(Map<CharSequence, ByteBuffer> value) {
            source.setParameters(value);
        }

        public Map<CharSequence, ByteBuffer> getFooters() {
            return source.getFooters();
        }

        public void setFooters(Map<CharSequence, ByteBuffer> value) {
            source.setFooters(value);
        }

        @SuppressWarnings("unchecked")
        private Map<String, Object> gets(int index) {
            Map<CharSequence, ByteBuffer> indeces = (Map<CharSequence, ByteBuffer>) get(index);
            Map<String, Object> result = new HashMap<String, Object>(
                    indeces.size());
            ByteBuffer buffer;
            for (Entry<CharSequence, ByteBuffer> entry : indeces.entrySet()) {
                if (mappings.containsKey(buffer = entry.getValue())) {
                    result.put(entry.getKey().toString(), mappings.get(buffer));
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        private void sets(int index, Map<String, Object> values) {
            Map<CharSequence, ByteBuffer> indeces = (Map<CharSequence, ByteBuffer>) get(index);
            for (Entry<String, Object> entry : values.entrySet()) {
                set(indeces, entry.getKey(), entry.getValue());
            }
        }

        @SuppressWarnings("unchecked")
        public <T> T get(int index, CharSequence key) {
            Map<CharSequence, ByteBuffer> indeces = (Map<CharSequence, ByteBuffer>) get(index);
            return (T) this.mappings.get(indeces.get(key));
        }

        @SuppressWarnings("unchecked")
        public void set(int index, CharSequence key, Object value) {
            Map<CharSequence, ByteBuffer> buffers = (Map<CharSequence, ByteBuffer>) get(index);
            set(buffers, key, value);
        }

        private void set(Map<CharSequence, ByteBuffer> indeces,
                CharSequence key, Object value) {
            ByteBuffer index;
            synchronized (indeces) {
                if ((index = indeces.get(key)) == null) {
                    indeces.put(key, index = mapping(value));
                    return;
                }
            }
            this.mappings.put(index, value);
        }

        private ByteBuffer mapping(Object value) {
            ByteBuffer index;
            synchronized (mappings) {
                do {
                    index = ByteBuffer.wrap(Helpers.makeUUID().getBytes());
                } while (this.mappings.containsKey(index));
                this.mappings.put(index, value);
            }
            return index;
        }

        public Map<String, Object> getHeader() {
            return gets(HEADERS);
        }

        public <T> T getHeader(CharSequence key) {
            return get(HEADERS, key);
        }

        public void setHeader(Map<String, Object> headers) {
            sets(HEADERS, headers);
        }

        public void setHeader(CharSequence key, Object value) {
            set(HEADERS, key, value);
        }

        public Object getParameter(Model model, String namespace,
                boolean isBatch) {
            Map<String, Object> params = Helpers.getMap(getParameter(),
                    namespace);
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
                value = params.get(name + "[]");
                if (value == null) {
                    value = params.get(name);
                }
                if (value == null) {
                    continue;
                }
                names.add(name);
                values.add(value);
            }
            Object[] columns = names.toArray();
            for (Object[] row : Helpers.newGrid(true, values.toArray())) {
                instance = model.clone();
                instance.metaValue(Reflects.toMap(columns, row));
                result.add(instance);
            }
            return result;
        }

        public Object getParameter(Class<?> type, String namespace,
                boolean isBatch) {
            Map<String, Object> params = Helpers.getMap(getParameter(),
                    namespace);
            if (!isBatch) {
                return Reflects.newInstance(type, params);
            }
            List<Object> result = new ArrayList<Object>();
            List<String> names = new ArrayList<String>();
            List<Object> values = new ArrayList<Object>();
            String name;
            Object value;
            Map<String, List<BoundField>> fields = Reflects
                    .getBoundFields(type);
            for (Entry<String, List<BoundField>> entry : fields.entrySet()) {
                name = entry.getKey();
                value = params.get(name + "[]");
                if (value == null) {
                    value = params.get(name);
                }
                if (value == null) {
                    continue;
                }
                names.add(name);
                values.add(value);
            }
            Object[] columns = names.toArray();
            for (Object[] row : Helpers.newGrid(true, values.toArray())) {
                result.add(Reflects.newInstance(type,
                        Reflects.toMap(columns, row)));
            }
            return result;
        }

        public Map<String, Object> getParameter() {
            return gets(PARAMETERS);
        }

        public <T> T getParameter(CharSequence key) {
            return get(PARAMETERS, key);
        }

        public void setParameter(Map<String, Object> parameters) {
            sets(PARAMETERS, parameters);
        }

        public void setParameter(CharSequence key, Object value) {
            set(PARAMETERS, key, value);
        }

        public Map<String, Object> getFooter() {
            return gets(FOOTERS);
        }

        public <T> T getFooter(CharSequence key) {
            return get(FOOTERS, key);
        }

        public void setFooter(Map<String, Object> footers) {
            sets(FOOTERS, footers);
        }

        public void setFooter(CharSequence key, Object value) {
            set(FOOTERS, key, value);
        }

        public AuthIdentity getIdentity() {
            return getHeader(KEY_IDENTITY);
        }

        public void setIdentity(AuthIdentity identity) {
            setHeader(KEY_IDENTITY, identity);
        }

        public String getModule() {
            return getHeader(KEY_MODULE);
        }

        public void setModule(String module) {
            setHeader(KEY_MODULE, module);
        }

        public String getOperate() {
            return getHeader(KEY_OPERATE);
        }

        public void setOperate(String operate) {
            setHeader(KEY_OPERATE, operate);
        }

        public Object getResult() {
            return getFooter(KEY_RESULT);
        }

        public void setResult(Object result) {
            setFooter(KEY_RESULT, result);
        }

    }

    private class AutoCommand extends ShellCommand {

        public Schema getSchema() {
            return BaseCommand.super.getSchema();
        }

        public Object get(String name) {
            return BaseCommand.super.get(name);
        }

        public Object get(int index) {
            return BaseCommand.super.get(index);
        }

        public void put(String name, Object value) {
            BaseCommand.super.put(name, value);
        }

        public void put(int index, Object value) {
            BaseCommand.super.put(index, value);
        }

        public Map<CharSequence, ByteBuffer> getHeaders() {
            return BaseCommand.super.getHeaders();
        }

        public void setHeaders(Map<CharSequence, ByteBuffer> value) {
            BaseCommand.super.setHeaders(value);
        }

        public Map<CharSequence, ByteBuffer> getParameters() {
            return BaseCommand.super.getParameters();
        }

        public void setParameters(Map<CharSequence, ByteBuffer> value) {
            BaseCommand.super.setParameters(value);
        }

        public Map<CharSequence, ByteBuffer> getFooters() {
            return BaseCommand.super.getFooters();
        }

        public void setFooters(Map<CharSequence, ByteBuffer> value) {
            BaseCommand.super.setFooters(value);
        }

    }

}
