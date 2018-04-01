import React from 'react';
import DragAbleBoard from "./DragAbleBoard.jsx";
import SidePanel from "./SidePanel.jsx";
import axios from 'axios';
import AdapterSelect from "./AdapterSelect.jsx";
import update from 'immutability-helper';

export default class BoardPage extends React.Component {

    constructor(pros) {
        super(pros);
        this.createComponent = this.createComponent.bind(this);
        this.moveComponentOnBoard = this.moveComponentOnBoard.bind(this);
        this.changeBoardSize = this.changeBoardSize.bind(this);
        this.runComponent = this.runComponent.bind(this);
        this.connectToWebsocket = this.connectToWebsocket.bind(this);
        this.removeComponent = this.removeComponent.bind(this);
        this.changeAddConnectionState = this.changeAddConnectionState.bind(this);
        this.changeRemoveConnectionState = this.changeRemoveConnectionState.bind(this);
        this.changeColorConnectionState = this.changeColorConnectionState.bind(this);
        this.componentClicked = this.componentClicked.bind(this);
        this.addConnection = this.addConnection.bind(this);
        this.setConnectionColor = this.setConnectionColor.bind(this);
        this.cancelAdapterSelect = this.cancelAdapterSelect.bind(this);
        this.refresh = this.refresh.bind(this);
        this.state = {
            componentsOnBoard: {},
            connectionsOfComponents: {},
            boardWidth: 100,
            boardHeight: 100,
            defaultMinSize: 100,
            minBoardWidth: 100,
            minBoardHeight: 100,
            websocketSubscription: [],
            connectTimeout: null,
            addConnectionState: false,
            removeConnectionState: false,
            colorConnectionState: false,
            connectionFrom: null,
            connectionColor: "#FF0000",
            adapterSetting: {show: false, adapters: null, componentId: null}
        };
    }

