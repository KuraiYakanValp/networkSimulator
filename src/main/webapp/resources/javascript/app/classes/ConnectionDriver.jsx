import React from 'react';

export default class ConnectionDriver extends React.Component {
    constructor(pros) {
        super(pros);
        this.addButtonHandle = this.addButtonHandle.bind(this);
        this.removeButtonHandle = this.removeButtonHandle.bind(this);
        this.colorButtonHandle = this.colorButtonHandle.bind(this);
        this.colorChangeHandle = this.colorChangeHandle.bind(this);
    }

    addButtonHandle() {
        if (!this.props.addConnectionState) {
            this.props.changeRemoveConnectionState(false);
            this.props.changeColorConnectionState(false);
        }
        this.props.changeAddConnectionState(!this.props.addConnectionState);
    }

    removeButtonHandle() {
        if (!this.props.removeConnectionState) {
            this.props.changeAddConnectionState(false);
            this.props.changeColorConnectionState(false);
        }
        this.props.changeRemoveConnectionState(!this.props.removeConnectionState);
    }

    colorButtonHandle() {
        if (!this.props.colorConnectionState) {
            this.props.changeAddConnectionState(false);
            this.props.changeRemoveConnectionState(false);
        }
        this.props.changeColorConnectionState(!this.props.colorConnectionState);
    }

    colorChangeHandle(e) {
        this.props.setConnectionColor(e.target.value);
    }

    render() {
        return (
            <div>
                <input type={"color"} value={this.props.connectionColor} onChange={this.colorChangeHandle}/>
                <p>
                    <input type="checkbox" checked={this.props.addConnectionState} onChange={this.addButtonHandle}/>
                    <label>Add connection</label>
                </p>
                <p>
                    <input type="checkbox" checked={this.props.colorConnectionState} onChange={this.colorButtonHandle}/>
                    <label>Change connection color</label>
                </p>
                <p>
                    <input type="checkbox" checked={this.props.removeConnectionState}
                           onChange={this.removeButtonHandle}/>
                    <label>Remove connection</label>
                </p>
            </div>
        );
    }
}