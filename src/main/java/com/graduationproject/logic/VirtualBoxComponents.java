package com.graduationproject.logic;

import com.graduationproject.logic.Interfaces.StringSender;
import com.graduationproject.logic.exceptions.CodedExceptions;
import com.graduationproject.logic.exceptions.SshExceptions;
import com.graduationproject.logic.exceptions.VirtualBoxExceptions;
import com.graduationproject.logic.exceptions.VirtualMachineExceptions;
import com.graduationproject.model.*;
import com.graduationproject.model.interfaces.InterfaceComponent;
import com.graduationproject.model.interfaces.InterfaceComponents;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

public class VirtualBoxComponents implements InterfaceComponents {
    public static final String TYPE_PC = "PC";
    public static final String TYPE_ROUTER = "router";
    public static final String TYPE_SWITCH = "switch";

    @Getter
    @Setter
    private HashMap<Integer, InterfaceComponent> components = new HashMap<Integer, InterfaceComponent>();

    @Getter
    @Setter
    private HashMap<String, Connection> connections = new HashMap<String, Connection>();

    public VirtualBoxComponent getComponent(Integer id) {
        return (VirtualBoxComponent) components.get(id);
    }

    public void moveComponentOnBoard(Integer id, PositionOnBoard positionOnBoard) {
        components.get(id).setPositionOnBoard(positionOnBoard);
    }

    public void changeName(Integer id, String newName) {
        components.get(id).setName(newName);
    }

    public void createComponent(String type, PositionOnBoard positionOnBoard) throws VirtualBoxExceptions, SshExceptions {
        createComponent(type, positionOnBoard, null);
    }

    public void createComponent(String type, Integer id) throws SshExceptions, VirtualBoxExceptions {
        createComponent(type, new PositionOnBoard(), id);
    }

    private void createComponent(String type, PositionOnBoard positionOnBoard, Integer id) throws VirtualBoxExceptions, SshExceptions {
        VirtualBoxComponent newComponent = null;

        if (type.equals(TYPE_PC)) {
            newComponent = new VirtualBoxComponentPC(positionOnBoard);
        } else if (type.equals(TYPE_ROUTER)) {
            newComponent = new VirtualBoxComponentRouter(positionOnBoard);
        } else if (type.equals(TYPE_SWITCH))
            newComponent = new VirtualBoxComponentSwitch(positionOnBoard);

        if (newComponent != null) {
            if (id != null) {
                newComponent.setId(id);
                newComponent.setName(newComponent.getType() + "-" + id);
            }
            components.put(newComponent.getId(), newComponent);
        }
    }


    public boolean containsID(Integer id) {
        return components.containsKey(id);
    }

    public void runComponent(Integer id, boolean run) throws VirtualBoxExceptions, SshExceptions {
        if (components.get(id) instanceof VirtualBoxComponentRunnable)
            ((VirtualBoxComponentRunnable) components.get(id)).runVirtualMachine(run);
    }

    public void removeComponent(Integer id) throws CodedExceptions {
        if (components.get(id) instanceof VirtualBoxComponentRunnable)
            ((VirtualBoxComponentRunnable) components.get(id)).removeVirtualMachine();
        removeConnections(id, true);
        components.remove(id);
    }

    public void removeComponents() throws VirtualBoxExceptions {
        for (Map.Entry<Integer, InterfaceComponent> entry : components.entrySet()) {
            if (entry.getValue() instanceof VirtualBoxComponentPC)
                ((VirtualBoxComponentRunnable) entry.getValue()).removeVirtualMachine();
            if (entry.getValue() instanceof VirtualBoxComponentRunnable)
                ((VirtualBoxComponentRunnable) entry.getValue()).removeVirtualMachine();
        }
        connections.clear();
        components.clear();
    }

    public void addConnection(Connection connection) throws CodedExceptions {
        addConnection(connection, true);
    }

