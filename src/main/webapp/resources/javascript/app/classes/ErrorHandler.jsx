import * as React from "react";
import update from 'immutability-helper';
import PopUpWindow from "./PopUpWindow.jsx";

export default class WaitHandler extends React.Component {
    constructor(pros) {
        super(pros);
        this.connectToWebsocket = this.connectToWebsocket.bind(this);
        this.errorButtonHandle = this.errorButtonHandle.bind(this);
        this.state = {
            list: [],
            websocketSubscription: [],
            connectTimeout: null
        };
    }

    connectToWebsocket() {
        if (this.props.stompClient.connected) {
            let websocketSubscription = [];
            websocketSubscription.push(this.props.stompClient.subscribe('/serverToUsers/error', function (message) {
                let data = JSON.parse(message.body);
                data["id"] = Math.random();
                this.setState(
                    update(this.state, {
                        list: {
                            $push: [data]
                        }
                    })
                );
            }.bind(this)));

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

    errorButtonHandle(e, id) {
        for (let i = 0; i < this.state.list.length; i++) {
            if (this.state.list[i].id == id) {
                this.setState(
                    update(this.state, {list: {$splice: [[i, 1]]}})
                );
                break;
            }
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

    render() {
        let show;
        this.state.list.some(function (error) {
            show = (
                <PopUpWindow>
                    <img src={"/resources/images/error.png"}/>
                    <p>{error.message}</p>
                    <input type="button" value={"OK"} onClick={((e) => this.errorButtonHandle(e, error["id"]))}/>
                </PopUpWindow>);
            return true;
        }.bind(this));
        return (
            <div>
                {show}
                <div className="page">
                    {this.props.children}
                </div>
            </div>
        );
    }
}