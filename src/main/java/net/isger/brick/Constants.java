package net.isger.brick;

/**
 * 常量信息
 * 
 * @author issing
 * 
 */
public interface Constants {

    public static final String SYSTEM = "system";

    public static final String BRICK = "brick";

    public static final String DEFAULT = "default";

    // -----------------------------------------------------------------

    public static final String BRICK_ENCODING = "brick.encoding";

    public static final String ENCODING_UTF_8 = "UTF-8";

    public static final String BRICK_NAME = "brick.name";

    public static final String BRICK_RELOAD = "brick.reload";

    public static final String BRICK_RAW = "brick.raw";

    public static final String RAW_JSON = "json";

    public static final String RAW_XML = "xml";

    public static final String RAW_PROPS = "properties";

    public static final String BRICK_MODULE_DESCRIBE = "brick.module.describe";

    // =================================================================

    public static final String BRICK_CONTAINER = "brick.core.container";

    public static final String BRICK_CONSOLE = "brick.core.console";

    public static final String BRICK_COMMAND = "brick.core.command";

    public static final String BRICK_MODULE = "brick.core.module";

    // =================================================================

    public static final String MOD_TASK = "task";

    public static final String MOD_AUTH = "auth";

    public static final String MOD_BUS = "bus";

    public static final String MOD_SCHED = "sched";

    public static final String MOD_CACHE = "cache";

    public static final String MOD_PLUGIN = "plugin";

    public static final String MOD_STUB = "stub";

    // =================================================================

    /** 操作失败 */
    public static final int CODE_FAILURE = -1;

    /** 操作成功 */
    public static final int CODE_SUCCESS = 0;

    /** 没有会话 */
    public static final int CODE_UNSESSION = 1;

    /** 没有授权 */
    public static final int CODE_UNAUTH = 2;

}
