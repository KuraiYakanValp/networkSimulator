import React from 'react';
import {BrowserRouter, Route, Switch} from 'react-router-dom';
import ComponentPage from "./ComponentPage.jsx";
import BoardPage from "./BoardPage.jsx";
import WaitHandler from "./WaitHandler.jsx";
import ErrorHandler from "./ErrorHandler.jsx";
import WebsocketConnectionHandler from "./WebsocketConnectionHandler.jsx";


export default class Page extends React.Component {

    constructor(props) {
        super(props);
        let socket = new SockJS('/websocket');
        let stompClient = Stomp.over(socket);
        stompClient.debug = null;
        stompClient.connect({}, function (frame) {
            this.setState((state) => ({
                disconnect: false
            }));
        }.bind(this), function () {
            if (this.state.disconnect!==null)
                this.setState((state) => ({
                    disconnect: true
                }));
        }.bind(this));
        this.state = {
            stompClient: stompClient,
            connectMethods: {},
            disconnect: false
        };
    }


    render() {
        return (
            <BrowserRouter>
                <WebsocketConnectionHandler disconnect={this.state.disconnect}>
                    <WaitHandler stompClient={this.state.stompClient}>
                        <ErrorHandler stompClient={this.state.stompClient}>
                            <Switch>
                                <Route path="/component/:id"
                                       render={(routeProps) => (
                                           <ComponentPage routeProps={routeProps} stompClient={this.state.stompClient}/>
                                       )}
                                />
                                <Route path="/"
                                       render={(routeProps) => (
                                           <BoardPage stompClient={this.state.stompClient}/>
                                       )}
                                />
                            </Switch>
                        </ErrorHandler>
                    </WaitHandler>
                </WebsocketConnectionHandler>
            </BrowserRouter>
        );
    }
}