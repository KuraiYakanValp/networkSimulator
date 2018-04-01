import React from 'react'
import {DragSource} from 'react-dnd'
import {ComponentTypes, DragGroups} from './Constants.jsx'
import ComponentsVisages from "./ComponentsVisages.jsx";
import {Redirect} from "react-router-dom";
import {ContextMenu, ContextMenuTrigger, MenuItem} from "react-contextmenu";


const boxSource = {
    beginDrag(props) {
        const {id, properties} = props;
        const {left, top} = properties.positionOnBoard;
        return {id, left, top}
    }
};

function collect(connect, monitor) {
    return {
        connectDragSource: connect.dragSource(),
        isDragging: monitor.isDragging()
    }
}


@DragSource(DragGroups.BOARD, boxSource, collect)
export default class ComponentOnBoard extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            redirect: false
        };
        this.onDoubleClickHandle = this.onDoubleClickHandle.bind(this);
        this.openInNewTab = this.openInNewTab.bind(this);
        this.runComponent = this.runComponent.bind(this);
        this.removeComponent = this.removeComponent.bind(this);
        this.onClickHandle = this.onClickHandle.bind(this);
        this.openLuCi = this.openLuCi.bind(this);
    }

    onDoubleClickHandle() {
        this.setState({redirect: true});
    }

    onClickHandle() {
        this.props.componentClicked(this.props.properties.id);
    }

    openInNewTab() {
        let win = window.open(`/component/${this.props.properties.id}`, '_blank');
        win.focus();
    }

    openLuCi() {
        let win = window.open(`/component/${this.props.properties.id}/LuCi`, '_blank');
        win.focus();
    }

    runComponent() {
        this.props.runComponent(this.props.properties.id, !this.props.properties.running);
    }

    removeComponent() {
        this.props.removeComponent(this.props.properties.id);
    }

    render() {
        const {
            properties,
            connectDragSource
        } = this.props;
        const left = properties.positionOnBoard.left;
        const top = properties.positionOnBoard.top;

        let router = null;
        if (properties.type === ComponentTypes.ROUTER && properties.running) {
            router = <MenuItem onClick={this.openLuCi}>Open LuCi</MenuItem>
        }

        let runnable = null;
        if (properties.type !== ComponentTypes.SWITCH) {
            runnable = <span>
                {router}
                <MenuItem divider/>
                <MenuItem onClick={this.runComponent}>{!properties.running ? "Start" : "Power off"}</MenuItem>
            </span>
        }

        if (this.state.redirect) {
            return <Redirect push to={`/component/${properties.id}`} target="_blank"/>;
        }

        return connectDragSource(
            <div style={{position: "absolute", left, top, zIndex: 5}} className={"componentId-" + properties.id}
                 onClick={this.onClickHandle} onDoubleClick={this.onDoubleClickHandle}>
                <div className="runningDetector" style={{backgroundColor: properties.running ? "green" : "red"}}/>
                <ContextMenuTrigger id={"menuTrigger" + properties.id} holdToDisplay={-1}>
                    <ComponentsVisages componentType={properties.type} componentName={properties.name}
                                       selected={this.props.selected}/>
                </ContextMenuTrigger>
                <ContextMenu id={"menuTrigger" + properties.id}>
                    <MenuItem onClick={this.openInNewTab}>Open in New Tab</MenuItem>
                    {runnable}
                    <MenuItem divider/>
                    <MenuItem onClick={this.removeComponent}>Remove</MenuItem>
                </ContextMenu>
            </div>
        )
    }
}