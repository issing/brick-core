package net.isger.brick.inject;

import net.isger.brick.Constants;

/**
 * 引导构建器
 * 
 * @author issing
 * 
 */
public class BootstrapBuilder {

    protected final ContainerBuilder builder;

    protected BootstrapBuilder() {
        this.builder = new ContainerBuilder();
        this.builder.constant(Constants.BRICK_RELOAD, Boolean.FALSE);
    }

    public static Container build() {
        return new BootstrapBuilder().builder.create();
    }

}
