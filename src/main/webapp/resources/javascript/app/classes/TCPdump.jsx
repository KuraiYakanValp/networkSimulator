import React from 'react';

export default class TCPdump extends React.Component {

    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.lastScrollHeight = this.tcpdump.scrollHeight;
        this.tcpdump.scrollTop = this.lastScrollHeight;
    }

    componentDidUpdate() {
        if (this.tcpdump.scrollTop >= this.lastScrollHeight-this.tcpdump.offsetHeight) {
            this.tcpdump.scrollTop = this.lastScrollHeight;
        }
        this.lastScrollHeight = this.tcpdump.scrollHeight;
    }


    render() {
        return (
            <div ref={(node) => {
                this.tcpdump = node
            }} className={"tcpdump"}>
                {this.props.tcpdump.map(row => {
                    return (<p key={row}>{row}</p>)
                })}
            </div>

        );
    }
}