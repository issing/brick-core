package net.isger.brick.stub.dialect;


public class DefaultHitch {

    public static void hitch(Object source) {
        if (!(source instanceof Dialects)) {
            return;
        }
        Dialects.addDialect(new MysqlDialect());
        Dialects.addDialect(new OracleDialect());
        Dialects.addDialect(new H2Dialect());
    }

}
