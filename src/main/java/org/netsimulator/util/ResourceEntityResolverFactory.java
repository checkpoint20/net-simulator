package org.netsimulator.util;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public interface ResourceEntityResolverFactory {
    String CONFIG_DTD_PUBLIC_ID = "NET-Simulator/dtd/config.dtd";
    String PROJECT_DTD_PUBLIC_ID = "NET-Simulator/dtd/netsimulator.dtd";

    EntityResolver create(String publicId, String resource);

    ResourceEntityResolverFactory DEFAULT = (publicId, resource) ->
        (pubId, sysId) -> pubId != null && pubId.equals(publicId)
            ? new InputSource(ResourceEntityResolverFactory.class.getResourceAsStream(resource))
            : null;

    default EntityResolver createForConfig() {
        return create(CONFIG_DTD_PUBLIC_ID, "/dtd/config.dtd");
    }

    default EntityResolver createForProject() {
        return create(PROJECT_DTD_PUBLIC_ID, "/dtd/netsimulator.dtd");
    }
}