    connectToWebsocket() {
        if (this.props.stompClient.connected) {
            let websocketSubscription = [];
            websocketSubscription.push(this.props.stompClient.subscribe('/userToServer/components', function (message) {
                let data = JSON.parse(message.body);
                this.setState((state) => ({componentsOnBoard: data.components}));
                this.setState((state) => ({connectionsOfComponents: data.connections}));
            }.bind(this)));

            websocketSubscription.push(this.props.stompClient.subscribe('/serverToUsers/components', function (message) {
                let data = JSON.parse(message.body);

                this.setState((state) => ({componentsOnBoard: data.components}));
                this.setState((state) => ({connectionsOfComponents: data.connections}));

                let components = this.state.componentsOnBoard;
                let minWidth = this.state.defaultMinSize;
                let minHeight = this.state.defaultMinSize;
                Object.keys(components).forEach(function (key) {
                    if (components[key].positionOnBoard.left > minWidth)
                        minWidth = components[key].positionOnBoard.left;
                    if (components[key].positionOnBoard.top > minHeight)
                        minHeight = components[key].positionOnBoard.top;
                });
                this.changeBoardMinSize(minWidth, minHeight);
            }.bind(this)));


            websocketSubscription.push(this.props.stompClient.subscribe('/serverToUsers/boardProperties', function (message) {
                let data = JSON.parse(message.body);
                this.setState((state) => ({boardWidth: data.size.width}));
                this.setState((state) => ({boardHeight: data.size.height}));
                this.setState((state) => ({minBoardWidth: data.minSize.width}));
                this.setState((state) => ({minBoardHeight: data.minSize.height}));
            }.bind(this)));

            this.props.stompClient.send("/userToServer/boardProperties/size/loadPage", {}, JSON.stringify({
                'width': document.querySelector(".dragAbleBoardOuter").clientWidth,
                'height': document.querySelector(".dragAbleBoardOuter").clientHeight
            }));
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
    }


    changeBoardSize(width, height) {
        this.props.stompClient.send("/userToServer/boardProperties/size/change", {}, JSON.stringify({
            'width': width,
            'height': height
        }));
    }

    changeBoardMinSize(minWidth, minHeight) {
        this.props.stompClient.send("/userToServer/boardProperties/minSize/change", {}, JSON.stringify({
            'width': minWidth,
            'height': minHeight
        }));
    }

    createComponent(componentType) {
        let doc = document.documentElement;
        let left = (window.pageXOffset || doc.scrollLeft) - (doc.clientLeft || 0);
        let top = (window.pageYOffset || doc.scrollTop) - (doc.clientTop || 0);
        let positionOnBoard = {
            left: Math.ceil(left + (window.innerWidth - document.querySelector(".sidePanel").clientWidth) / 2),
            top: Math.ceil(top + window.innerHeight / 2)
        };
        positionOnBoard.left = positionOnBoard.left % this.state.boardWidth;
        positionOnBoard.top = positionOnBoard.top % this.state.boardHeight;

        this.props.stompClient.send("/userToServer/component/create", {}, JSON.stringify({
            'type': componentType,
            'positionOnBoard': positionOnBoard
        }));
    }

    removeComponent(id) {
        this.props.stompClient.send("/userToServer/component/" + id + "/remove", {});
    }

    moveComponentOnBoard(id, left, top) {
        if (left < 0)
            left = 0;
        if (top < 0)
            top = 0;
        if (left > this.state.boardWidth-document.querySelector(".componentId-"+id).clientWidth)
            left = this.state.boardWidth-document.querySelector(".componentId-"+id).clientWidth;
        if (top > this.state.boardHeight-document.querySelector(".componentId-"+id).clientHeight)
            top = this.state.boardHeight-document.querySelector(".componentId-"+id).clientHeight;

        this.props.stompClient.send("/userToServer/component/" + id + "/moveOnBoard", {}, JSON.stringify({
            'left': left,
            'top': top
        }));
    }

    runComponent(id, run) {
        this.props.stompClient.send("/userToServer/component/" + id + "/run", {}, run);
    }

    changeAddConnectionState(newState) {
        this.setState((state) => ({addConnectionState: newState}));
        this.setState((state) => ({connectionFrom: null}));
    }

    changeRemoveConnectionState(newState) {
        this.setState((state) => ({removeConnectionState: newState}));
        this.setState((state) => ({connectionFrom: null}));
    }

    changeColorConnectionState(newState) {
        this.setState((state) => ({colorConnectionState: newState}));
        this.setState((state) => ({connectionFrom: null}));
    }

    componentClicked(id) {
        if (this.state.removeConnectionState || this.state.addConnectionState || this.state.colorConnectionState) {
            axios.get('component/' + id + '/adapters')
                .then(
                    response => {
                        if (response.data.adapters === null || this.state.removeConnectionState || this.state.colorConnectionState) {
                            this.addConnection(id, null);
                        } else {
                            let adapter = null;
                            let last = null;
                            let count = 0;
                            response.data.adapters.forEach(adapter => {
                                if (!adapter.disabled && !adapter.set) {
                                    last = adapter;
                                    count++;
                                }
                            });
                            if (count === 1)
                                adapter = last;

                            if (count <= 0) {
                                alert("No free adapter to make connection");
                            } else if (adapter != null) {
                                this.addConnection(id, adapter);
                            } else {
                                this.setState(update(this.state, {
                                    adapterSetting: {
                                        show: {$set: true},
                                        adapters: {$set: response.data.adapters},
                                        componentId: {$set: id}
                                    }
                                }));
                            }
                        }
                    },
                    error => {
                        alert("Error");
                    }
                );
        }
    }

    addConnection(id, adapter) {
        this.setState(update(this.state, {
            adapterSetting: {
                show: {$set: false},
                adapters: {$set: null},
                componentId: {$set: null}
            }
        }));
        if (this.state.connectionFrom === null) {
            this.setState((state) => ({connectionFrom: {id: id, adapter: adapter}}));
        } else if (this.state.connectionFrom.id != id) {
            let action = null;
            if (this.state.addConnectionState) {
                action = "addConnection";
            } else if (this.state.removeConnectionState) {
                action = "removeConnection";
            } else if (this.state.colorConnectionState) {
                action = "colorConnection";
            }
            if (action !== null) {
                this.props.stompClient.send("/userToServer/components/" + action, {}, JSON.stringify({
                    'fromComponent': this.state.connectionFrom,
                    'toComponent': {id: id, adapter: adapter},
                    'color': this.state.connectionColor
                }));
                this.cancelAdapterSelect();
            }
        }
    }

    cancelAdapterSelect() {
        this.setState((state) => ({connectionFrom: null}));
        this.setState(update(this.state, {
            adapterSetting: {
                show: {$set: false},
                adapters: {$set: null},
                componentId: {$set: null}
            }
        }));
    }

    refresh() {
        this.props.stompClient.send("/userToServer/components/refresh", {}, {});
    }

    setConnectionColor(color) {
        this.setState((state) => ({connectionColor: color}));
    }

    render() {
        let adapterSelect = null;
        if (this.state.adapterSetting.show) {
            adapterSelect = <AdapterSelect adapters={this.state.adapterSetting.adapters}
                                           componentId={this.state.adapterSetting.componentId}
                                           removeConnectionState={this.state.removeConnectionState}
                                           addConnection={this.addConnection}
                                           cancel={this.cancelAdapterSelect}/>
        }
        return (
            <section>
                {adapterSelect}
                <DragAbleBoard moveComponentOnBoard={this.moveComponentOnBoard}
                               componentsOnBoard={this.state.componentsOnBoard}
                               connectionsOfComponents={this.state.connectionsOfComponents}
                               width={this.state.boardWidth}
                               height={this.state.boardHeight}
                               runComponent={this.runComponent}
                               removeComponent={this.removeComponent}
                               componentClicked={this.componentClicked}
                               selectedComponentId={this.state.connectionFrom == null ? null : this.state.connectionFrom.id}/>
                <SidePanel createComponent={this.createComponent}
                           changeBoardSize={this.changeBoardSize}
                           boardWidth={this.state.boardWidth}
                           boardHeight={this.state.boardHeight}
                           minBoardWidth={this.state.minBoardWidth}
                           minBoardHeight={this.state.minBoardHeight}
                           addConnectionState={this.state.addConnectionState}
                           removeConnectionState={this.state.removeConnectionState}
                           colorConnectionState={this.state.colorConnectionState}
                           changeAddConnectionState={this.changeAddConnectionState}
                           changeRemoveConnectionState={this.changeRemoveConnectionState}
                           changeColorConnectionState={this.changeColorConnectionState}
                           connectionColor={this.state.connectionColor}
                           setConnectionColor={this.setConnectionColor}
                           refresh={this.refresh}/>
            </section>
        );
    }
}