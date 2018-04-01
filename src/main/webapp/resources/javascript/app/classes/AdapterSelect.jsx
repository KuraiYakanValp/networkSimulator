import React from 'react';
import PopUpWindow from "./PopUpWindow.jsx";

export default class AdapterSelect extends React.Component {
    constructor(pros) {
        super(pros);
        this.onClickHandle = this.onClickHandle.bind(this);
        this.onChangeHandle = this.onChangeHandle.bind(this);
        this.state = {
            value: "null"
        }
    }

    onClickHandle() {
        if (this.state.value == "null") {
            this.props.cancel();
        } else {
            let adapterToSend = null;
            this.props.adapters.forEach(adapter => {
                if (adapter.number == this.state.value) {
                    adapterToSend = adapter;
                }
            });
            this.props.addConnection(this.props.componentId, adapterToSend);
        }
    }

    onChangeHandle(event) {
        this.setState({value: event.target.value});
    }

    render() {
        return (
            <PopUpWindow>
                <select value={this.state.value} onChange={this.onChangeHandle}>
                    <option value={"null"}>Cancel</option>
                    {this.props.adapters.map(adapter => {
                        return (
                            <option
                                disabled={this.props.removeConnectionState ? adapter.disabled : (adapter.disabled || adapter.set)}
                                key={adapter.number}
                                value={adapter.number}>Adapter {adapter.number} - {adapter.mac}</option>
                        );
                    })}
                </select>
                <input style={{marginTop: "1em"}} type="button" value="OK" onClick={this.onClickHandle}/>
            </PopUpWindow>
        );
    }
}