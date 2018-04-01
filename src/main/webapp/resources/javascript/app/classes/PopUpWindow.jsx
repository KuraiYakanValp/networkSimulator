import * as React from "react";

export default class PopUpWindow extends React.Component {


    render() {
        return (
            <div className="popupBorder">
                <div className="popupBorderInner">
                    <div className="popupBorderContainer">
                        {this.props.children}
                    </div>
                </div>
            </div>
        );
    }
}