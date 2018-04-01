import React from 'react';
import ComponentToCreate from "./ComponentToCreate.jsx";
import {ComponentTypes} from './Constants.jsx';
import BoardSize from "./BoardSize.jsx"
import ConnectionDriver from "./ConnectionDriver.jsx"

export default class SidePanel extends React.Component {
    render() {
        return (
            <div className="sidePanel">
                <div>
                    <div>
                        {Object.entries(ComponentTypes).map((componentType) => <ComponentToCreate key={componentType[1]}
                                                                                                  componentType={componentType[1]}
                                                                                                  createComponent={this.props.createComponent}/>)}
                    </div>
                    <hr/>
                    <BoardSize changeBoardSize={this.props.changeBoardSize} boardWidth={this.props.boardWidth}
                               boardHeight={this.props.boardHeight} minBoardWidth={this.props.minBoardWidth}
                               minBoardHeight={this.props.minBoardHeight}/>
                    <hr/>
                    <ConnectionDriver
                        addConnectionState={this.props.addConnectionState}
                        removeConnectionState={this.props.removeConnectionState}
                        colorConnectionState={this.props.colorConnectionState}
                        changeAddConnectionState={this.props.changeAddConnectionState}
                        changeRemoveConnectionState={this.props.changeRemoveConnectionState}
                        changeColorConnectionState={this.props.changeColorConnectionState}
                        connectionColor={this.props.connectionColor}
                        setConnectionColor={this.props.setConnectionColor}/>
                    <hr/>
                    <p>
                        <input type="button" onClick={this.props.refresh} value="Refresh"/>
                    </p>
                    <hr/>
                    <p>
                        <a href={"/export"} target="_blank">Export</a>
                    </p>
                    <div style={{display: "inline"}}>
                        <label>Import:</label>
                        <iframe src="/import" className={"importIframe"}/>
                    </div>
                </div>
            </div>
        );
    }
}