import React from 'react';
import NumericInput from 'react-numeric-input';

export default class BoardSize extends React.Component {
    constructor(pros) {
        super(pros);
        this.changeWidthHandle = this.changeWidthHandle.bind(this);
        this.changeHeightHandle = this.changeHeightHandle.bind(this);
        this.decreaseSize = this.decreaseSize.bind(this);
        this.increaseSize = this.increaseSize.bind(this);
        this.state = {
            maxSize: 99999999,
            buttonsChange: 50
        }
    }

    validationOfWidth(width) {
        if (width < this.props.minBoardWidth)
            return this.props.minBoardWidth;
        if (width > this.state.maxSize)
            return this.state.maxSize;
        return width
    }

    validationOfHeight(height) {
        if (height < this.props.minBoardHeight)
            return this.props.minBoardHeight;
        if (height > this.state.maxSize)
            return this.state.maxSize;
        return height
    }


    changeWidthHandle(event) {
        this.props.changeBoardSize(this.validationOfWidth(event), this.props.boardHeight);
    }

    changeHeightHandle(event) {
        this.props.changeBoardSize(this.props.boardWidth, this.validationOfHeight(event));
    }

    decreaseSize() {
        this.props.changeBoardSize(this.validationOfWidth(this.props.boardWidth - this.state.buttonsChange), this.validationOfHeight(this.props.boardHeight - this.state.buttonsChange));
    }

    increaseSize() {
        this.props.changeBoardSize(this.validationOfWidth(this.props.boardWidth + this.state.buttonsChange), this.validationOfHeight(this.props.boardHeight + this.state.buttonsChange));
    }

    render() {
        return (
            <div>
                <span>Board size:</span>
                <div>
                    <NumericInput min={this.props.minBoardWidth} max={this.state.maxSize} step={5}
                                  value={this.props.boardWidth} onChange={this.changeWidthHandle} strict/>
                    <NumericInput min={this.props.minBoardHeight} max={this.state.maxSize} step={5}
                                  value={this.props.boardHeight} onChange={this.changeHeightHandle} strict/>
                </div>
                <div>
                    <input type="button" value="-" onClick={this.decreaseSize}/>
                    <input type="button" value="+" onClick={this.increaseSize}/>
                </div>
            </div>
        );
    }
}