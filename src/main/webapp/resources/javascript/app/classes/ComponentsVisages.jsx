import React from 'react';
import {ComponentTypes} from "./Constants.jsx";
import classNames from 'classnames';

export default class ComponentsVisages extends React.Component {
    constructor(pros) {
        super(pros);
        this.onClickHandle = this.onClickHandle.bind(this);
    }

    onClickHandle() {
        this.props.createComponent(this.props.componentType);
    }

    render() {
        let imageSrc = "";
        switch (this.props.componentType) {
            case ComponentTypes.PC:
                imageSrc = "resources/images/pc.png";
                break;
            case ComponentTypes.ROUTER:
                imageSrc = "resources/images/router.png";
                break;
            case ComponentTypes.SWITCH:
                imageSrc = "resources/images/switch.png";
        }
        let classes=classNames({componentVisage:true,selectedComponent:this.props.selected});
        return (
            <div style={{paddingBottom: "1.2em"}}>
                <img className={classes} src={imageSrc}/>
                <br/>
                <label style={{position: "absolute"}}>{this.props.componentName}</label>
            </div>
        );
    }
}