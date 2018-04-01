import React from 'react';
import InputTypeText from "./InputTypeText.jsx";
import TCPdump from "./TCPdump.jsx";
import {ComponentTypes} from "./Constants.jsx";

export default class NetworksInformation extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            newIP: ""
        };
        this.addIP = this.addIP.bind(this);
        this.removeIP = this.removeIP.bind(this);
        this.ipMapper = this.ipMapper.bind(this);
    }

    addIP(ip, link) {
        let splitedIP = ip.split("\/");
        let controlIP = /((^\s*((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))\s*$)|(^\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?\s*$))/;
        if ((splitedIP.length === 1 && controlIP.test(ip)) || (splitedIP.length === 2 && controlIP.test(splitedIP[0]) && splitedIP[1] <= 32 && splitedIP[1] >= 0 && /^\+?(0|[1-9]\d*)$/.test(splitedIP[1]))) {
            this.props.addIP(ip, link);
            this.setState((state) => ({newIP: ""}));
        } else if (ip !== "") {
            alert("Invalid IP");
        }
    }

    removeIP(ip, link) {
        this.props.removeIP(ip, link);
    }

    ipMapper(ip, link) {
        return (<li key={ip}>
            <span>{ip}</span>
            <button className={"removeButton"} type="button" onClick={() => {
                this.removeIP(ip, link)
            }}><img
                src="/resources/images/error.png"/></button>
        </li> );
    }

    render() {
        return (
            <div>
                <input type="button" value="Refresh networks information" onClick={this.props.refresh}/>
                <table>
                    <thead>
                    <tr>
                        <th>Adapter</th>
                        {this.props.deviceType === ComponentTypes.ROUTER ? null : <th>Link</th>}
                        <th>Mac</th>
                        {this.props.deviceType === ComponentTypes.ROUTER ? null : <th>IPv4</th>}
                        {this.props.deviceType === ComponentTypes.ROUTER ? null : <th>IPv6</th>}
                        {this.props.deviceType === ComponentTypes.ROUTER ? null : <th>Add IP</th>}
                        <th>Connected</th>
                        {this.props.running ? null : <th>Remove adapter</th>}
                    </tr>
                    </thead>
                    {Object.keys(this.props.networksInformation).map(key => {
                        let tcpdump = null;
                        if (this.props.tcpdump[key] !== undefined) {
                            tcpdump = (<tr>
                                <td colSpan={7}>
                                    <TCPdump tcpdump={this.props.tcpdump[key]}/>
                                </td>
                            </tr>);
                        }
                        let networkInformation = this.props.networksInformation[key];
                        if (networkInformation.adapter.disabled !== null && !networkInformation.adapter.disabled)
                            return (
                                <tbody key={key}>
                                <tr>
                                    <td>{networkInformation.adapter.number}</td>
                                    {this.props.deviceType === ComponentTypes.ROUTER ? null :
                                        <td>{networkInformation.link}</td>}
                                    <td>{networkInformation.adapter.mac}</td>
                                    {this.props.deviceType === ComponentTypes.ROUTER ? null : <td>
                                        <ul>{networkInformation.ipv4.map(ip => {
                                            return this.ipMapper(ip, networkInformation.link)
                                        })}</ul>
                                    </td>}
                                    {this.props.deviceType === ComponentTypes.ROUTER ? null : <td>
                                        <ul>{networkInformation.ipv6.map(ip => {
                                            this.ipMapper(ip, networkInformation.link)
                                        })}</ul>
                                    </td>}
                                    {this.props.deviceType === ComponentTypes.ROUTER ? null : <td>
                                        <InputTypeText disabled={!this.props.running} value={this.state.newIP}
                                                       changeParam={networkInformation.link} changed={this.addIP}/>
                                    </td>}
                                    <td>{networkInformation.adapter.set ? "Yes" : "No"}</td>
                                    <td>{this.props.running ? null :
                                        <button className={"removeButton"} type="button" onClick={() => {
                                            this.props.removeNetworkAdapter(networkInformation.adapter.number)
                                        }}><img src="/resources/images/error.png"/></button>}</td>
                                </tr>
                                {tcpdump}

                                </tbody>
                            )
                    })}
                </table>
            </div>
        );
    }
}