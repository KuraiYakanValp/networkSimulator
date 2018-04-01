package com.graduationproject.config;

import com.graduationproject.logic.*;
import com.graduationproject.model.*;
import com.graduationproject.model.interfaces.InterfaceComponent;
import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class Mappers {
    private static MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
    public static BoundMapperFacade<VirtualBoxComponents, Components> componentsMapper;
    public static BoundMapperFacade<VirtualBoxComponent, Component> componentMapper = mapperFactory.getMapperFacade(VirtualBoxComponent.class, Component.class);
    public static BoundMapperFacade<VirtualBoxComponentPC, ComponentPC> componentPcMapper = mapperFactory.getMapperFacade(VirtualBoxComponentPC.class, ComponentPC.class);
    public static BoundMapperFacade<VirtualBoxComponentRouter, ComponentRouter> componentRouterMapper = mapperFactory.getMapperFacade(VirtualBoxComponentRouter.class, ComponentRouter.class);
    public static BoundMapperFacade<VirtualBoxComponentSwitch, ComponentSwitch> componentSwitchMapper = mapperFactory.getMapperFacade(VirtualBoxComponentSwitch.class, ComponentSwitch.class);

    public Mappers() {
        initializeComponentsMapper();
    }

    private void initializeComponentsMapper() {
        MapperFactory componentsMapperFactory = new DefaultMapperFactory.Builder().build();
        componentsMapperFactory.classMap(VirtualBoxComponents.class, Components.class)
                .customize(new ComponentsMapper()).register();
        componentsMapper = componentsMapperFactory.getMapperFacade(VirtualBoxComponents.class, Components.class);
    }

    public static Component mapVirtualBoxComponent(VirtualBoxComponent virtualBoxComponent) {
        Component component;
        if (virtualBoxComponent.getClass().equals(VirtualBoxComponentPC.class)) {
            component = componentPcMapper.map((VirtualBoxComponentPC) virtualBoxComponent);
        } else if (virtualBoxComponent.getClass().equals(VirtualBoxComponentRouter.class)) {
            component = componentRouterMapper.map((VirtualBoxComponentRouter) virtualBoxComponent);
        } else if (virtualBoxComponent.getClass().equals(VirtualBoxComponentSwitch.class)) {
            component = componentSwitchMapper.map((VirtualBoxComponentSwitch) virtualBoxComponent);
        } else
            component = componentMapper.map(virtualBoxComponent);
        return component;
    }

    private class ComponentsMapper extends CustomMapper<VirtualBoxComponents, Components> {

        @Override
        public void mapAtoB(VirtualBoxComponents a, Components b, MappingContext context) {
            HashMap<Integer, InterfaceComponent> virtualBoxComponents = a.getComponents();
            HashMap<Integer, InterfaceComponent> components = new HashMap<Integer, InterfaceComponent>();

            for (Map.Entry<Integer, InterfaceComponent> entry : virtualBoxComponents.entrySet()) {
                VirtualBoxComponent virtualBoxComponent = (VirtualBoxComponent) entry.getValue();
                Component component = mapVirtualBoxComponent(virtualBoxComponent);
                components.put(entry.getKey(), component);
            }

            b.setComponents(components);
            b.setConnections(a.getConnections());
        }
    }
}


