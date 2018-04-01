import React from 'react';
import InputTypeText from './InputTypeText.jsx';
import update from 'immutability-helper';
import {ComponentTypes} from "./Constants.jsx";
import NetworksInformation from "./NetworksInformation.jsx";

export default class ComponentPage extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            component: {},
            tcpdump: {},
            changedComponent: {},
            websocketSubscription: [],
            tcpdumpWebsocketSubscription: {},
            connectTimeout: null
        };
        this.connectToWebsocket = this.connectToWebsocket.bind(this);
        this.changeNameHandle = this.changeNameHandle.bind(this);
        this.run = this.run.bind(this);
        this.openVirtualMachine = this.openVirtualMachine.bind(this);
        this.openSshTerminal = this.openSshTerminal.bind(this);
        this.addIP = this.addIP.bind(this);
        this.removeIP = this.removeIP.bind(this);
        this.addTCPdumpWebsocket = this.addTCPdumpWebsocket.bind(this);
        this.controlTCPdumpWebsockets = this.controlTCPdumpWebsockets.bind(this);
        this.removeAllTCPdumcWebsockets = this.removeAllTCPdumcWebsockets.bind(this);
        this.openLuCi = this.openLuCi.bind(this);
        this.refreshNetworksInformation = this.refreshNetworksInformation.bind(this);
        this.addNetworkAdapter = this.addNetworkAdapter.bind(this);
        this.removeNetworkAdapter = this.removeNetworkAdapter.bind(this);
    }


    connectToWebsocket() {
        if (this.props.stompClient.connected) {
            let websocketSubscription = [];

            websocketSubscription.push(this.props.stompClient.subscribe('/userToServer/components', function (message) {
                let data = JSON.parse(message.body);
                this.setState((state) => ({component: data.components[this.props.routeProps.match.params.id]}));
                this.setState((state) => ({changedComponent: this.state.component}));
                if (this.state.component.type !== undefined && this.state.component.type === ComponentTypes.PC)
                    this.controlTCPdumpWebsockets();
            }.bind(this)));

            websocketSubscription.push(this.props.stompClient.subscribe('/serverToUsers/components', function (message) {
                let data = JSON.parse(message.body);
                this.setState((state) => ({component: data.components[this.props.routeProps.match.params.id]}));
                this.setState((state) => ({changedComponent: this.state.component}));
                if (this.state.component.type !== undefined && this.state.component.type === ComponentTypes.PC)
                    this.controlTCPdumpWebsockets();
            }.bind(this)));

            this.setState(
                update(this.state, {
                    websocketSubscription: {
                        $push: websocketSubscription
                    }
                })
            );
            if (this.state.connectTimeout != null)
                clearInterval(this.state.connectTimeout);
        }
    }

    addNetworkAdapter() {
        this.props.stompClient.send("/userToServer/component/" + this.state.component.id + "/addNetworkAdapter", {}, {});
    }

    removeNetworkAdapter(adapterNumber) {
        this.props.stompClient.send("/userToServer/component/" + this.state.component.id + "/removeNetworkAdapter", {}, adapterNumber);
    }

    controlTCPdumpWebsockets() {
        if (this.state.component.running) {
            let add = [];
            let remove = [];
            ComponentPage.differenceKey(this.state.component.networksInformation.networksInformation, this.state.tcpdumpWebsocketSubscription, add, remove);
            for (let key in add) {
                let networkInformation = this.state.component.networksInformation.networksInformation[add[key]];
                if (networkInformation.adapter.disabled !== null && !networkInformation.adapter.disabled)
                    this.addTCPdumpWebsocket(add[key]);
            }
            if (remove.length > 0) {
                for (let key in remove) {
                    this.state.tcpdumpWebsocketSubscription[remove[key]]["subscribe"].unsubscribe();
                    this.state.tcpdumpWebsocketSubscription[remove[key]]["normal"].unsubscribe();
                }
                this.setState(
                    update(this.state, {
                        tcpdump: {
                            $unset: remove
                        }
                    })
                );
                this.setState(
                    update(this.state, {
                        tcpdumpWebsocketSubscription: {
                            $unset: remove
                        }
                    })
                );
            }
        } else {
            this.removeAllTCPdumcWebsockets();
        }
    }

    openLuCi() {
        let win = window.open(`/component/${this.state.component.id}/LuCi`, '_blank');
        win.focus();
    }

    addTCPdumpWebsocket(networkLink) {
        if (this.state.tcpdumpWebsocketSubscription[networkLink] === undefined) {
            let tcpdumpWebsocketSubscription = {};
            tcpdumpWebsocketSubscription[networkLink] = {};
            tcpdumpWebsocketSubscription[networkLink]["subscribe"] = (this.props.stompClient.subscribe('/userToServer/component/' + this.state.component.id + '/' + networkLink + '/tcpdump', function (message) {
                let data = JSON.parse(message.body);
                let obj = {};
                obj[networkLink] = data;
                this.setState(
                    update(this.state, {
                        tcpdump: {
                            $merge: obj
                        }
                    })
                );
            }.bind(this)));

            tcpdumpWebsocketSubscription[networkLink]["normal"] = (this.props.stompClient.subscribe('/serverToUsers/component/' + this.state.component.id + '/' + networkLink + '/tcpdump', function (message) {
                let data = message.body;
                let obj = {tcpdump: {}};
                obj.tcpdump[networkLink] = {$push: [data]};
                this.setState(update(this.state, obj));
            }.bind(this)));


            this.setState(
                update(this.state, {
                    tcpdumpWebsocketSubscription: {
                        $merge: tcpdumpWebsocketSubscription
                    }
                })
            );
        }
    }

    removeAllTCPdumcWebsockets() {
        for (let key in this.state.tcpdumpWebsocketSubscription) {
            this.state.tcpdumpWebsocketSubscription[key]["subscribe"].unsubscribe();
            this.state.tcpdumpWebsocketSubscription[key]["normal"].unsubscribe();
        }
        this.setState((state) => ({tcpdumpWebsocketSubscription: {}}));
        this.setState((state) => ({tcpdump: {}}));
    }

    componentDidMount() {
        this.setState((state) => ({
            connectTimeout: setInterval(this.connectToWebsocket, 100)
        }));
    }

    componentWillUnmount() {
        this.state.websocketSubscription.forEach(function (element) {
            element.unsubscribe();
        });
        this.setState((state) => ({websocketSubscription: []}));
        if (this.state.connectTimeout != null)
            clearInterval(this.state.connectTimeout);

        this.removeAllTCPdumcWebsockets();
    }

    changeNameHandle(value) {
        this.props.stompClient.send("/userToServer/component/" + this.state.component.id + "/changeName", {}, value);
    }

    run() {
        this.props.stompClient.send("/userToServer/component/" + this.state.component.id + "/run", {}, !this.state.component.running);
    }

    openVirtualMachine() {
        this.props.stompClient.send("/userToServer/component/" + this.state.component.id + "/openVirtualMachine", {}, {});
    }

    openSshTerminal() {
        this.props.stompClient.send("/userToServer/component/" + this.state.component.id + "/openSshTerminal", {}, {});
    }

    refreshNetworksInformation() {
        this.props.stompClient.send("/userToServer/component/" + this.state.component.id + "/refreshNetworksInformation", {}, {});
    }

    addIP(ip, link) {
        this.props.stompClient.send("/userToServer/component/" + this.state.component.id + "/addIP", {}, JSON.stringify({
            'ip': ip,
            'link': link
        }));
    }

    removeIP(ip, link) {
        this.props.stompClient.send("/userToServer/component/" + this.state.component.id + "/removeIP", {}, JSON.stringify({
            'ip': ip,
            'link': link
        }));
    }

    render() {
        if (this.state.component.type === undefined)
            return (<div/>);

        let openVirtualMachine = null;
        if (this.state.component.running) {
            let router = null;
            if (this.state.component.type === ComponentTypes.ROUTER) {
                router = <input type="button" value="Open LuCi" onClick={this.openLuCi}/>
            }

            openVirtualMachine = <span>
                <input type="button" value="Open virtual machine" onClick={this.openVirtualMachine}/>
                <input type="button" value="Open virtual ssh terminal" onClick={this.openSshTerminal}/>
                {router}
            </span>;
        }

        let runnableControl = null;
        if (this.state.component.type !== ComponentTypes.SWITCH) {
            runnableControl = (
                <div>
                    <input type="button" value={this.state.component.running ? "Stop" : "Start"} onClick={this.run}/>
                    {openVirtualMachine}
                </div>);
        }

        let newAdapter = null;
        if (this.state.component.type !== ComponentTypes.SWITCH && this.state.component.running === false) {
            newAdapter = <div><input type="button" value="Add adapter" onClick={this.addNetworkAdapter}/></div>;
        }

        let networksInformation = null;
        if (this.state.component.type !== ComponentTypes.SWITCH) {
            networksInformation = <NetworksInformation
                networksInformation={this.state.component.networksInformation.networksInformation}
                running={this.state.component.running}
                addIP={this.addIP}
                removeIP={this.removeIP}
                tcpdump={this.state.tcpdump}
                refresh={this.refreshNetworksInformation}
                removeNetworkAdapter={this.removeNetworkAdapter}
                deviceType={this.state.component.type}
            />;
        }
        return (
            <div>
                <div className={"componentPageOuter"}/>
                <section className={"componentPageInner"}>
                    <h1>{this.state.component.name}</h1>
                    {runnableControl}
                    <br/>
                    <label>Name:</label>
                    <InputTypeText value={this.state.component.name} changed={this.changeNameHandle}/>
                    {newAdapter}
                    {networksInformation}
                </section>
            </div>
        );
    }

    static differenceKey(a, b, diffInA, diffInB) {
        let same = [];
        for (let keyA in a) {
            for (let keyB in b) {
                if (keyA === keyB) {
                    same.push(keyA);
                    break;
                }
            }
        }
        for (let keyA in a) {
            if (!same.includes(keyA))
                diffInA.push(keyA);
        }
        for (let keyB in b) {
            if (!same.includes(keyB))
                diffInB.push(keyB);
        }
    }
}