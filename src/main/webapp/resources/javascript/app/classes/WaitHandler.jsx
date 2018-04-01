import * as React from "react";
import PopUpWindow from "./PopUpWindow.jsx";
import update from 'immutability-helper';

export default class WaitHandler extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            list:[],
            websocketSubscription: [],
            connectTimeout: null
        };
        this.connectToWebsocket = this.connectToWebsocket.bind(this);
    }



    connectToWebsocket() {
        if (this.props.stompClient.connected) {
            let websocketSubscription = [];
            websocketSubscription.push(this.props.stompClient.subscribe('/serverToUsers/wait', function (message) {
                let data = JSON.parse(message.body);
                this.setState(
                    update(this.state, {
                        list: {
                            $push: [data]
                        }
                    })
                );
            }.bind(this)));

            websocketSubscription.push(this.props.stompClient.subscribe('/serverToUsers/stopWait', function (message) {
                let data = JSON.parse(message.body);
                for (let i = 0; i < this.state.list.length; i++) {
                    if (this.state.list[i].id == data.id) {
                        this.setState(
                            update(this.state, {list: {$splice: [[i, 1]]}})
                        );
                        break;
                    }
                }
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
        this.state.list.some(function (waitItem) {
            show = (
                <PopUpWindow>
                    <img src={"/resources/images/circle-loading.gif"}/>
                    <p>{waitItem.message}</p>
                </PopUpWindow>);
            return true;
        });
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