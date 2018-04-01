import React from 'react';
import ComponentsVisages from "./ComponentsVisages.jsx"

export default class ComponentToCreate extends React.Component {
    constructor(pros){
        super(pros);
        this.onClickHandle = this.onClickHandle.bind(this);
    }

    onClickHandle(){
        this.props.createComponent(this.props.componentType);
    }

    render() {
        return (
            <button className="componentToCreate" onClick={this.onClickHandle}>
                <ComponentsVisages componentType={this.props.componentType} componentName={this.props.componentType}/>
            </button>
        );
    }
}