    private void addConnection(Connection connection, boolean addToList) throws CodedExceptions {
        if (containsID(connection.getFromComponent().getId()) && containsID(connection.getToComponent().getId()) && !Objects.equals(connection.getFromComponent().getId(), connection.getToComponent().getId())) {
            String key = connectionKeyGenerator(connection.getFromComponent().getId(), connection.getToComponent().getId());
            if (!connections.containsKey(key) || !addToList) {
                boolean added = true;
                VirtualBoxComponent componentFrom = (VirtualBoxComponent) components.get(connection.getFromComponent().getId());
                VirtualBoxComponent componentTo = (VirtualBoxComponent) components.get(connection.getToComponent().getId());
                if (componentFrom instanceof VirtualBoxComponentRunnable && componentTo instanceof VirtualBoxComponentRunnable) {//component to component - crete connection between two component
                    ((VirtualBoxComponentRunnable) componentFrom).connectNetworkAdapter(connection.getFromComponent().getAdapter(), key);
                    ((VirtualBoxComponentRunnable) componentTo).connectNetworkAdapter(connection.getToComponent().getAdapter(), key);
                } else if (componentFrom instanceof VirtualBoxComponentSwitch && componentTo instanceof VirtualBoxComponentSwitch) {//switch to switch - change connection of one switch to second one
                    LinkedList<Connection> fromConnections = new LinkedList<Connection>();
                    LinkedList<Connection> toConnections = new LinkedList<Connection>();
                    relatedConnections(fromConnections, toConnections, componentFrom.getId(), componentTo.getId());
                    LinkedList<Connection> lessConnections;
                    ConnectionInformation lessConnectionInformation;
                    ConnectionInformation moreConnectionInformation;
                    VirtualBoxComponentSwitch lessConnectionComponent;
                    VirtualBoxComponentSwitch moreConnectionComponent;
                    if (fromConnections.size() > toConnections.size()) {
                        lessConnections = toConnections;
                        lessConnectionInformation = connection.getToComponent();
                        moreConnectionInformation = connection.getFromComponent();
                    } else {
                        lessConnections = fromConnections;
                        lessConnectionInformation = connection.getFromComponent();
                        moreConnectionInformation = connection.getToComponent();
                    }
                    lessConnectionComponent = (VirtualBoxComponentSwitch) components.get(lessConnectionInformation.getId());
                    moreConnectionComponent = (VirtualBoxComponentSwitch) components.get(moreConnectionInformation.getId());
                    if (!lessConnectionComponent.getNetworkName().equals(moreConnectionComponent.getNetworkName())) {//anticycling
                        lessConnectionComponent.setNetworkName(moreConnectionComponent.getNetworkName());
                        for (Connection connectionToChange : lessConnections) {
                            ConnectionInformation stayingConnection = connectionToChange.getOtherByID(lessConnectionInformation.getId());
                            try {
                                addConnection(new Connection(stayingConnection.clone(), moreConnectionInformation.clone(), connectionToChange.getColor()), false);
                            } catch (CloneNotSupportedException e) {
                                throw new CodedExceptions("Can't be cloned", CodedExceptions.CLONE_NOT_SUPPORTED_EXCEPTION);
                            }
                        }
                    }
                } else {//component to switch - give to component connection of switch
                    VirtualBoxComponentSwitch virtualBoxComponentSwitch = null;
                    VirtualBoxComponentRunnable virtualBoxComponentRunnable = null;
                    Adapter adapter = null;
                    if (componentFrom instanceof VirtualBoxComponentSwitch && componentTo instanceof VirtualBoxComponentRunnable) {
                        virtualBoxComponentSwitch = (VirtualBoxComponentSwitch) componentFrom;
                        virtualBoxComponentRunnable = (VirtualBoxComponentRunnable) componentTo;
                        adapter = connection.getToComponent().getAdapter();
                    } else if (componentTo instanceof VirtualBoxComponentSwitch && componentFrom instanceof VirtualBoxComponentRunnable) {
                        virtualBoxComponentSwitch = (VirtualBoxComponentSwitch) componentTo;
                        virtualBoxComponentRunnable = (VirtualBoxComponentRunnable) componentFrom;
                        adapter = connection.getFromComponent().getAdapter();
                    } else {
                        added = false;
                    }
                    if (added) {
                        virtualBoxComponentRunnable.connectNetworkAdapter(adapter, virtualBoxComponentSwitch.getNetworkName());
                    }
                }
                if (added && addToList)
                    connections.put(key, connection);
            }
        }
    }

    public void removeConnection(Connection connection) throws CodedExceptions {
        String key = connectionKeyGenerator(connection.getFromComponent().getId(), connection.getToComponent().getId());
        if (connections.containsKey(key)) {
            Connection removingConnection = connections.get(key);
            VirtualBoxComponent componentFrom = (VirtualBoxComponent) components.get(removingConnection.getFromComponent().getId());
            VirtualBoxComponent componentTo = (VirtualBoxComponent) components.get(removingConnection.getToComponent().getId());
            if (componentFrom instanceof VirtualBoxComponentRunnable)
                ((VirtualBoxComponentRunnable) componentFrom).disconnectNetworkAdapter(removingConnection.getFromComponent().getAdapter());
            if (componentTo instanceof VirtualBoxComponentRunnable)
                ((VirtualBoxComponentRunnable) componentTo).disconnectNetworkAdapter(removingConnection.getToComponent().getAdapter());
            connections.remove(key);
            if (componentFrom instanceof VirtualBoxComponentSwitch && componentTo instanceof VirtualBoxComponentSwitch) {
                LinkedList<Connection> fromConnections = new LinkedList<Connection>();
                LinkedList<Connection> toConnections = new LinkedList<Connection>();
                relatedConnections(fromConnections, toConnections, componentFrom.getId(), componentTo.getId());
                ((VirtualBoxComponentSwitch) componentFrom).setNetworkName(componentFrom.getId().toString());
                ((VirtualBoxComponentSwitch) componentTo).setNetworkName(componentTo.getId().toString());
                try {
                    for (Connection connectionToChange : fromConnections) {
                        addConnection(new Connection(connectionToChange.getFromComponent().clone(), connectionToChange.getToComponent().clone(), connectionToChange.getColor()), false);
                    }
                    for (Connection connectionToChange : toConnections) {
                        addConnection(new Connection(connectionToChange.getFromComponent().clone(), connectionToChange.getToComponent().clone(), connectionToChange.getColor()), false);
                    }
                } catch (CloneNotSupportedException e) {
                    throw new CodedExceptions("Can't be cloned", CodedExceptions.CLONE_NOT_SUPPORTED_EXCEPTION);
                }
            }
        }
    }

