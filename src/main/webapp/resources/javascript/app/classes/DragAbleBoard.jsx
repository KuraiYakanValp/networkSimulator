import React from 'react';
import HTML5Backend from 'react-dnd-html5-backend';
import {DragGroups} from "./Constants.jsx";
import {DragDropContext, DropTarget} from 'react-dnd';
import ComponentOnBoard from "./ComponentOnBoard.jsx"
import LineTo from 'react-lineto';

const boxTarget = {
    drop(props, monitor, component) {
        const item = monitor.getItem();
        const delta = monitor.getDifferenceFromInitialOffset();
        const left = Math.round(item.left + delta.x);
        const top = Math.round(item.top + delta.y);
        component.props.moveComponentOnBoard(item.id, left, top);
    }
};

function collect(connect, monitor) {
    return {
        connectDropTarget: connect.dropTarget(),
        isOver: monitor.isOver()
    };
}

@DragDropContext(HTML5Backend)
@DropTarget(DragGroups.BOARD, boxTarget, collect)
export default class DragAbleBoard extends React.Component {

    render() {
        return this.props.connectDropTarget(
            <div className={"dragAbleBoardOuter"}>
                <div className="dragAbleBoard" style={{width: this.props.width, height: this.props.height}}>
                    {Object.keys(this.props.componentsOnBoard).map(key => {
                        let componentOnBoard = this.props.componentsOnBoard[key];
                        return (
                            <ComponentOnBoard
                                key={key}
                                id={key}
                                properties={componentOnBoard}
                                runComponent={this.props.runComponent}
                                removeComponent={this.props.removeComponent}
                                componentClicked={this.props.componentClicked}
                                selected={key == this.props.selectedComponentId}
                            />
                        )
                    })}
                    {Object.keys(this.props.connectionsOfComponents).map(key => {
                        let connectionOfComponents = this.props.connectionsOfComponents[key];
                        return (
                            <LineTo
                                zIndex={1}
                                from={"componentId-" + connectionOfComponents.fromComponent.id}
                                to={"componentId-" + connectionOfComponents.toComponent.id}
                                key={key}
                                borderColor={connectionOfComponents.color}
                            />
                        )
                    })}
                </div>
            </div>,
        )
    }
}