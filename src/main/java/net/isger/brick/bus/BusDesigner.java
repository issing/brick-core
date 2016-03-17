package net.isger.brick.bus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.isger.brick.bus.protocol.Protocol;
import net.isger.brick.bus.protocol.ProtocolsConversion;
import net.isger.brick.util.AbstractDesigner;

public class BusDesigner extends AbstractDesigner {

    private static final String PARAM_PROTOCOLS = "protocols";

    @SuppressWarnings("unchecked")
    public void design(Map<String, Object> config) {
        Object protocols = config.get(PARAM_PROTOCOLS);
        if (protocols == null) {
            protocols = Protocol.class.getPackage().getName();
        } else {
            List<Object> values = new ArrayList<Object>();
            values.add(Protocol.class.getPackage().getName());
            if (protocols instanceof Collection) {
                values.addAll((Collection<Object>) protocols);
            } else {
                values.add(protocols);
            }
            protocols = values;
        }
        config.put(PARAM_PROTOCOLS, protocols);
        super.design(config);
    }

    protected void prepare() {
        addConversion(EndpointsConversion.getInstance());
        addConversion(ProtocolsConversion.getInstance());
    }

}