    public void colorConnection(Connection connection) {
        String key = connectionKeyGenerator(connection.getFromComponent().getId(), connection.getToComponent().getId());
        if (connections.containsKey(key)) {
            connections.get(key).setColor(connection.getColor());
        }
    }

    private void relatedConnections(LinkedList<Connection> fromConnections, LinkedList<Connection> toConnections, Integer fromId, Integer toId) {
        for (Map.Entry<String, Connection> entry : connections.entrySet()) {
            if (fromId != null && entry.getKey().contains(fromId.toString()))
                fromConnections.add(entry.getValue());
            if (toId != null && entry.getKey().contains(toId.toString()))
                toConnections.add(entry.getValue());
        }
    }

    public void openVirtualMachine(Integer id) throws VirtualBoxExceptions {
        InterfaceComponent component = components.get(id);
        if (component instanceof VirtualBoxComponentRunnable)
            ((VirtualBoxComponentRunnable) component).openVirtualMachine();
    }

    public void openSshTerminal(Integer id) throws CodedExceptions {
        InterfaceComponent component = components.get(id);
        if (component instanceof VirtualBoxComponentRunnable)
            ((VirtualBoxComponentRunnable) component).openSshTerminal();
    }

    public void removeConnections(Integer id, boolean force) throws CodedExceptions {
        LinkedList<String> toRemove = new LinkedList<String>();
        for (Map.Entry<String, Connection> entry : connections.entrySet()) {
            if (entry.getKey().contains(id.toString()))
                toRemove.add(entry.getKey());
        }
        for (String key : toRemove) {
            if (force) {
                connections.remove(key);
            } else
                removeConnection(connections.get(key));
        }
    }

    public Adapters getComponentAdapters(Integer id) throws VirtualBoxExceptions {
        return ((VirtualBoxComponent) components.get(id)).getAdapters();
    }

    private String connectionKeyGenerator(Integer from, Integer to) {
        if (from > to) {
            return from.toString() + "-" + to.toString();
        } else {
            return to.toString() + "-" + from.toString();
        }
    }

    public void addIP(Integer id, String ip, String link) throws VirtualBoxExceptions, SshExceptions, VirtualMachineExceptions {
        InterfaceComponent component = components.get(id);
        if (component instanceof VirtualBoxComponentPC)
            ((VirtualBoxComponentPC) component).addIP(ip, link);
    }

    public void removeIP(Integer id, String ip, String link) throws VirtualBoxExceptions, SshExceptions, VirtualMachineExceptions {
        InterfaceComponent component = components.get(id);
        if (component instanceof VirtualBoxComponentPC)
            ((VirtualBoxComponentPC) component).removeIP(ip, link);
    }

