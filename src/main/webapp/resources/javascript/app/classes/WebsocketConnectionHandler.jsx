import * as React from "react";
import PopUpWindow from "./PopUpWindow.jsx";

export default class WaitHandler extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            disconnect: null
        };
    }

    render() {
        let disconnect = null;
        if (this.props.disconnect)
            disconnect = (
                <PopUpWindow>
                    <p>Connection lost</p>
                </PopUpWindow>);
        return (
            <div>
                {disconnect}
                <div className="page">
                    {this.props.children}
                </div>
            </div>
        );
    }
}