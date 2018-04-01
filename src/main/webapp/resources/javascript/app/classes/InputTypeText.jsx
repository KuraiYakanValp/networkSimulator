import React from 'react';

export default class InputTypeText extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            actualValue: ""
        };
        this.changeHandle = this.changeHandle.bind(this);
        this.blurHandle = this.blurHandle.bind(this);
    }

    valueParser(incoming){
        return incoming===undefined||incoming===null?"":incoming;
    }

    componentWillReceiveProps(next) {
        this.setState((state) => ({actualValue:this.valueParser(next.value)}));
    }

    changeHandle(event) {
        this.setState({actualValue: event.target.value});
    }

    blurHandle() {
        this.props.changed(this.state.actualValue,this.props.changeParam);
    }

    render() {
        return (<input type={"text"} disabled={this.props.disabled !== undefined && this.props.disabled}
                        value={this.state.actualValue}
                       onChange={this.changeHandle} onBlur={this.blurHandle}/>
        );
    }
}