    public void change(Components components) throws CodedExceptions {
        removeComponents();
        for (Map.Entry<Integer, InterfaceComponent> entry : components.getComponents().entrySet()) {
            Component component = (Component) entry.getValue();
            createComponent(component.getType(), component.getId());
            VirtualBoxComponent newComponent = (VirtualBoxComponent) this.components.get(component.getId());
            newComponent.setName(component.getName());
            newComponent.setPositionOnBoard(component.getPositionOnBoard());

            if (newComponent instanceof VirtualBoxComponentRunnable) {
                VirtualBoxComponentRunnable newRunnableComponent = (VirtualBoxComponentRunnable) newComponent;
                newRunnableComponent.removeNetworkAdapter(2);//TODO maybe remove it automatic
                if (newComponent instanceof VirtualBoxComponentRouter)
                    newRunnableComponent.removeNetworkAdapter(3);
                for (Map.Entry<String, NetworkInformation> entry1 : component.getNetworksInformation().getNetworksInformation().entrySet()) {
                    if (entry1.getValue().getAdapter().getDisabled() != null && !entry1.getValue().getAdapter().getDisabled())
                        VirtualBoxStaticEvents.setNetworkAdapterWithoutParameters(newRunnableComponent.getVirtualMachineName(), entry1.getValue().getAdapter().getNumber(), "null");
                }
                newRunnableComponent.loadNetworksInformation();
            }

            if (component.isRunning())
                runComponent(newComponent.getId(), component.isRunning());

            if (newComponent instanceof VirtualBoxComponentPC) {
                VirtualBoxComponentPC newPCComponent = (VirtualBoxComponentPC) newComponent;
                for (Map.Entry<String, NetworkInformation> entry1 : component.getNetworksInformation().getNetworksInformation().entrySet()) {
                    if (entry1.getValue().getAdapter().getDisabled() != null && !entry1.getValue().getAdapter().getDisabled()) {
                        for (String ip : entry1.getValue().getIpv4()) {
                            newPCComponent.addIP(ip, entry1.getValue().getLink());
                        }
                        for (String ip : entry1.getValue().getIpv6()) {
                            newPCComponent.addIP(ip, entry1.getValue().getLink());
                        }
                    }
                }
            }
        }
        for (Map.Entry<String, Connection> entry : components.getConnections().entrySet()) {
            Connection connection = entry.getValue();
            addConnection(connection);
        }
    }

    public String getType(Integer id) {
        return components.get(id).getType();
    }

    public String getIp(Integer id) {
        InterfaceComponent virtualBoxComponentPC = components.get(id);
        if (virtualBoxComponentPC instanceof VirtualBoxComponentRunnable) {
            return ((VirtualBoxComponentRunnable) virtualBoxComponentPC).getIp();
        } else {
            return null;
        }
    }

    public Boolean isRunning(Integer id) throws VirtualBoxExceptions, SshExceptions {
        return components.get(id).isRunning();
    }

    /**
     * does not refresh connections
     */
    public void refresh() throws VirtualBoxExceptions, SshExceptions {
        for (Map.Entry<Integer, InterfaceComponent> entry : components.entrySet()) {
            refreshRunningState(entry.getKey());
        }
    }

    public void refreshRunningState(Integer id) throws SshExceptions, VirtualBoxExceptions {
        components.get(id).isRunning();
    }

    public void refreshNetworksInformation(Integer id) throws SshExceptions, VirtualBoxExceptions {
        InterfaceComponent virtualBoxComponentPC = components.get(id);
        if (virtualBoxComponentPC instanceof VirtualBoxComponentRunnable)
            ((VirtualBoxComponentRunnable) virtualBoxComponentPC).loadNetworksInformation();

    }

    public void addNetworkAdapter(Integer id) throws VirtualBoxExceptions, SshExceptions {
        InterfaceComponent virtualBoxComponent = components.get(id);
        if (virtualBoxComponent instanceof VirtualBoxComponentRunnable)
            ((VirtualBoxComponentRunnable) virtualBoxComponent).addNetworkAdapter();
    }

    public void removeNetworkAdapter(Integer id, Integer adapterNumber) throws CodedExceptions {
        InterfaceComponent virtualBoxComponent = components.get(id);
        if (virtualBoxComponent instanceof VirtualBoxComponentRunnable) {
            LinkedList<Connection> relatedConnections = new LinkedList<>();
            relatedConnections(relatedConnections, relatedConnections, id, id);
            for (Connection connection : relatedConnections) {
                ConnectionInformation from = connection.getFromComponent();
                ConnectionInformation to = connection.getToComponent();
                if (Objects.equals(from.getId(), id) && Objects.equals(from.getAdapter().getNumber(), adapterNumber))
                    removeConnection(connection);
                if (Objects.equals(to.getId(), id) && Objects.equals(to.getAdapter().getNumber(), adapterNumber))
                    removeConnection(connection);
            }
            ((VirtualBoxComponentRunnable) virtualBoxComponent).removeNetworkAdapter(adapterNumber);
        }
    }

    public LinkedList<String> getTCPdump(Integer id, String networkLink) {
        InterfaceComponent virtualBoxComponentPC = components.get(id);
        if (virtualBoxComponentPC instanceof VirtualBoxComponentPC) {
            return ((VirtualBoxComponentPC) virtualBoxComponentPC).getTCPdump(networkLink);
        } else {
            return null;
        }
    }

    public void addNewTCPdumpRowAction(Integer id, String networkLink, StringSender action) {
        InterfaceComponent virtualBoxComponentPC = components.get(id);
        if (virtualBoxComponentPC instanceof VirtualBoxComponentPC)
            ((VirtualBoxComponentPC) virtualBoxComponentPC).addNewTCPdumpRowAction(networkLink, action);

    }

}
